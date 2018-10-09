package com.gravilink.decent.request;

import com.google.common.primitives.Bytes;
import com.gravilink.decent.DecentListener;
import com.gravilink.decent.DecentTransaction;
import com.gravilink.decent.DecentWallet;
import com.gravilink.decent.DecentWalletManager;
import com.gravilink.decent.TransferOperation;
import com.gravilink.decent.crypto.ECKey;
import com.gravilink.decent.crypto.Sha256Hash;
import com.gravilink.decent.crypto.Utils;
import com.neovisionaries.ws.client.WebSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.spec.ECPoint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class PushTransaction extends AbstractDecentRequest {

  private static final int EXPIRATION_TIME = 30;
  private static final char[] hexArray = "0123456789abcdef".toCharArray();

  private static final int GET_CHAIN_ID = 0;
  private static final int GET_DYNAMIC_DATA = 1;
  private static final int GET_TRANSACTION_RESULT = 2;

  private static String chainId = null;

  private DecentTransaction transaction;
  private TransferOperation operation;
  private long expiration;
  private long headBlockPrefix;
  private long headBlockNumber;


  private int RequestStage;

  public PushTransaction(DecentWalletManager manager, DecentListener adapter, DecentTransaction transaction, DecentWallet wallet) {
    super(manager, adapter, wallet);
    this.transaction = transaction;
    this.operation = (TransferOperation) transaction.operations.get(0);
  }

  @Override
  boolean isDatabaseRequired() {
    return true;
  }

  @Override
  boolean isHistoryRequired() {
    return false;
  }

  @Override
  boolean isBroadcastRequired() {
    return true;
  }

  @Override
  boolean onReady(WebSocket socket, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    if (chainId == null) {
      socket.sendText(String.format(Locale.ENGLISH, "{\"id\":0, \"method\":\"call\", \"params\":[%d, \"get_chain_id\", []]}", databaseApiIndex));
      RequestStage = GET_CHAIN_ID;
    } else {
      socket.sendText("{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_dynamic_global_properties\", []]}");
      RequestStage = GET_DYNAMIC_DATA;
    }
    return false;
  }

  @Override
  boolean onResponse(WebSocket socket, JSONObject response, int databaseApiIndex, int historyApiIndex, int broadcastApiIndex) {
    try {
      switch (RequestStage) {
        case GET_CHAIN_ID: {
          PushTransaction.chainId = response.getString("result");
          socket.sendText("{\"id\":0, \"method\":\"call\", \"params\":[0, \"get_dynamic_global_properties\", []]}");
          RequestStage++;
          return false;
        }

        case GET_DYNAMIC_DATA: {
          JSONObject result = response.getJSONObject("result");
          this.headBlockNumber = result.getInt("head_block_number");
          String headBlockId = result.getString("head_block_id");

          Date blockTime;
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
          dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
          blockTime = dateFormat.parse(result.getString("time"));
          expiration = blockTime.getTime() / 1000 + EXPIRATION_TIME;

          String hashData = headBlockId.substring(8, 16);
          StringBuilder builder = new StringBuilder();
          for (int i = 0; i < 8; i = i + 2) {
            builder.append(hashData.substring(6 - i, 8 - i));
          }
          headBlockPrefix = Long.parseLong(builder.toString(), 16);

          String sign = getSignature();
          String expirationString = dateFormat.format(new Date(expiration * 1000));

          String call = String.format(Locale.ENGLISH,
                  "{\"id\":0,\"method\":\"call\",\"params\":[\"%d\", \"broadcast_transaction\", [{" +
                          "\"expiration\":\"%s\",\"signatures\":[\"%s\"]," +
                          "\"operations\":[[0, {" +
                          "\"fee\":{\"amount\":%d,\"asset_id\":\"1.3.0\"}," +
                          "\"from\":\"%s\", \"to\":\"%s\"" +
                          "\"amount\":{\"amount\":%d, \"asset_id\":\"1.3.0\"}," +
                          "\"extensions\":[]}]]," +
                          "\"extensions\":[], \"ref_block_num\":%d, \"ref_block_prefix\":%d}]]}",
                  broadcastApiIndex, expirationString, sign, operation.fee,
                  operation.from.getId(), operation.to.getId(), operation.amount, headBlockNumber, headBlockPrefix
          );

          socket.sendText(call);
          RequestStage++;
          return false;
        }

        case GET_TRANSACTION_RESULT: {
          adapter.onTransactionPushed(transaction);
          return true;
        }
      }
    } catch (JSONException | IOException | ParseException e) {
      onError(e);
      return true;
    }
    return false;
  }

  private String getSignature() throws IOException {
    boolean isGrapheneCanonical = false;
    byte[] sigData = null;

    while (!isGrapheneCanonical) {
      byte[] serializedTransaction = this.getSerialized();
      Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction));
      int recId = -1;
      ECKey.ECDSASignature sig = wallet.getPrivateKey().sign(hash);

      // Now we have to work backwards to figure out the recId needed to recover the signature.
      for (int i = 0; i < 4; i++) {
        ECKey k = ECKey.recoverFromSignature(i, sig, hash, wallet.getPrivateKey().isCompressed());
        if (k != null && k.getPubKeyPoint().equals(wallet.getPrivateKey().getPubKeyPoint())) {
          recId = i;
          break;
        }
      }

      sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
      int headerByte = recId + 27 + (wallet.getPrivateKey().isCompressed() ? 4 : 0);
      sigData[0] = (byte) headerByte;
      System.arraycopy(Utils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
      System.arraycopy(Utils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);

      // Further "canonicality" tests
      if (((sigData[0] & 0x80) != 0) || (sigData[0] == 0) ||
              ((sigData[1] & 0x80) != 0) || ((sigData[32] & 0x80) != 0) ||
              (sigData[32] == 0) || ((sigData[33] & 0x80) != 0)) {
        this.expiration++;
      } else {
        isGrapheneCanonical = true;
      }
    }
    char[] result = new char[sigData.length * 2];
    for (int i = 0; i < sigData.length; i++) {
      int v = sigData[i] & 0xFF;
      result[i * 2] = hexArray[v >>> 4];
      result[i * 2 + 1] = hexArray[v & 0x0F];
    }

    return new String(result);
  }

  private byte[] getSerialized() throws IOException {
    List<Byte> result = new ArrayList<>();

    byte[] buffer = new byte[chainId.length() / 2];
    for (int i = 0; i < chainId.length(); i += 2) {
      buffer[i / 2] = (byte) ((Character.digit(chainId.charAt(i), 16) << 4)
              + Character.digit(chainId.charAt(i + 1), 16));
    }
    result.addAll(Bytes.asList(buffer));//Chain id bytes.

    buffer = new byte[]{
            (byte) headBlockNumber, (byte) (headBlockNumber >> 8),
            (byte) this.headBlockPrefix, (byte) (this.headBlockPrefix >> 8), (byte) (this.headBlockPrefix >> (8 * 2)), (byte) (this.headBlockPrefix >> (8 * 3)),
            (byte) this.expiration, (byte) (this.expiration >> 8), (byte) (this.expiration >> (8 * 2)), (byte) (this.expiration >> (8 * 3))
    };
    result.addAll(Bytes.asList(buffer));//Block info bytes.

    result.add((byte) 1);//Operation count bytes.
    result.add((byte) 0);//Operation type bytes.

    result.addAll(Bytes.asList(ByteBuffer.allocate(Long.SIZE / 8).putLong(Long.reverseBytes(operation.fee)).array()));//Fee amount.
    result.add((byte) 0);//Fee asset type byte.

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutput dataOutput = new DataOutputStream(byteArrayOutputStream);
    long value = Long.valueOf(operation.from.getId().substring(4, operation.from.getId().length()));
    while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
      dataOutput.writeByte(((int) value & 0x7F) | 0x80);
      value >>>= 7;
    }
    dataOutput.writeByte((int) value & 0x7F);
    result.addAll(Bytes.asList(byteArrayOutputStream.toByteArray()));//From user bytes.

    byteArrayOutputStream = new ByteArrayOutputStream();
    dataOutput = new DataOutputStream(byteArrayOutputStream);
    value = Long.valueOf(operation.to.getId().substring(4, operation.to.getId().length()));
    while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
      dataOutput.writeByte(((int) value & 0x7F) | 0x80);
      value >>>= 7;
    }
    dataOutput.writeByte((int) value & 0x7F);
    result.addAll(Bytes.asList(byteArrayOutputStream.toByteArray()));//To user bytes.

    result.addAll(Bytes.asList(ByteBuffer.allocate(Long.SIZE / 8).putLong(Long.reverseBytes(operation.amount)).array()));//Amount amount.
    result.add((byte) 0);//Amount asset type byte.

    result.add((byte) 0);//Memo byte.
    result.add((byte) 0);//Operation extensions byte.

    result.add((byte) 0);//Extensions byte.
    return Bytes.toArray(result);
  }
}
