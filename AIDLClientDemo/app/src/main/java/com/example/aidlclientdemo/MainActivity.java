package com.example.aidlclientdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aidlservicedemo.IAdd;
import com.example.aidlservicedemo.IPlayListener;
import com.example.aidlservicedemo.Person;

import java.util.List;
/**
 * 主界面
 *
 * @author Jin
 * @since 2021/7/1
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private final IPlayListener.Stub mPlayListener =
      new IPlayListener.Stub() {

        @Override
        public void onSuccess(String name, Bundle bundle) {
          Log.i("TAG", "onSuccess: " + name);
          if (bundle != null) {
            Log.i("TAG", "user: " + bundle.getString("user", "蔡依林"));
            Log.i("TAG", "age: " + bundle.getInt("age", 33));
          }
        }
      };
  private IAdd mIAdd;
  private final ServiceConnection serviceConnection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          mIAdd = IAdd.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          mIAdd = null;
        }
      };
  private TextView m1Tv;
  private TextView m2Tv;
  private TextView m3Tv;
  private TextView m4Tv;
  private TextView m5Tv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    if (mIAdd == null) {
      Intent intent = new Intent(IAdd.class.getName());
      intent.setAction("service.calc");
      intent.setPackage("com.example.aidlservicedemo");
      bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }
  }

  private void initView() {
    m1Tv = findViewById(R.id.tv_1);
    m2Tv = findViewById(R.id.tv_2);
    m3Tv = findViewById(R.id.tv_3);
    m4Tv = findViewById(R.id.tv_4);
    m5Tv = findViewById(R.id.tv_5);

    m1Tv.setOnClickListener(this);
    m2Tv.setOnClickListener(this);
    m3Tv.setOnClickListener(this);
    m4Tv.setOnClickListener(this);
    m5Tv.setOnClickListener(this);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(serviceConnection);
  }

  @Override
  public void onClick(View v) {
    if (mIAdd != null) {
      try {
        int id = v.getId();
        if (id == R.id.tv_1) {
          // addNumbers
          int addNumbers = mIAdd.addNumbers(11, 13);
          Log.i("TAG", "addNumbers: " + addNumbers);
        } else if (id == R.id.tv_2) {
          // getStringList
          List<String> stringList = mIAdd.getStringList();
          Log.i("TAG", "StringList: " + stringList.toString());
        } else if (id == R.id.tv_3) {
          // getPersonList
          List<Person> personList = mIAdd.getPersonList();
          for (Person person : personList) {
            Log.i("TAG", "PersonName: " + person.name);
          }
        } else if (id == R.id.tv_4) {
          // placeCall
          mIAdd.placeCall("12454567");
        } else if (id == R.id.tv_5) {
          // placeCall
          mIAdd.involved(mPlayListener);
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }
}
