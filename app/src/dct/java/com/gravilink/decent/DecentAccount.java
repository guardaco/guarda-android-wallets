package com.gravilink.decent;

public class DecentAccount {
  private String name;
  private String publicKey;
  private String id;

  public DecentAccount() {
  }

  public DecentAccount(String id, String name, String publicKey) {
    this.id = id;
    this.name = name;
    this.publicKey = publicKey;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPublicKey(String publicKey) {
    this.publicKey = publicKey;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public String getId() {
    return id;
  }

  public void clear() {
    this.id = null;
    this.name = null;
    this.publicKey = null;
  }

  public boolean isInitialized() {
    return name != null && publicKey != null && id != null;
  }
}
