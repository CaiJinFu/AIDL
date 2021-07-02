package com.example.aidlservicedemo;

import java.util.List;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

/**
 * AIDLServiceDemo
 * 
 * @author Jin
 * @since 2021/7/1
 */
public class AddService extends Service {

    public IAdd.Stub mBinder = new IAdd.Stub() {
        @Override
        public int addNumbers(int num1, int num2) throws RemoteException {
            Log.d("TAG", "addNumbers() called with: num1 = [" + num1 + "], num2 = [" + num2 + "]");
            return num1 + num2;
        }

        @Override
        public List<String> getStringList() throws RemoteException {
            return MainActivity.getList();
        }

        @Override
        public List<Person> getPersonList() throws RemoteException {
            return MainActivity.getPersons();
        }

        @Override
        public void placeCall(String number) throws RemoteException {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
            if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                // use System.currentTimeMillis() to have a unique ID for the pending intent
                PendingIntent pIntent =
                    PendingIntent.getActivity(getApplicationContext(), (int)System.currentTimeMillis(), intent1, 0);

                // build notification
                // the addAction re-use the same intent to keep the example short
                Notification n = new Notification.Builder(getApplicationContext()).setContentTitle("AIDL Server App")
                    .setContentText("Please grant call permission from settings").setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent).setAutoCancel(true).build();

                NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(0, n);
                return;
            }
            startActivity(intent);
        }

        @Override
        public void involved(IPlayListener iPlayListener) throws RemoteException {
            if (iPlayListener != null) {
                Bundle bundle = new Bundle();
                bundle.putString("user", "周杰伦");
                bundle.putInt("age", 55);
                iPlayListener.onSuccess("你好", bundle);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
