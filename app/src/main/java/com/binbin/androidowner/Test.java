package com.binbin.androidowner;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Test {
    public static void installPackage(Context context, InputStream inputStream)
            throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        int sessionId = packageInstaller.createSession(new PackageInstaller
                .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        long sizeBytes = 0;

        OutputStream out;
        out = session.openWrite("my_app_session", 0, sizeBytes);

        int total = 0;
        byte[] buffer = new byte[65536];
        int c;
        while ((c = inputStream.read(buffer)) != -1) {
            total += c;
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        inputStream.close();
        out.close();

        // fake intent
        IntentSender statusReceiver = null;
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                1337111117, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        session.commit(pendingIntent.getIntentSender());
        session.close();
    }
}
