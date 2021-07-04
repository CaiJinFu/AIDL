package com.example.aidlservicedemo;

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

import java.util.ArrayList;
import java.util.List;

/**
 * AIDLServiceDemo
 *
 * @author Jin
 * @since 2021/7/1
 */
public class AIDLService extends Service {

  public ITest.Stub mBinder =
      new ITest.Stub() {
        @Override
        public int addNumbers(int num1, int num2) {
          Log.i("TAG", "addNumbers() called with: num1 = [" + num1 + "], num2 = [" + num2 + "]");
          return num1 + num2;
        }

        @Override
        public List<String> getStringList() {
          return getList();
        }

        @Override
        public List<Person> getPersonList() {
          return getPersons();
        }

        @Override
        public void placeCall(String number) {
          Intent intent = new Intent(Intent.ACTION_CALL);
          intent.setData(Uri.parse("tel:" + number));
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
          if (ActivityCompat.checkSelfPermission(
                  getApplicationContext(), Manifest.permission.CALL_PHONE)
              != PackageManager.PERMISSION_GRANTED) {

            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            // use System.currentTimeMillis() to have a unique ID for the pending intent
            PendingIntent pIntent =
                PendingIntent.getActivity(
                    getApplicationContext(), (int) System.currentTimeMillis(), intent1, 0);

            // build notification
            // the addAction re-use the same intent to keep the example short
            Notification n =
                new Notification.Builder(getApplicationContext())
                    .setContentTitle("AIDL Server App")
                    .setContentText("Please grant call permission from settings")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

  public static List<String> getList() {
    List<String> country = new ArrayList<>();
    country.add("India");
    country.add("Bhutan");
    country.add("Nepal");
    country.add("USA");
    country.add("Canada");
    country.add("China");
    return country;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  public List<Person> getPersons() {
    List<Person> person = new ArrayList<>();
    person.add(new Person("A", 10));
    person.add(new Person("B", 20));
    person.add(new Person("C", 30));
    person.add(new Person("D", 40));
    person.add(new Person("E", 50));
    person.add(new Person("F", 60));
    return person;
  }
}
