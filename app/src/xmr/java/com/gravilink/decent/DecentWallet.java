package com.gravilink.decent;

import com.gravilink.decent.crypto.DumpedPrivateKey;
import com.gravilink.decent.crypto.ECKey;

import de.adorsys.android.securestoragelibrary.SecurePreferences;

public class DecentWallet extends DecentAccount {
  private static final String WALLET_PRIVATE_KEY = "walletPrivateKey";
  private static final String WALLET_PUBLIC_KEY = "walletPublicKey";
  private static final String WALLET_NAME = "walletName";
  private static final String WALLET_ID = "walletId";

  private ECKey privateKey;

  public DecentWallet() {
    String privateKey = SecurePreferences.getStringValue(WALLET_PRIVATE_KEY, "");
    String publicKey = SecurePreferences.getStringValue(WALLET_PUBLIC_KEY, "");
    String id = SecurePreferences.getStringValue(WALLET_ID, "");
    String name = SecurePreferences.getStringValue(WALLET_NAME, "");
    if (!privateKey.isEmpty() && !publicKey.isEmpty() && !id.isEmpty() && !name.isEmpty()) {
      try {
        setPrivateKey(privateKey);
      } catch (Exception e) {}
      setId(id);
      setName(name);
      setPublicKey(publicKey);
    }
  }

  public void setPrivateKey(String privateKey) throws Exception{
    this.privateKey = DumpedPrivateKey.fromBase58(privateKey);
    SecurePreferences.setValue(WALLET_PRIVATE_KEY, privateKey);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    SecurePreferences.setValue(WALLET_NAME, name);
  }

  @Override
  public void setId(String id) {
    super.setId(id);
    SecurePreferences.setValue(WALLET_ID, id);
  }

  @Override
  public void setPublicKey(String publicKey) {
    super.setPublicKey(publicKey);
    SecurePreferences.setValue(WALLET_PUBLIC_KEY, publicKey);
  }

  public ECKey getPrivateKey() {
    return privateKey;
  }

  @Override
  public void clear() {
    super.clear();
    this.privateKey = null;
    SecurePreferences.removeValue(WALLET_PUBLIC_KEY);
    SecurePreferences.removeValue(WALLET_NAME);
    SecurePreferences.removeValue(WALLET_PRIVATE_KEY);
    SecurePreferences.removeValue(WALLET_ID);
  }

  public DecentAccount getAccount() {
    return new DecentAccount(getId(), getName(), getPublicKey());
  }

  @Override
  public boolean isInitialized() {
    return privateKey != null && super.isInitialized();
  }
}
