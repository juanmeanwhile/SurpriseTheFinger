package com.meanwhile.surprisethefinger;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.adidas.common.exception.SupernovaException;
import com.adidas.sso.common.activity.EntryPointActivity;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "prefs";
    private static final String PREF_PRIVATE_KEY = "private_key";
    private static final String KEY_NAME = "daKey";
    private String mPrivateKey;
    private String mUserId = "myUsername";
    private AlertDialog mDialog;

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);

        createKeyPair();
        enroll();
    }


    public void onButtonClicked(View view) {
        //Check if there is a fingerprint
        mDialog = new AlertDialog.Builder(this).setTitle(R.string.fingerprint_dialog_title).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, R.string.save_finger_canceled, Toast.LENGTH_LONG).show();
                    }
                }).create();

        //mDialog.show();

        //saveFingerprint();

        goToCart();
    }

    public void createKeyPair() {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            keyPairGenerator.initialize(
                    new KeyGenParameterSpec.Builder(Util.KEY_NAME,
                            KeyProperties.PURPOSE_SIGN)
                            .setDigests(KeyProperties.DIGEST_SHA256)
                            .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                                    // Require the user to authenticate with a fingerprint to authorize
                                    // every use of the private key
                            .setUserAuthenticationRequired(true)
                            .build());
            keyPairGenerator.generateKeyPair();
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    private void enroll() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(Util.KEY_NAME).getPublicKey();
            // Provide the public key to the backend. In most cases, the key needs to be transmitted
            // to the backend over the network, for which Key.getEncoded provides a suitable wire
            // format (X.509 DER-encoded). The backend can then create a PublicKey instance from the
            // X.509 encoded form using KeyFactory.generatePublic. This conversion is also currently
            // needed on API Level 23 (Android M) due to a platform bug which prevents the use of
            // Android Keystore public keys when their private keys require user authentication.
            // This conversion creates a new public key which is not backed by Android Keystore and
            // thus is not affected by the bug.
            KeyFactory factory = KeyFactory.getInstance(publicKey.getAlgorithm());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey.getEncoded());
            PublicKey verificationKey = factory.generatePublic(spec);
            FakeServer.getInstance().enroll("user", verificationKey);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException |
                IOException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public void saveFingerprint(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
            keyPairGenerator.initialize(new KeyGenParameterSpec.Builder(Util.KEY_NAME, KeyProperties.PURPOSE_SIGN).setDigests(KeyProperties.DIGEST_SHA256)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1")).setUserAuthenticationRequired(true).build());
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            //we store our private key
            mPrivateKey = keyPair.getPrivate().toString();

            //we call our server and we enroll out key
            FakeServer.getInstance().enroll(mUserId, keyPair.getPublic());

            //we are done
            goToCart();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    public void goToCart(){
        startActivity(new Intent(this, CartActivity.class));
    }
}
