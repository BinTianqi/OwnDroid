package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.Keep;

@Keep
public interface IPackageInstaller extends IInterface {
    @Keep
    abstract class Stub extends Binder implements IPackageInstaller {
        public static IPackageInstaller asInterface(IBinder obj) {
            throw new UnsupportedOperationException();
        }
    }
}