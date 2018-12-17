package com.example.firebasechat;

import android.util.Base64;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Base64.class})
@PowerMockIgnore({"javax.crypto.*" , "java.security.*", "sun.security.*"})

public class UserMethodsTest {

    @Test
    public void setSessionPair_() {
        User new_user = new User();
        String[] sessionPair_ = new String[2];
        sessionPair_[0] = "BzDshr8s56y1OpSL";
        sessionPair_[1] = "IhvjxfgeWpUyexD2";
        new_user.setSessionPair_(sessionPair_);
        assertEquals(sessionPair_[0], new_user.sessionPair_[0]);
        assertEquals(sessionPair_[1], new_user.sessionPair_[1]);
    }

    @Test
    public void KeyExchange() {
        User Admin;
        User User;
        String[] sessionPair_ = new String[2];
        sessionPair_[0] = "BzDshr8s56y1OpSL";
        sessionPair_[1] = "IhvjxfgeWpUyexD2";

        Admin = new User();
        User = new User();
        User.init(0);

        Admin.setSessionPair_(sessionPair_);


        String pub_key = User.getUserPubKeyEncStr();

        String result[] = Admin.DHGenerateAdmin(pub_key);

        User.EncryptSession(result);

        assertEquals(sessionPair_[0], User.sessionPair_[0]);
        assertEquals(sessionPair_[1], User.sessionPair_[1]);
    }

}