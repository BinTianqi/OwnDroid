package com.binbin.androidowner;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import java.util.List;

public class Test {
    public static List<UserHandle> returnUsers(Context myContext){
        UserManager userManager = (UserManager) myContext.getSystemService(Context.USER_SERVICE);
        return userManager.getUserProfiles();
    }
}
