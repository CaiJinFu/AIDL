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

import com.example.aidlservicedemo.IBinderPool;
import com.example.aidlservicedemo.IPlayListener;
import com.example.aidlservicedemo.ITest;
import com.example.aidlservicedemo.Person;

import java.util.List;
/**
 * 主界面
 *
 * @author Jin
 * @since 2021/7/1
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
  private static final String TAG = "MainActivity";

  private final IPlayListener.Stub mPlayListener =
      new IPlayListener.Stub() {

        @Override
        public void onSuccess(String name, Bundle bundle) {
          Log.i(TAG, "onSuccess: " + name);
          if (bundle != null) {
            Log.i(TAG, "user: " + bundle.getString("user", "蔡依林"));
            Log.i(TAG, "age: " + bundle.getInt("age", 33));
          }
        }
      };
  private ITest iTest; 
  private BinderPool mBinderPool;
  private final ServiceConnection serviceConnection =
      new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
          iTest = ITest.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          iTest = null;
        }
      };
  private TextView mConnectTraditionalTv;
  private TextView m1Tv;
  private TextView m2Tv;
  private TextView m3Tv;
  private TextView m4Tv;
  private TextView m5Tv;
  private TextView m6Tv;
  private TextView m7Tv;
  private TextView m8Tv;
  private TextView m9Tv;
  private TextView m10Tv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initView();
    
    // 应用启动时不自动连接服务，等待用户点击按钮
  }

  private void initView() {
    mConnectTraditionalTv = findViewById(R.id.tv_connect_traditional);
    m1Tv = findViewById(R.id.tv_1);
    m2Tv = findViewById(R.id.tv_2);
    m3Tv = findViewById(R.id.tv_3);
    m4Tv = findViewById(R.id.tv_4);
    m5Tv = findViewById(R.id.tv_5);
    m6Tv = findViewById(R.id.tv_6);
    m7Tv = findViewById(R.id.tv_7);
    m8Tv = findViewById(R.id.tv_8);
    m9Tv = findViewById(R.id.tv_9);
    m10Tv = findViewById(R.id.tv_10);
    
    mConnectTraditionalTv.setOnClickListener(this);
    m1Tv.setOnClickListener(this);
    m2Tv.setOnClickListener(this);
    m3Tv.setOnClickListener(this);
    m4Tv.setOnClickListener(this);
    m5Tv.setOnClickListener(this);
    m6Tv.setOnClickListener(this);
    m7Tv.setOnClickListener(this);
    m8Tv.setOnClickListener(this);
    m9Tv.setOnClickListener(this);
    m10Tv.setOnClickListener(this);
  }
  
  private void setTraditionalButtonsEnabled(boolean enabled) {
    if (m1Tv != null) {
      m1Tv.setEnabled(enabled);
    }
    if (m2Tv != null) {
      m2Tv.setEnabled(enabled);
    }
    if (m3Tv != null) {
      m3Tv.setEnabled(enabled);
    }
    if (m4Tv != null) {
      m4Tv.setEnabled(enabled);
    }
    if (m5Tv != null) {
      m5Tv.setEnabled(enabled);
    }
  }

  private void setBinderPoolButtonsEnabled(boolean enabled) {
    if (m6Tv != null) {
      m6Tv.setEnabled(enabled);
    }
    if (m7Tv != null) {
      m7Tv.setEnabled(enabled);
    }
    if (m8Tv != null) {
      m8Tv.setEnabled(enabled);
    }
    if (m9Tv != null) {
      m9Tv.setEnabled(enabled);
    }
    if (m10Tv != null) {
      m10Tv.setEnabled(enabled);
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mBinderPool != null) {
      mBinderPool.destroy();
    }
    unbindService(serviceConnection);
  }

  @Override
  public void onClick(View v) {
    int id = v.getId();
    
    // 传统服务连接按钮
    if (id == R.id.tv_connect_traditional) {
      if (iTest == null) {
        // 显示连接中状态
        v.setEnabled(false);
        ((TextView) v).setText("连接中...");
        
        Intent intent = new Intent(ITest.class.getName());
        intent.setAction("service.calc");
        intent.setPackage("com.example.aidlservicedemo");
        
        // 异步连接服务
        new Thread(() -> {
          boolean success = bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
          
          // 延迟一段时间等待连接完成
          try {
            Thread.sleep(2000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          
          runOnUiThread(() -> {
            if (success && iTest != null) {
              v.setEnabled(true);
              ((TextView) v).setText("传统服务已连接");
              
              // 启用传统服务按钮
              setTraditionalButtonsEnabled(true);
              Log.i(TAG, "传统服务连接成功");
            } else {
              v.setEnabled(true);
              ((TextView) v).setText("连接失败，点击重试");
              Log.e(TAG, "传统服务连接失败");
            }
          });
        }).start();
      } else {
        Log.i(TAG, "传统服务已连接");
      }
      return;
    }
    
    // 前5个按钮使用传统方式连接的服务
    if (iTest != null && (id == R.id.tv_1 || id == R.id.tv_2 || id == R.id.tv_3 || id == R.id.tv_4 || id == R.id.tv_5)) {
      try {
        if (id == R.id.tv_1) {
          // addNumbers
          int addNumbers = iTest.addNumbers(11, 13);
          Log.i(TAG, "addNumbers: " + addNumbers);
        } else if (id == R.id.tv_2) {
          // getStringList
          List<String> stringList = iTest.getStringList();
          Log.i(TAG, "StringList: " + stringList.toString());
        } else if (id == R.id.tv_3) {
          // getPersonList
          List<Person> personList = iTest.getPersonList();
          for (Person person : personList) {
            Log.i(TAG, "PersonName: " + person.name);
          }
        } else if (id == R.id.tv_4) {
          // placeCall
          iTest.placeCall("12454567");
        } else if (id == R.id.tv_5) {
          // involved
          iTest.involved(mPlayListener);
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
    
    // 后5个按钮使用连接池方式连接的服务
    if (id == R.id.tv_6 || id == R.id.tv_7 || id == R.id.tv_8 || id == R.id.tv_9 || id == R.id.tv_10) {
      // 连接池方式 - 点击时才连接
      if (mBinderPool == null) {
        // 显示连接中状态
        v.setEnabled(false);
        
        new Thread(() -> {
          mBinderPool = BinderPool.getInstance(MainActivity.this);
          
          // 在UI线程中恢复按钮状态
          runOnUiThread(() -> {
            v.setEnabled(true);
            
            // 连接完成后再次调用点击事件
            onClick(v);
          });
        }).start();
        return;
      }
      
      try {
        if (id == R.id.tv_6) {
          // 连接池-加法服务
          IBinder binder = mBinderPool.queryBinder(BinderPool.BINDER_CODE_TEST);
          if (binder != null) {
            ITest testService = ITest.Stub.asInterface(binder);
            int result = testService.addNumbers(20, 30);
            Log.i(TAG, "连接池加法结果: " + result);
          } else {
            Log.e(TAG, "无法获取加法服务");
          }
        } else if (id == R.id.tv_7) {
          // 连接池-字符串服务
          IBinder binder = mBinderPool.queryBinder(BinderPool.BINDER_CODE_TEST);
          if (binder != null) {
            ITest testService = ITest.Stub.asInterface(binder);
            List<String> stringList = testService.getStringList();
            Log.i(TAG, "连接池字符串列表: " + stringList.toString());
          } else {
            Log.e(TAG, "无法获取字符串服务");
          }
        } else if (id == R.id.tv_8) {
          // 连接池-人员服务
          IBinder binder = mBinderPool.queryBinder(BinderPool.BINDER_CODE_TEST);
          if (binder != null) {
            ITest testService = ITest.Stub.asInterface(binder);
            List<Person> personList = testService.getPersonList();
            for (Person person : personList) {
              Log.i(TAG, "连接池人员: " + person.name + " - " + person.age);
            }
          } else {
            Log.e(TAG, "无法获取人员服务");
          }
        } else if (id == R.id.tv_9) {
          // 连接池-回调服务
          IBinder binder = mBinderPool.queryBinder(BinderPool.BINDER_CODE_TEST);
          if (binder != null) {
            ITest testService = ITest.Stub.asInterface(binder);
            testService.involved(mPlayListener);
            Log.i(TAG, "连接池回调服务调用成功");
          } else {
            Log.e(TAG, "无法获取回调服务");
          }
        } else if (id == R.id.tv_10) {
          // 测试连接池状态
          IBinder binder = mBinderPool.queryBinder(BinderPool.BINDER_CODE_TEST);
          if (binder != null) {
            Log.i(TAG, "连接池状态: 服务正常");
            
            // 测试多个服务调用
            ITest testService = ITest.Stub.asInterface(binder);
            int result1 = testService.addNumbers(5, 10);
            List<String> result2 = testService.getStringList();
            
            Log.i(TAG, "多服务测试 - 加法: " + result1);
            Log.i(TAG, "多服务测试 - 字符串: " + result2.size() + " 项");
          } else {
            Log.e(TAG, "连接池状态: 服务异常");
          }
        }
      } catch (RemoteException e) {
        Log.e(TAG, "连接池服务调用异常", e);
      }
    }
  }
}