package com.meanwhile.surprisethefinger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by mengujua on 23/01/16.
 */
public class Transaction implements Serializable {
    private String mUser;
    private long mNonce;
    String mProduct;

    public Transaction (String user, long nonce, String productId) {
        mUser = user;
        mNonce = nonce;
        mProduct = productId;
    }

    public String getUserId() {
        return mUser;
    }

    public byte[] toByteArray() throws IOException {
        return serialize(this);
    }


    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    public Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

}
