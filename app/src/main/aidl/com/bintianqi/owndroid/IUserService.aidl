package com.bintianqi.owndroid;

interface IUserService {
    String execute(String command) = 1;
    int getUid() = 2;
}
