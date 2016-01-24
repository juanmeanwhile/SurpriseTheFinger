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

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "prefs";
    private static final String PREF_PRIVATE_KEY = "private_key";
    private String mPrivateKey;
    private String mUserId = "myUsername";
    private AlertDialog mDialog;

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.button);
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

        saveFingerprint();
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
