package android.app.admin;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

public interface IDevicePolicyManager extends IInterface {
    abstract class Stub extends Binder implements IDevicePolicyManager {
        public static IDevicePolicyManager asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}
