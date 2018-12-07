package com.example.firebasechat;

import android.util.Base64;
import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;

//import org.apache.commons.codec.binary.Base64;

public class User {
    // все переменные
    public String userPubKeyEncStr;
    public String userPrivateKeyEncStr;
    KeyPair userKpair;
    // AES
    static public String[] sessionPair_ = new String[2];
    static public String[] sessionPair = new String[2];

    public User() {

    }
    void init(int status) {
        if(status == 0) {
            try {
                // ЮЗЕР создаёт ключ 1024 бит
                SecureRandom secureRandom = new SecureRandom();
                KeyPairGenerator userKpairGen = KeyPairGenerator.getInstance("DH");
                userKpairGen.initialize(1024, secureRandom);
                userKpair = userKpairGen.generateKeyPair();
                // записываем ключи
                byte[] userPubKeyEnc = userKpair.getPublic().getEncoded();
                userPubKeyEncStr = Base64.encodeToString(userPubKeyEnc, Base64.DEFAULT);

                byte[] userPrivateKeyEnc = userKpair.getPrivate().getEncoded();
                userPrivateKeyEncStr = Base64.encodeToString(userPrivateKeyEnc, Base64.DEFAULT);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else {}
    }
    public void setSessionPair_(String[] sessionPair_) {
        this.sessionPair_ = sessionPair_;
    }
    public void setSessionPair(String[] sessionPair) {
        this.sessionPair = sessionPair;
    }

    public void EncryptSession(String[] result) {
        try {
            byte[] adminPubKeyEnc = Base64.decode(result[0], Base64.DEFAULT);
            byte[] cipherString1 = Base64.decode(result[1], Base64.DEFAULT);
            byte[] cipherString2 = Base64.decode(result[2], Base64.DEFAULT);
            byte[] encodedParams = Base64.decode(result[3], Base64.DEFAULT);

            // Саня наговнякал, собираем приватный ключ из сохранённой на устройстве строки
            KeyFactory myKeyFac = KeyFactory.getInstance("DH");
            PKCS8EncodedKeySpec myPrivateSpec = new PKCS8EncodedKeySpec(Base64.decode(userPrivateKeyEncStr, Base64.DEFAULT));
            PrivateKey userPrivateKeyEnc = myKeyFac.generatePrivate(myPrivateSpec);

            KeyAgreement userKeyAgree = KeyAgreement.getInstance("DH");
            userKeyAgree.init(userPrivateKeyEnc);
            //

            // получает из байтов ключ АДМИНА и добавляет к общему секрету
            KeyFactory userKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(adminPubKeyEnc);
            PublicKey adminPubKey = userKeyFac.generatePublic(x509KeySpec);

            userKeyAgree.doPhase(adminPubKey, true);
            byte[] userSharedSecret = userKeyAgree.generateSecret();

            // формирует AES ключ
            SecretKeySpec userAesKey = new SecretKeySpec(userSharedSecret, 0, 16, "AES");
            // применяет параметры шифрования и свой AES ключ
            AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
            aesParams.init(encodedParams);
            // создаёт шифр с полученными параметрами шифрования
            Cipher userCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            userCipher.init(Cipher.DECRYPT_MODE, userAesKey, aesParams);

            // расшифровавыет сессионную пару
            sessionPair_[0] = new String(userCipher.doFinal(cipherString1));
            sessionPair_[1] = new String(userCipher.doFinal(cipherString2));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String[] DHGenerateAdmin(String userPubKeyEncStr) {
        try {
            // АДМИН из байтов АЛИСЫ формирует её публичный ключ
            byte[] userPubKeyEnc = Base64.decode(userPubKeyEncStr, Base64.DEFAULT);
            KeyFactory adminKeyFac = KeyFactory.getInstance("DH");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(userPubKeyEnc);
            PublicKey userPubKey = adminKeyFac.generatePublic(x509KeySpec);

            // АДМИН получает параметры ключа АЛИСЫ и на их основе создаёт пару собственных ключей
            DHParameterSpec dhParamFromuserPubKey = ((DHPublicKey) userPubKey).getParams();
            SecureRandom secureRandom = new SecureRandom();
            KeyPairGenerator adminKpairGen = KeyPairGenerator.getInstance("DH");
            adminKpairGen.initialize(dhParamFromuserPubKey, secureRandom);
            KeyPair adminKpair = adminKpairGen.generateKeyPair();

            // АДМИН создаёт DH KeyAgreement объект (приватный ключ) и инвертирует публичный ключ в байты
            KeyAgreement adminKeyAgree = KeyAgreement.getInstance("DH");
            adminKeyAgree.init(adminKpair.getPrivate());
            byte[] adminPubKeyEnc = adminKpair.getPublic().getEncoded();
            // добавляет ключ Алисы к общему секрету
            adminKeyAgree.doPhase(userPubKey, true);
            byte[] adminSharedSecret = adminKeyAgree.generateSecret();

            // формирует AES ключ и шифрует им свой секретный ключ
            SecretKeySpec adminAesKey = new SecretKeySpec(adminSharedSecret, 0, 16, "AES");
            // создаёт шифр, применяет его и параметры шифрования
            Cipher adminCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            adminCipher.init(Cipher.ENCRYPT_MODE, adminAesKey);

            // нужно передать зашифрованный текст и параметры шифрования
            byte[] cipherString1 = adminCipher.doFinal(sessionPair[0].getBytes());
            byte[] cipherString2 = adminCipher.doFinal(sessionPair[1].getBytes());
            byte[] encodedParams = adminCipher.getParameters().getEncoded();

            // перегенерировать байты в текст и передать
            String[] resultString = new String[4];
            resultString[0] = Base64.encodeToString(adminPubKeyEnc, Base64.DEFAULT);
            resultString[1] = Base64.encodeToString(cipherString1, Base64.DEFAULT);
            resultString[2] = Base64.encodeToString(cipherString2, Base64.DEFAULT);
            resultString[3] = Base64.encodeToString(encodedParams, Base64.DEFAULT);

            return resultString;
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
