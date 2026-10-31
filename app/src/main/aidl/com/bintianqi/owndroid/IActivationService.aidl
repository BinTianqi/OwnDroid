package com.bintianqi.owndroid;

import android.os.Bundle;

interface IActivationService {
    Bundle execute(String command) = 1;
    Bundle activateDeviceOwner() = 2;
    void destroy() = 16777114;
}
