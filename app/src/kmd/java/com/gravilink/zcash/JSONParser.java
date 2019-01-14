package com.gravilink.zcash;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class JSONParser {

  private static final String HASH = "hash";
  private static final String MAINCHAIN = "mainChain";
  private static final String FEE = "fee";
  private static final String TYPE = "type";
  private static final String SHIELDED = "shielded";
  private static final String INDEX = "index";
  private static final String BLOCKHASH = "blockHash";
  private static final String BLOCKHEIGHT = "blockHeight";
  private static final String VERSION = "version";
  private static final String TIMESTAMP = "timestamp";
  private static final String TIME = "time";
  private static final String VIN = "vin";
  private static final String VOUT = "vout";
  private static final String VJOINSPLIT = "vjoinsplit";
  private static final String LOCKTIME = "lockTime";
  private static final String VALUE = "value";
  private static final String OUTPUTVALUE = "outputValue";
  private static final String SHIELDEDVALUE = "shieldedValue";
  private static final String RETRIEVEDVOUT = "retrievedVout";
  private static final String N = "n";
  private static final String SCRIPTPUBKEY = "scriptPubKey";
  private static final String ADDRESSES = "addresses";
  private static final String ASM = "asm";
  private static final String HEX = "hex";
  private static final String REQSIGS = "reqSigs";
  private static final String VALUEZAT = "valueZat";
  private static final String SCRIPTSIG = "scriptSig";
  private static final String TXID = "txid";
  private static final String COINBASE = "coinbase";
  private static final String SEQUENCE = "sequence";


  public static List<ZCashTransactionDetails_taddr> parseTxArray(InputStream is) throws IOException {
    JsonReader reader = new JsonReader(new InputStreamReader(is, "UTF-8"));
    List<ZCashTransactionDetails_taddr> txs = null;
    try {
      txs = readTxArray(reader);
      reader.close();
    } catch (IOException e) {
      e.printStackTrace();
      Log.e("Error message", e.getMessage());
    }

    return txs;
  }

  private static List<ZCashTransactionDetails_taddr> readTxArray(JsonReader reader) throws IOException {
    List<ZCashTransactionDetails_taddr> txs = new LinkedList<>();
    reader.beginArray();
    while (reader.hasNext()) {
      txs.add(readTx(reader));
    }

    reader.endArray();
    return txs;
  }

  private static ZCashTransactionDetails_taddr readTx(JsonReader reader) throws IOException, IllegalStateException {
    ZCashTransactionDetails_taddr tx = new ZCashTransactionDetails_taddr();
    if(reader.peek() != JsonToken.BEGIN_OBJECT) {
      throw new IOException("Cannot parse JSON");
    }

    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      String name = reader.nextName();
      switch (name) {
        case HASH:
          tx.hash = reader.nextString();
          break;
        case MAINCHAIN:
          tx.mainChain = reader.nextBoolean();
          break;
        case FEE:
          tx.fee = Double.valueOf(reader.nextDouble() * 1e8).longValue();
          break;
        case TYPE:
          tx.type = reader.nextString();
          break;
        case SHIELDED:
          tx.shielded = reader.nextBoolean();
          break;
        case INDEX:
          tx.index = reader.nextLong();
          break;
        case BLOCKHASH:
          tx.blockHash = reader.nextString();
          break;
        case BLOCKHEIGHT:
          tx.blockHeight = reader.nextLong();
          break;
        case VERSION:
          tx.version = reader.nextLong();
          break;
        case LOCKTIME:
          tx.locktime = reader.nextLong();
          break;
        case TIME:
          tx.time = reader.nextLong();
          break;
        case TIMESTAMP:
          tx.timestamp = reader.nextLong();
          break;
        case VIN:
          tx.vin = readTxInputs(reader);
          break;
        case VOUT:
          tx.vout = readTxOutputs(reader, null);
          break;
        case VJOINSPLIT:
          skipJoinSplits(reader);
          break;
        case VALUE:
          tx.value = Double.valueOf(reader.nextDouble() * 1e8).longValue();
          break;
        case OUTPUTVALUE:
          tx.outputValue = Double.valueOf(reader.nextDouble() * 1e8).longValue();
          break;
        case SHIELDEDVALUE:
          tx.shieldedValue = Double.valueOf(reader.nextDouble() * 1e8).longValue();
          break;
        default:
          reader.skipValue();
      }
    }

    reader.endObject();
    return tx;
  }

  private static void skipJoinSplits(JsonReader reader) throws IOException {
    reader.skipValue();
  }

  private static Vector<ZCashTransactionOutput> readTxOutputs(JsonReader reader, String txid) throws IOException {
    Vector<ZCashTransactionOutput> vout = new Vector<>();
    //reader.nextName();
    reader.beginArray();
    while (reader.hasNext()) {
      ZCashTransactionOutput out = readTxSingleOutput(reader);
      out.txid = txid;
      vout.add(out);
    }

    reader.endArray();
    return vout;
  }

  private static ZCashTransactionOutput readTxSingleOutput(JsonReader reader) throws IOException {
    ZCashTransactionOutput output = new ZCashTransactionOutput();
    reader.beginObject(); //output
    while (reader.peek() != JsonToken.END_OBJECT) {
      String name = reader.nextName();
      switch (name) {
        case N:
          output.n = reader.nextLong();
          break;
        case SCRIPTPUBKEY:
          reader.beginObject();
          while (reader.peek() != JsonToken.END_OBJECT) {
            name = reader.nextName();
            switch (name) {
              case ADDRESSES:
                reader.beginArray();
                while (reader.hasNext()) {
                  output.address = reader.nextString();
                }
                reader.endArray();
                break;
              case ASM:
                output.asm = reader.nextString();
                break;
              case HEX:
                output.hex = reader.nextString();
                break;
              case REQSIGS:
                output.regSigs = reader.nextLong();
                break;
              case TYPE:
                output.type = reader.nextString();
                break;
              default:
                reader.skipValue();
            }
          }
          reader.endObject();
          break;
        case VALUE:
          output.value = Double.valueOf(reader.nextDouble() * 1e8).longValue();
          break;
        case VALUEZAT:
          output.value = reader.nextLong();
          break;
        default:
          reader.skipValue();
      }
    }

    reader.endObject(); //output end
    return output;
  }

  private static Vector<ZCashTransactionOutput> readTxInputs(JsonReader reader) throws IOException {
    Vector<ZCashTransactionOutput> vin = new Vector<>();
    reader.beginArray();
    while (reader.hasNext()) {
      vin.add(readTxSingleInput(reader));
    }

    reader.endArray();
    return vin;
  }

  private static ZCashTransactionInput readTxSingleInput(JsonReader reader) throws IOException {
    ZCashTransactionInput input = new ZCashTransactionInput();
    reader.beginObject();
    while (reader.peek() != JsonToken.END_OBJECT) {
      String name = reader.nextName();
      switch (name) {
        case COINBASE:
          input.coinbase = reader.nextString();
          break;
        case SEQUENCE:
          input.sequence = reader.nextLong();
          break;
        case TXID:
          input.txid = reader.nextString();
          break;
        case VOUT:
          input.n = reader.nextLong();
          break;
        case SCRIPTSIG:
          reader.skipValue();
          break;
        case RETRIEVEDVOUT:
          input.copyDataFrom(readTxSingleOutput(reader));
          break;
        default:
          reader.skipValue();
      }
    }

    reader.endObject();
    return input;
  }

  private static long readFieldLong(JsonReader reader, boolean fromDouble) throws IOException {
    reader.nextName();
    if (fromDouble) {
      return Double.valueOf(reader.nextDouble() * 1e8).longValue();
    }

    return reader.nextLong();
  }

  private static String readFieldString(JsonReader reader) throws IOException {
    reader.nextName();
    return reader.nextString();
  }

  private static boolean readFieldBoolean(JsonReader reader) throws IOException {
    reader.nextName();
    return reader.nextBoolean();
  }
}
