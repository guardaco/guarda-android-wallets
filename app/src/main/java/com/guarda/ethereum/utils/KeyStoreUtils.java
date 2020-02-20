package com.guarda.ethereum.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import com.guarda.ethereum.BuildConfig;
import com.guarda.ethereum.GuardaApp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.security.auth.x500.X500Principal;

import autodagger.AutoInjector;

import static com.guarda.ethereum.managers.SharedManager.ENCRYPTED_AES;
import static com.guarda.ethereum.managers.SharedManager.GUARDA_SHAR_PREF;

@AutoInjector(GuardaApp.class)
public class KeyStoreUtils {

    private static final String AndroidKeyStore = "AndroidKeyStore";
    private static final String AES_MODE = "AES/GCM/NoPadding";
    private static final String KEY_ALIAS = "GUARDA_KEY_ALIS_V2";
    private static final String XEALTH_KEY_ALIAS = "XEALTH_GUARDA_KEY_ALIS";
    private static final String FIXED_IV = "wvhoZu807WTa";
    private static final String RSA_MODE =  "RSA/ECB/PKCS1Padding";
    private static final String AES_MODE_OLD = "AES/ECB/PKCS7Padding";

    @Inject
    Context context;

    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private byte[] iv;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public KeyStoreUtils() {
        GuardaApp.getAppComponent().inject(this);

        try {
            keyStore = KeyStore.getInstance(AndroidKeyStore);
            keyStore.load(null);

            if (!keyStore.containsAlias(KEY_ALIAS)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);
                    keyGenerator.init(
                            new KeyGenParameterSpec.Builder(KEY_ALIAS,
                                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                                    .setRandomizedEncryptionRequired(false)
                                    .build());
                    keyGenerator.generateKey();
                } else {
                    // Generate a key pair for encryption
                    Calendar start = Calendar.getInstance();
                    Calendar end = Calendar.getInstance();
                    end.add(Calendar.YEAR, 30);
                    KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                            .setAlias(KEY_ALIAS)
                            .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                            .setSerialNumber(BigInteger.TEN)
                            .setStartDate(start.getTime())
                            .setEndDate(end.getTime())
                            .build();
//                    KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
                    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", AndroidKeyStore);
                    kpg.initialize(spec);
                    kpg.generateKeyPair();
                }
            }

            //Generate and Store AES
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                generateAndStoreAES();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String encryptData(String data) {
        if (BuildConfig.DEBUG) {
//            Log.d("psd", "KeyStoreUtils.encryptData() - data = " + data);
        }
        try {
            final Cipher c = Cipher.getInstance(AES_MODE);
            final GCMParameterSpec spec = new GCMParameterSpec(128, FIXED_IV.getBytes());
            c.init(Cipher.ENCRYPT_MODE, getSecretKey(), spec);
            byte[] encodedBytes = c.doFinal(data.getBytes("UTF-8"));
//            Log.d("psd", "KeyStoreUtils.encryptData() - return = " + Base64.encodeToString(encodedBytes, Base64.DEFAULT) + " data = " + data);
            return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public String decryptData(String data) {
        try {
            final Cipher c = Cipher.getInstance(AES_MODE);
            final GCMParameterSpec spec = new GCMParameterSpec(128, FIXED_IV.getBytes());
            c.init(Cipher.DECRYPT_MODE, getSecretKey(), spec);
            byte[] decodedBytes = c.doFinal(Base64.decode(data, Base64.DEFAULT));
//            Log.d("psd", "KeyStoreUtils.decryptData() - return = " + new String(decodedBytes, "UTF-8") + " data = " + data);
            return new String(decodedBytes, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey generateKey() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {
        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, AndroidKeyStore);

        keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setRandomizedEncryptionRequired(false)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return keyGenerator.generateKey();
    }

    private SecretKey getSecretKey() throws NoSuchAlgorithmException,
            UnrecoverableEntryException, KeyStoreException {
        return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
    }


    //Supporting API < 23


    private void generateOldKeyPair() throws KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        // Generate the RSA key pairs
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            // Generate a key pair for encryption
            Calendar start = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            end.add(Calendar.YEAR, 30);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(KEY_ALIAS)
                    .setSubject(new X500Principal("CN=" + KEY_ALIAS))
                    .setSerialNumber(BigInteger.TEN)
                    .setStartDate(start.getTime())
                    .setEndDate(end.getTime())
                    .build();
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, AndroidKeyStore);
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", AndroidKeyStore);
            kpg.initialize(spec);
            kpg.generateKeyPair();
        }
    }

    private byte[] rsaEncrypt(byte[] secret) throws NoSuchAlgorithmException, UnrecoverableEntryException,
            KeyStoreException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, IOException {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        // Encrypt the text
        Cipher inputCipher = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        inputCipher.init(Cipher.ENCRYPT_MODE, privateKeyEntry.getCertificate().getPublicKey());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, inputCipher);
        cipherOutputStream.write(secret);
        cipherOutputStream.close();

        byte[] vals = outputStream.toByteArray();
        return vals;
    }

    private  byte[]  rsaDecrypt(byte[] encrypted) throws Exception {
        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
        Cipher output = Cipher.getInstance(RSA_MODE, "AndroidOpenSSL");
        output.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());
        CipherInputStream cipherInputStream = new CipherInputStream(
                new ByteArrayInputStream(encrypted), output);
        ArrayList<Byte> values = new ArrayList<>();
        int nextByte;
        while ((nextByte = cipherInputStream.read()) != -1) {
            values.add((byte)nextByte);
        }

        byte[] bytes = new byte[values.size()];
        for(int i = 0; i < bytes.length; i++) {
            bytes[i] = values.get(i).byteValue();
        }
        return bytes;
    }

    private void generateAndStoreAES() throws Exception {
        Log.d("psd", "generateAndStoreAES started");
        SharedPreferences pref = context.getSharedPreferences(GUARDA_SHAR_PREF, Context.MODE_PRIVATE);
        String enryptedKeyB64 = pref.getString(ENCRYPTED_AES, null);
        if (enryptedKeyB64 == null) {
            byte[] key = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(key);
            byte[] encryptedKey = rsaEncrypt(key);
            enryptedKeyB64 = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(ENCRYPTED_AES, enryptedKeyB64);
            edit.apply();
            Log.d("psd", "generateAndStoreAES generated");
        }
        Log.d("psd", "generateAndStoreAES not null");
    }

    private Key getOldSecretKey() throws Exception {
        SharedPreferences pref = context.getSharedPreferences(GUARDA_SHAR_PREF, Context.MODE_PRIVATE);
        String enryptedKeyB64 = pref.getString(ENCRYPTED_AES, null);
        // need to check null, omitted here
        byte[] encryptedKey = Base64.decode(enryptedKeyB64, Base64.DEFAULT);
        byte[] key = rsaDecrypt(encryptedKey);
        return new SecretKeySpec(key, "AES");
    }

    public String encryptOld(String input) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE_OLD, "BC");
        c.init(Cipher.ENCRYPT_MODE, getOldSecretKey());
        byte[] encodedBytes = c.doFinal(input.getBytes("UTF-8"));
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }


    public String decryptOld(String encrypted) throws Exception {
        Cipher c = Cipher.getInstance(AES_MODE_OLD, "BC");
        c.init(Cipher.DECRYPT_MODE, getOldSecretKey());
        byte[] decodedBytes = c.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
        return new String(decodedBytes, "UTF-8");
    }
}
