package com.guarda.zcash;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.guarda.zcash.crypto.Base58;
import com.guarda.zcash.crypto.BrainKeyDict;
import com.guarda.zcash.crypto.DumpedPrivateKey;
import com.guarda.zcash.crypto.ECKey;
import com.guarda.zcash.crypto.Sha256Hash;
import com.guarda.zcash.request.AbstractZCashRequest;
import com.guarda.zcash.request.CreateClaimTransaction_taddr;
import com.guarda.zcash.request.CreateTransaction_taddr;
import com.guarda.zcash.request.GetBalance_taddr;
import com.guarda.zcash.request.GetUTXOSRequest;
import com.guarda.zcash.request.PushTransaction_taddr;
import com.guarda.zcash.request.UpdateTransactionCache_taddr;
import com.guarda.ethereum.models.constants.Common;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.guarda.ethereum.models.constants.Const.ZEC_MAINNET_ADDR_PREFIX;
import static com.guarda.ethereum.models.constants.Const.ZEC_TESTNET_ADDR_PREFIX;


public class ZCashWalletManager {

  private static volatile ZCashWalletManager instance;
  public static String value;

  private ZCashWalletManager() {
  }

  public static ZCashWalletManager getInstance() {
    if (instance == null) {
      synchronized (ZCashWalletManager.class) {
        if (instance == null) {
          try {
            instance = new ZCashWalletManager(Common.NODE_ADDRESS, Common.BTC_NODE_LOGIN, Common.BTC_NODE_PASS);
          } catch (ZCashException zce) {
            zce.printStackTrace();
          }
        }
      }
    }
    return instance;
  }

  private static final String explorerAddress = "https://api.zcha.in/v2/mainnet/";
  public static final int EXPIRY_HEIGHT_NO_LIMIT = 0;
  private Vector<ZCashTransactionDetails_taddr> cachedTansactionDetails_taddr;

  public ZCashWalletManager(String nodeAddress, String nodeUser, String nodePassword) throws ZCashException {
    String methodName = "ZCashWalletManager counstructor";
    checkArgumentNonNull(nodeAddress, "nodeAddress", methodName);
    checkArgumentNonNull(nodeUser, "nodeUser", methodName);
    checkArgumentNonNull(nodePassword, "nodePassword", methodName);

    AbstractZCashRequest.setExplorerAddress(explorerAddress);
    AbstractZCashRequest.setAuth(nodeUser, nodePassword);
    AbstractZCashRequest.setNodeAddress(nodeAddress);
  }

  public static String generateNewPrivateKey_taddr() throws ZCashException {
    String fullkey = BrainKeyDict.suggestBrainKey(); //Append default sequence number
    String privateKey;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      byte[] bytes = md.digest(fullkey.getBytes("UTF-8"));
      MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
      byte[] result = sha256.digest(bytes);
      privateKey = ECKey.fromPrivate(result, true).getPrivateKeyEncoded().toBase58();
    } catch (NoSuchAlgorithmException e) {
      throw new ZCashException("NoSuchAlgotithmException in generateNewPrivatKey_taddr.", e);
    } catch (UnsupportedEncodingException e) {
      throw new ZCashException("UnsupportedEncodingException in generateNewPrivatKey_taddr.", e);
    }

    return privateKey;
  }

  public static String publicKeyFromPrivateKey_taddr(String privKey) throws ZCashException {
    String methodName = "publicKeyFromPrivateKey_taddr";
    checkArgumentNonNull(privKey, "privKey", methodName);
    try {
      Base58.decodeChecked(privKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid private key.");
    }

    ECKey key = DumpedPrivateKey.fromBase58(privKey);
    byte[] pubKey = key.getPubKeyPoint().getEncoded(key.isCompressed());

    pubKey = Sha256Hash.hash(pubKey);

    byte[] pubKeyHash = new byte[20];
    RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
    ripemd160Digest.update(pubKey, 0, pubKey.length);
    ripemd160Digest.doFinal(pubKeyHash, 0);

    pubKey = Bytes.concat(ZEC_MAINNET_ADDR_PREFIX, pubKeyHash);
//    pubKey = Bytes.concat(ZEC_TESTNET_ADDR_PREFIX, pubKeyHash);
    //                               ^~~~~~~~~~~~~~~~~~~~~~~~ mainnet prefix

    byte[] checksum = Sha256Hash.hashTwice(pubKey);
    byte[] summed = Bytes.concat(pubKey, new byte[]{checksum[0], checksum[1], checksum[2], checksum[3]});

    return Base58.encode(summed);
  }

  public void getBalance_taddr(final String pubKey,
                               final WalletCallback<String, Long> onComplete) throws ZCashException {
    String methodName = "getBalance_taddr";
    checkArgumentNonNull(pubKey, "pubKey", methodName);
    try {
      Base58.decodeChecked(pubKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid address.");
    }

    new Thread(new GetBalance_taddr(pubKey, cachedTansactionDetails_taddr, onComplete)).start();
  }

  public void createTransaction_taddr(final String fromAddr,
                                      final String toAddr,
                                      final Long amount,
                                      final Long fee,
                                      final String privateKey,
                                      final long minconf,
                                      final WalletCallback<String, ZCashTransaction_taddr> onComplete) throws ZCashException {
    createTransaction_taddr(fromAddr, toAddr, amount, fee, privateKey, minconf, EXPIRY_HEIGHT_NO_LIMIT, onComplete);
  }

  public void createTransaction_taddr(final String fromAddr,
                                      final String toAddr,
                                      final Long amount,
                                      final Long fee,
                                      final String privateKey,
                                      final long minconf,
                                      final int expiryHeight,
                                      final WalletCallback<String, ZCashTransaction_taddr> onComplete) throws ZCashException {
    String methodName = "createTransaction_taddr";
    checkArgumentNonNull(fromAddr, "fromAddr", methodName);
    checkArgumentNonNull(toAddr, "toAddr", methodName);
    checkArgumentNonNull(amount, "amount", methodName);
    checkArgumentNonNull(fee, "fee", methodName);
    checkArgumentNonNull(privateKey, "privateKey", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);
    try {
      Base58.decodeChecked(fromAddr);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid fromAddr.");
    }

    try {
      Base58.decodeChecked(toAddr);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid toAddr.");
    }

    try {
      Base58.decodeChecked(privateKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid private key.");
    }

//    if (!fromAddr.equals(publicKeyFromPrivateKey_taddr(privateKey))) {
//      throw new ZCashException("fromAddr does not correspond to privateKey in createTransaction_taddr");
//    }

    if (amount < 0) {
      throw new ZCashException("Cannot send negative amount of coins.");
    }

    if (fee < 0) {
      throw new ZCashException("Cannot create transaction with negative fee.");
    }

    if (amount + fee == 0) {
      throw new ZCashException("Transaction with amount + fee = 0 would not do anything.");
    }

    if(expiryHeight < 0 || expiryHeight > 499999999) {
      throw new ZCashException("Expiry height must be in [0, 499999999].");
    }

    new Thread(new GetUTXOSRequest(fromAddr, minconf, new WalletCallback<String, List<ZCashTransactionOutput>>() {

      @Override
      public void onResponse(String r1, List<ZCashTransactionOutput> r2) {
        if (r1.equals("ok")) {
          new CreateTransaction_taddr(fromAddr, toAddr, amount, fee, privateKey, expiryHeight, onComplete, r2).run();
        } else {
          onComplete.onResponse(r1, null);
        }
      }
    })).start();
  }

    public void createClaimTransaction_taddr(final String fromAddr,
                                        final String toAddr,
                                        final Long amount,
                                        final Long fee,
                                        final String privateKey,
                                        final long minconf,
                                        final long interest,
                                        final WalletCallback<String, ZCashTransaction_taddr> onComplete) throws ZCashException {
        String methodName = "createTransaction_taddr";
        checkArgumentNonNull(fromAddr, "fromAddr", methodName);
        checkArgumentNonNull(toAddr, "toAddr", methodName);
        checkArgumentNonNull(amount, "amount", methodName);
        checkArgumentNonNull(fee, "fee", methodName);
        checkArgumentNonNull(privateKey, "privateKey", methodName);
        checkArgumentNonNull(onComplete, "onComplete", methodName);
        try {
            Base58.decodeChecked(fromAddr);
        } catch (IllegalArgumentException e) {
            throw new ZCashException("Invalid fromAddr.");
        }

        try {
            Base58.decodeChecked(toAddr);
        } catch (IllegalArgumentException e) {
            throw new ZCashException("Invalid toAddr.");
        }

        try {
            Base58.decodeChecked(privateKey);
        } catch (IllegalArgumentException e) {
            throw new ZCashException("Invalid private key.");
        }

        if (amount < 0) {
            throw new ZCashException("Cannot send negative amount of coins.");
        }

        if (fee < 0) {
            throw new ZCashException("Cannot create transaction with negative fee.");
        }

        if (amount + fee == 0) {
            throw new ZCashException("Transaction with amount + fee = 0 would not do anything.");
        }

        if (interest <= 0) {
            throw new ZCashException("Transaction for claiming with interest <= 0 would not do anything.");
        }

        new Thread(new GetUTXOSRequest(fromAddr, minconf, new WalletCallback<String, List<ZCashTransactionOutput>>() {

            @Override
            public void onResponse(String r1, List<ZCashTransactionOutput> r2) {
                if (r1.equals("ok")) {
                    new CreateClaimTransaction_taddr(fromAddr, toAddr, amount, fee, privateKey, EXPIRY_HEIGHT_NO_LIMIT, interest, onComplete, r2).run();
                } else {
                    onComplete.onResponse(r1, null);
                }
            }
        })).start();
    }

  public void pushTransaction_taddr(ZCashTransaction_taddr tx,
                                    WalletCallback<String, Void> onComplete) throws ZCashException {
    String methodName = "pushTransaction_taddr";
    checkArgumentNonNull(tx, "tx", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);

    Thread net = new Thread(new PushTransaction_taddr(tx, onComplete));
    net.start();
  }

  public void getTransactionHistory_taddr(String pubKey,
                                          final int limit,
                                          final int offset,
                                          final UpdateRequirement requirement,
                                          final WalletCallback<String, List<ZCashTransactionDetails_taddr>> onComplete) throws ZCashException {
    String methodName = "getTransactionHistory_taddr";
    checkArgumentNonNull(pubKey, "pubKey", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);

    try {
      Base58.decodeChecked(pubKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid address.");
    }

    if (limit < 0) {
      throw new ZCashException("Negative limit is not allowed in getTransactionHistory_taddr");
    }

    if (offset < 0) {
      throw new ZCashException("Negative offset is not allowed in getTransactionHistory_taddr");
    }

    if (requirement == UpdateRequirement.NO_UPDATE) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          if (cachedTansactionDetails_taddr == null) {
            onComplete.onResponse("Wallet is not imported.", null);
            return;
          }

          int start, end;
          int size = cachedTansactionDetails_taddr.size();
          end = offset > size ? 0 : size - offset;
          start = limit > end ? 0 : end - limit;
          List<ZCashTransactionDetails_taddr> result = new LinkedList<>(cachedTansactionDetails_taddr.subList(start, end));
          Collections.reverse(result);
          onComplete.onResponse("ok", result);
        }
      }).start();
      return;
    }

    Thread net = new Thread(new UpdateTransactionCache_taddr(cachedTansactionDetails_taddr, false, pubKey, new WalletCallback<String, Void>() {
      @Override
      public void onResponse(String r1, Void r2) {
        if (r1.equals("ok") || requirement == UpdateRequirement.TRY_UPDATE) {
          int start, end;
          synchronized (cachedTansactionDetails_taddr) {
            if (cachedTansactionDetails_taddr == null) {
              onComplete.onResponse("Wallet is not imported.", null);
              return;
            }

            int size = cachedTansactionDetails_taddr.size();
            end = offset > size ? 0 : size - offset;
            start = limit > end ? 0 : end - limit;
            List<ZCashTransactionDetails_taddr> result = new LinkedList<>(cachedTansactionDetails_taddr.subList(start, end));
            Collections.reverse(result);
            onComplete.onResponse("ok", result);
          }
        } else {
          onComplete.onResponse(r1, null);
        }
      }
    }));
    net.start();
  }

  public void importWallet_taddr(String privateKey,
                                 final UpdateRequirement requirement,
                                 final WalletCallback<String, Void> onComplete) throws ZCashException {
    String methodName = "importWallet_taddr";
    checkArgumentNonNull(privateKey, "privateKey", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);
    try {
      Base58.decodeChecked(privateKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid private key.");
    }


    cachedTansactionDetails_taddr = new Vector<>();
    String pubKey;
    if (requirement == UpdateRequirement.NO_UPDATE) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          onComplete.onResponse("ok", null);
        }
      }).start();
    } else {
      pubKey = publicKeyFromPrivateKey_taddr(privateKey);
      new Thread(new UpdateTransactionCache_taddr(cachedTansactionDetails_taddr, true, pubKey, new WalletCallback<String, Void>() {
        @Override
        public void onResponse(String r1, Void r2) {
          if (r1.equals("ok") || requirement == UpdateRequirement.TRY_UPDATE) {
            onComplete.onResponse("ok", null);
          } else {
            onComplete.onResponse(r1, null);
          }
        }
      })).start();
    }
  }

  public void importWallet_taddr(String privateKey,
                                 Vector<ZCashTransactionDetails_taddr> cache,
                                 final UpdateRequirement requirement,
                                 final WalletCallback<String, Void> onComplete) throws ZCashException {
    String methodName = "importWallet_taddr";
    checkArgumentNonNull(privateKey, "privateKey", methodName);
    checkArgumentNonNull(cache, "cache", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);
    try {
      Base58.decodeChecked(privateKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid private key.");
    }

    if (cache.size() > 1) {
      ZCashTransactionDetails_taddr first = cache.get(0);
      for (int i = 1; i < cache.size(); i++) {
        ZCashTransactionDetails_taddr second = cache.get(i);
        if (first.blockHeight > second.blockHeight) {
          throw new ZCashException("Transactions in cache must be sorted in ascending order.");
        }

        first = second;
      }
    }

    cachedTansactionDetails_taddr = cache;
    String pubKey;
    if (requirement == UpdateRequirement.NO_UPDATE) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          onComplete.onResponse("ok", null);
        }
      }).start();
    } else {
      pubKey = publicKeyFromPrivateKey_taddr(privateKey);
      new Thread(new UpdateTransactionCache_taddr(cachedTansactionDetails_taddr, false, pubKey, new WalletCallback<String, Void>() {
        @Override
        public void onResponse(String r1, Void r2) {
          if (r1.equals("ok") || requirement == UpdateRequirement.TRY_UPDATE) {
            onComplete.onResponse("ok", null);
          } else {
            onComplete.onResponse(r1, null);
          }
        }
      })).start();
    }
  }

  public static String generateNewPrivateKey_zaddr() throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public static String publicKeyFromPrivateKey_zaddr(String privKey) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public void getBalance_zaddr(String pubKey, WalletCallback<String, Long> onComplete) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public ZCashTransaction_zaddr createTransaction_zaddr(String fromAddr, String toAddr, BigInteger amount, BigDecimal fee, String memo, String privateKey) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public void pushTransaction_zaddr(ZCashTransaction_zaddr tx, WalletCallback<String, Void> onComplete) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public void getTransactionHistory_zaddr(String pubKey, int limit, int offset, WalletCallback<String, List<ZCashTransactionDetails_zaddr>> onComplete) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public void importWallet_zaddr(String privateKey, WalletCallback<String, Void> onComplete) throws ZCashException {
    throw new ZCashException("Unimplemented");
  }

  public Vector<ZCashTransactionDetails_taddr> getTransactionCache() {
    return cachedTansactionDetails_taddr;
  }

  private static void checkArgumentNonNull(Object o, String name, String methodName) throws ZCashException {
    if (o == null) {
      throw new ZCashException(String.format("Parameter %s of %s is null.", name, methodName));
    }
  }

  public enum UpdateRequirement {
    NO_UPDATE,
    TRY_UPDATE,
    REQUIRE_UPDATE
  }
}
