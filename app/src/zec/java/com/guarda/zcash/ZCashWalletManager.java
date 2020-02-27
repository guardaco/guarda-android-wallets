package com.guarda.zcash;

import com.google.common.primitives.Bytes;
import com.guarda.ethereum.models.constants.Common;
import com.guarda.zcash.crypto.Base58;
import com.guarda.zcash.crypto.BrainKeyDict;
import com.guarda.zcash.crypto.DumpedPrivateKey;
import com.guarda.zcash.crypto.ECKey;
import com.guarda.zcash.crypto.Sha256Hash;
import com.guarda.zcash.request.AbstractZCashRequest;
import com.guarda.zcash.request.CreateTransaction_taddr;
import com.guarda.zcash.request.CreateTransaction_ttoz;
import com.guarda.zcash.request.CreateTransaction_zaddr;
import com.guarda.zcash.request.CreateTransaction_ztot;
import com.guarda.zcash.request.GetBalance_taddr;
import com.guarda.zcash.request.GetUTXOSRequest;
import com.guarda.zcash.request.UpdateTransactionCache_taddr;
import com.guarda.zcash.sapling.db.DbManager;
import com.guarda.zcash.sapling.key.SaplingCustomFullKey;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Vector;

import static com.guarda.ethereum.models.constants.Const.ZEC_MAINNET_ADDR_PREFIX;
import static com.guarda.ethereum.models.constants.Const.ZEC_TESTNET_ADDR_PREFIX;


public class ZCashWalletManager {

  private static volatile ZCashWalletManager instance;
  public static String value;

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
    //                               ^~~~~~~~~~~~~~~~~~~~~~~~ mainnet prefix
//    pubKey = Bytes.concat(ZEC_TESTNET_ADDR_PREFIX, pubKeyHash);
    //                               ^~~~~~~~~~~~~~~~~~~~~~~~ testnet prefix

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

  //only for t-to-t transparent transactions
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

    if (!fromAddr.equals(publicKeyFromPrivateKey_taddr(privateKey))) {
      throw new ZCashException("fromAddr does not correspond to privateKey in createTransaction_taddr");
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

    if(expiryHeight < 0 || expiryHeight > 499999999) {
      throw new ZCashException("Expiry height must be in [0, 499999999].");
    }

    new Thread(new GetUTXOSRequest(fromAddr, minconf, (r1, r2) -> {
        if (r1.equals("ok")) {
          new CreateTransaction_taddr(fromAddr, toAddr, amount, fee, privateKey, expiryHeight, onComplete, r2).run();
        } else {
          onComplete.onResponse(r1, null);
        }
    })).start();
  }

  //only for t-to-z transactions
  public void createTransaction_ttoz(final String fromAddr,
                                      final String toAddr,
                                      final Long amount,
                                      final Long fee,
                                      final String memo,
                                      final String privateKey,
                                      final SaplingCustomFullKey saplingCustomFullKey,
                                      final long minconf,
                                      final WalletCallback<String, ZCashTransaction_ttoz> onComplete) throws ZCashException {
    createTransaction_ttoz(fromAddr, toAddr, amount, fee, memo, privateKey, saplingCustomFullKey, minconf, EXPIRY_HEIGHT_NO_LIMIT, onComplete);
  }

  public void createTransaction_ttoz(final String fromAddr,
                                     final String toAddr,
                                     final Long amount,
                                     final Long fee,
                                     final String memo,
                                     final String privateKey,
                                     final SaplingCustomFullKey saplingCustomFullKey,
                                     final long minconf,
                                     final int expiryHeight,
                                     final WalletCallback<String, ZCashTransaction_ttoz> onComplete) throws ZCashException {
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
      Base58.decodeChecked(privateKey);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid private key.");
    }

    if (!fromAddr.equals(publicKeyFromPrivateKey_taddr(privateKey))) {
      throw new ZCashException("fromAddr does not correspond to privateKey in createTransaction_taddr");
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

    if(expiryHeight < 0 || expiryHeight > 499999999) {
      throw new ZCashException("Expiry height must be in [0, 499999999].");
    }

    if (memo.getBytes().length > 512) {
      throw new ZCashException("Memo field is too long");
    }

    new Thread(new GetUTXOSRequest(fromAddr, minconf, (r1, r2) -> {
        if (r1.equals("ok")) {
          new CreateTransaction_ttoz(fromAddr, toAddr, amount, fee, memo, privateKey, saplingCustomFullKey, expiryHeight, onComplete, r2).run();
        } else {
          onComplete.onResponse(r1, null);
        }
    })).start();
  }

  //only for sapling z to z transactions
  public void createTransaction_zaddr(final String fromAddr,
                                      final String toAddr,
                                      final Long amount,
                                      final Long fee,
                                      final String memo,
                                      final SaplingCustomFullKey privateKey,
                                      final long minconf,
                                      final DbManager dbManager,
                                      final WalletCallback<String, ZcashTransaction> onComplete) throws ZCashException {
    createTransaction_zaddr(fromAddr, toAddr, amount, fee, memo, privateKey, minconf, EXPIRY_HEIGHT_NO_LIMIT, dbManager, onComplete);
  }

  private void createTransaction_zaddr(final String fromAddr,
                                       final String toAddr,
                                       final Long amount,
                                       final Long fee,
                                       final String memo,
                                       final SaplingCustomFullKey privateKey,
                                       final long minconf,
                                       final int expiryHeight,
                                       final DbManager dbManager,
                                       final WalletCallback<String, ZcashTransaction> onComplete) throws ZCashException {

    if (amount < 0) {
      throw new ZCashException("Cannot send negative amount of coins.");
    }

    if (fee < 0) {
      throw new ZCashException("Cannot create transaction with negative fee.");
    }

    if (amount + fee == 0) {
      throw new ZCashException("Transaction with amount + fee = 0 would not do anything.");
    }

    if (memo.getBytes().length > 512) {
      throw new ZCashException("Memo field is too long");
    }

    new CreateTransaction_zaddr(fromAddr, toAddr, amount, fee, memo, privateKey, expiryHeight, dbManager, onComplete).run();
  }

  //only for z-to-t transactions
  public void createTransaction_ztot(final String fromAddr,
                                     final String toAddr,
                                     final Long amount,
                                     final Long fee,
                                     final SaplingCustomFullKey privateKey,
                                     final DbManager dbManager,
                                     final WalletCallback<String, ZcashTransaction> onComplete) throws ZCashException {
    createTransaction_ztot(fromAddr, toAddr, amount, fee, privateKey, EXPIRY_HEIGHT_NO_LIMIT, dbManager, onComplete);
  }

  private void createTransaction_ztot(final String fromAddr,
                                     final String toAddr,
                                     final Long amount,
                                     final Long fee,
                                     final SaplingCustomFullKey privateKey,
                                     final int expiryHeight,
                                     final DbManager dbManager,
                                     final WalletCallback<String, ZcashTransaction> onComplete) throws ZCashException {
    String methodName = "createTransaction_ztot";
    checkArgumentNonNull(fromAddr, "fromAddr", methodName);
    checkArgumentNonNull(toAddr, "toAddr", methodName);
    checkArgumentNonNull(amount, "amount", methodName);
    checkArgumentNonNull(fee, "fee", methodName);
    checkArgumentNonNull(onComplete, "onComplete", methodName);

    try {
      Base58.decodeChecked(toAddr);
    } catch (IllegalArgumentException e) {
      throw new ZCashException("Invalid toAddr.");
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

    new CreateTransaction_ztot(fromAddr, toAddr, amount, fee, privateKey, expiryHeight, dbManager, onComplete).run();
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
