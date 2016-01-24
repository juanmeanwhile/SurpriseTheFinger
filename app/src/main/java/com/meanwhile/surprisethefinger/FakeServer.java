package com.meanwhile.surprisethefinger;

import android.content.Context;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.HashMap;

/**
 * Created by mengujua on 23/01/16.
 */
public class FakeServer {

    private HashMap<String, PublicKey> mKeyMap;
    private static FakeServer mInstance;

    public FakeServer(){
        mKeyMap = new HashMap<String, PublicKey>();
    }


    public static FakeServer getInstance() {
            if (mInstance == null) {
                mInstance = new FakeServer();
            }
        return mInstance;
    }

    boolean enroll(String userId, PublicKey publicKey){
        mKeyMap.put(userId, publicKey);
        return true;
    }

    public boolean verify(Transaction transaction, byte[] transactionSignature) {
        try {
            //if (mReceivedTransactions.contains(transaction)) {
                // It verifies the equality of the transaction including the client nonce
                // So attackers can't do replay attacks.
            //    return false;
            //}

            //mReceivedTransactions.add(transaction);
            PublicKey publicKey = mKeyMap.get(transaction.getUserId());
            Signature verificationFunction = Signature.getInstance("SHA256withECDSA");
            verificationFunction.initVerify(publicKey);
            verificationFunction.update(transaction.toByteArray());
            if (verificationFunction.verify(transactionSignature)) {
                // Transaction is verified with the public key associated with the user
                // Do some post purchase processing in the server
                return true;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            // In a real world, better to send some error message to the user
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
