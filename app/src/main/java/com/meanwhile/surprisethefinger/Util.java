package com.meanwhile.surprisethefinger;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

/**
 * Created by mengujua on 23/01/16.
 */
public class Util {

    public static final String KEY_NAME = "anonimousKey";

    public PublicKey getPublicKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        PublicKey publicKey =
                keyStore.getCertificate(KEY_NAME).getPublicKey();

        return publicKey;

    }

    public PrivateKey getPrivateKey() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        PublicKey publicKey =
                keyStore.getCertificate(KEY_NAME).getPublicKey();

        keyStore.load(null);
        PrivateKey key = (PrivateKey) keyStore.getKey(KEY_NAME, null);
        return key;
    }

}
