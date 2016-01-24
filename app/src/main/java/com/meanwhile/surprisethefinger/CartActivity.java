package com.meanwhile.surprisethefinger;

import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.design.widget.Snackbar;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompatApi23;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompatApi23.AuthenticationCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class CartActivity extends AppCompatActivity  {

    private Button mButton;
    private AlertDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mButton = (Button) findViewById(R.id.button);
    }

    public void onBuyClick(View view) {
        //Check if there is a fingerprint
        mDialog = new AlertDialog.Builder(this).setTitle(R.string.fingerprint_dialog_title).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                Toast.makeText(CartActivity.this, R.string.save_finger_canceled, Toast.LENGTH_LONG).show();
            }
        }).create();
        mDialog.show();

        try {
            Signature signature = Signature.getInstance("SHA256withECDSA");
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PrivateKey key = (PrivateKey) keyStore.getKey(Util.KEY_NAME, null);
            signature.initSign(key);
            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(signature);

            CancellationSignal cancellationSignal = new CancellationSignal();
            FingerprintManager fingerprintManager = getSystemService(FingerprintManager.class);
            fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    mDialog.dismiss();
                    Toast.makeText(getBaseContext(), "authentication error:" + errString.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    super.onAuthenticationHelp(helpCode, helpString);
                }

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    mDialog.dismiss();
                    Toast.makeText(getBaseContext(), "FIngerprint correct, sending transaction to the server", Toast.LENGTH_SHORT).show();
                    sendCartToServer(result.getCryptoObject());
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            }, null);

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private void sendCartToServer( FingerprintManager.CryptoObject cryptoObject) {
        Signature signature = cryptoObject.getSignature();
    // Include a client nonce in the transaction so that the nonce is also signed
    // by the private key and the backend can verify that the same nonce can't be used
    // to prevent replay attacks.
        Transaction transaction = new Transaction("myUsername", new SecureRandom().nextLong(), "turret");
        try {
            signature.update(transaction.toByteArray());

            byte[] sigBytes = signature.sign();
            // Send the transaction and signedTransaction to the dummy backend
            if (FakeServer.getInstance().verify(transaction, sigBytes)) {
                onPurchased(transaction);

            } else {
                Toast.makeText(this, R.string.error_purchasing, Toast.LENGTH_SHORT).show();
            }
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPurchased(Transaction transaction) {
        Toast.makeText(this,getString( R.string.success, transaction.mProduct), Toast.LENGTH_LONG).show();
    }

}
