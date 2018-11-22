package com.example.firebasechat;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import android.util.Base64;

//import org.apache.commons.codec.binary.Base64;

public class User {


    private SecureRandom secureRandom_;
    static private int counter_ = 0;
    static private User mainAbonent_;
    KeyPair pg;

    // AES
    static private String[] sessionPair_ = new String[2];

    public User() {
        try {
                // RSA key generation
                secureRandom_ = new SecureRandom(); // mb insert byte;
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance( "RSA" );
                keyGen.initialize(2048, secureRandom_);
                pg = keyGen.genKeyPair();
                Cipher cipher = Cipher.getInstance( "RSA" );
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
     public String get_public() {
         return pg.getPublic().toString();
     }

    public void setSessionPair_(String[] sessionPair_) {
        this.sessionPair_ = sessionPair_;
    }

    private void getSession(KeyPair pg, Cipher cipher) {
        try {
            for(int i = 0; i < 2; ++i) {
                sessionPair_[i] = new String(
                        decryptRSA(
                                mainAbonent_.encryptRSA(i, pg.getPublic(), cipher),
                                pg.getPrivate(),
                                cipher
                        )
                );
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    // RSA methods
    private byte[] encryptRSA(int elem, PublicKey openKey, Cipher cipher){
        try {
            cipher.init( Cipher.ENCRYPT_MODE, openKey );
            return cipher.doFinal( sessionPair_[elem].getBytes("UTF-8") ); // or toString()
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String decryptRSA(byte[] secretMessage, PrivateKey secretKey, Cipher cipher){
        try {
            cipher.init( Cipher.DECRYPT_MODE, secretKey );
            return new String(cipher.doFinal(secretMessage), "UTF-8");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // AES methods
    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(sessionPair_[1].getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(sessionPair_[0].getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = Base64.encode(cipher.doFinal(value.getBytes()), Base64.DEFAULT);
            return new String(encrypted, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(sessionPair_[1].getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(sessionPair_[0].getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));
            return new String(original, "UTF-8");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
