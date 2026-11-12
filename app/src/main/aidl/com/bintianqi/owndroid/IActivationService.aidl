package com.bintianqi.owndroid;

import android.os.Bundle;

interface IActivationService {
    Bundle execute(String command) = 1;
    Bundle executeWithBypass(String command) = 2;
    void destroy() = 16777114;
}
