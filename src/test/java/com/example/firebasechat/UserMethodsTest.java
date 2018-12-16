package com.example.firebasechat;

import android.util.Base64;

import org.junit.Assert;
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
        sessionPair_[0] = "GzDshr7s34y1BrSL";
        sessionPair_[1] = "NMHjxlleWpApdxD2";
        new_user.setSessionPair_(sessionPair_);
        assertEquals(sessionPair_[0], new_user.sessionPair_[0]);
        assertEquals(sessionPair_[1], new_user.sessionPair_[1]);
    }

    /*
    @Test
    public void encrypt() {
        PowerMockito.mockStatic(Base64.class);
        //when(Base64.encode(argument)).thenReturn("expected result");
        //when(Base64.decode(argument)).thenReturn("expected result");
        String msg = "Hello";
        User new_user = new User();
        String[] sessionPair_ = new String[2];
        sessionPair_[0] = "GzDshr7s34y1BrSL";
        sessionPair_[1] = "NMHjxlleWpApdxD2";
        new_user.setSessionPair_(sessionPair_);
        String encmsg = new_user.encrypt(msg);
        encmsg = new_user.decrypt(encmsg);
        assertEquals(encmsg, msg);
    }
    */
}