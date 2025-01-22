package com.bintianqi.owndroid;

import android.accounts.Account;

interface IUserService {
    String execute(String command) = 1;
    int getUid() = 2;
    Account[] listAccounts() = 3;
}
