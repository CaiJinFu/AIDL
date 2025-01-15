# AIDL

aidl是Android进程间通信的一种方式，下面就介绍一个如何实现客户端与服务端的AIDL。

## 服务端

新建一个service的项目。

### 1.首先创建一个AIDL文件
![image](https://github.com/user-attachments/assets/f2c2deb0-f286-4fe0-b511-5f15c8bc5958)

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/278e346db32e3c07717a5ba98256c71d.png#pic_center)

文件名可以随便取，这里取一个ITest，点击finish之后，就会出现一个aidl的目录。

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/80e8e9e8411c7107bd3c4f3e2a44060f.png#pic_center)

**ITest.aidl**

```java
// ITest.aidl
package com.example.aidlservicedemo;
import com.example.aidlservicedemo.Person;
import com.example.aidlservicedemo.IPlayListener;
// Declare any non-default types here with import statements

interface ITest {
    int addNumbers(int num1,int num2);
    List<String> getStringList();
    List<Person> getPersonList();
    void placeCall(String number);
    void involved(IPlayListener iPlayListener);
}
```

此时还没有IPlayListener与Person，下面来创建一下。注意：在aidl文件里面是不会自动导入的，所以我们要手动导入这些文件。

```java
import com.example.aidlservicedemo.Person;
import com.example.aidlservicedemo.IPlayListener;
```

IPlayListener也是一个aidl文件，所以还需要照着前面的步骤创建一个名字为IPlayListener.aidl。

Person是一个类，这个类要序列化。

```java
package com.example.aidlservicedemo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Person
 * 
 * @author Jin
 * @since 2021/7/1
 */
public class Person implements Parcelable {

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
    public String name;
    public int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person() {}

    protected Person(Parcel in) {
        name = in.readString();
        age = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(age);
    }
}
```

要想在aidl之间传递Person这个对象，还需要照着前面的步骤创建一个名字为Person.aidl。不过这个内容有点特别，创建完之后还需要改一下，具体如下：

```java
// Person.aidl
package com.example.aidlservicedemo;
parcelable Person;
```

### 2.make project

此时点一下make project，就是那个小锤子，编译一下，会生成一些相关aidl的文件。

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/1e54c71a54a8ab793e7844de8bd947bd.png#pic_center)

### 3.编写一个服务类

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/b8128e9eed25b48cda9ab5f2b9efe215.png#pic_center)

名字可以随便取，这里取名为AIDLService。在service清单文件中添加如下信息。

```xml
<service
  android:name=".AIDLService"
  android:enabled="true"
  android:exported="true"
  android:process=":remote"
  >
  <intent-filter>
    <action android:name="service.calc" />
  </intent-filter>
</service>
```

紧接着是对服务类的编写，代码很简单的，不做解释了，具体代码如下：

```java
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
          // 检查权限，没有权限则在通知栏提示
          if (ActivityCompat.checkSelfPermission(
                  getApplicationContext(), Manifest.permission.CALL_PHONE)
              != PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent =
                PendingIntent.getActivity(
                    getApplicationContext(), (int) System.currentTimeMillis(), intent, 0);
            Notification n =
                new Notification.Builder(getApplicationContext())
                    .setContentTitle("AIDL Server App")
                    .setContentText("Please grant call permission from settings")
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(0, n);
            return;
          }
          // 拨打电话
          Intent intent = new Intent(Intent.ACTION_CALL);
          intent.setData(Uri.parse("tel:" + number));
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.addFlags(Intent.FLAG_FROM_BACKGROUND);
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

```

服务端编写完成，运行一下项目。

## 客户端

新建一个client项目。

### 1.创建aidl文件

当创建完服务端的aidl文件后，客服端的就比较简单了，只需要把服务端aidl目录copy过来就好了。

### 2.创建Person类

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/95b99263f23bc512dc08d74a9a4c0260.png#pic_center)

如图所示，包名要跟服务端的一致，否则是无效的。

### 3.make project

![在这里插入图片描述](https://i-blog.csdnimg.cn/blog_migrate/7cf1d801c2cbbb7d73e5bc710c5b3c2b.png#pic_center)

### 4.调用服务端

调用关键代码如下：

```java
Intent intent = new Intent(ITest.class.getName());
intent.setAction("service.calc");
intent.setPackage("com.example.aidlservicedemo");
bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
```

完整的调用代码如下所示：

```java
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
  private ITest iTest;
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
    if (iTest == null) {
      Intent intent = new Intent(ITest.class.getName());
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
    if (iTest != null) {
      try {
        int id = v.getId();
        if (id == R.id.tv_1) {
          // addNumbers
          int addNumbers = iTest.addNumbers(11, 13);
          Log.i("TAG", "addNumbers: " + addNumbers);
        } else if (id == R.id.tv_2) {
          // getStringList
          List<String> stringList = iTest.getStringList();
          Log.i("TAG", "StringList: " + stringList.toString());
        } else if (id == R.id.tv_3) {
          // getPersonList
          List<Person> personList = iTest.getPersonList();
          for (Person person : personList) {
            Log.i("TAG", "PersonName: " + person.name);
          }
        } else if (id == R.id.tv_4) {
          // placeCall
          iTest.placeCall("12454567");
        } else if (id == R.id.tv_5) {
          // placeCall
          iTest.involved(mPlayListener);
        }
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }
}
```

运行项目，至此完成。建议下载项目运行一下。

[https://github.com/CaiJinFu/AIDL](https://github.com/CaiJinFu/AIDL)

[AIDL通信实例](https://blog.csdn.net/Mr_JingFu/article/details/118464663?ops_request_misc=%257B%2522request%255Fid%2522%253A%2522d6ae1789605ace61169d9878801fd4ce%2522%252C%2522scm%2522%253A%252220140713.130102334.pc%255Fblog.%2522%257D&request_id=d6ae1789605ace61169d9878801fd4ce&biz_id=0&utm_medium=distribute.pc_search_result.none-task-blog-2~blog~first_rank_ecpm_v1~rank_v31_ecpm-1-118464663-null-null.nonecase&utm_term=AIDL&spm=1018.2226.3001.4450)
