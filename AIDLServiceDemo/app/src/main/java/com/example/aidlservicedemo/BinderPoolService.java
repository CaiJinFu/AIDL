package com.example.aidlservicedemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Binder连接池服务
 * 用于管理多个Binder服务的连接
 */
public class BinderPoolService extends Service {
    private static final String TAG = "BinderPoolService";
    
    // Binder服务代码定义
    public static final int BINDER_CODE_TEST = 1;
    public static final int BINDER_CODE_PLAY = 2;
    public static final int BINDER_CODE_CALC = 3;
    
    private final AtomicBoolean mServiceDestroyed = new AtomicBoolean(false);
    private final SparseArray<IBinder> mBinderArray = new SparseArray<>();
    
    private final IBinderPool.Stub mBinderPool = new IBinderPool.Stub() {
        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException {
            Log.d(TAG, "queryBinder: code = " + binderCode);
            
            synchronized (mBinderArray) {
                IBinder binder = mBinderArray.get(binderCode);
                if (binder != null) {
                    return binder;
                }
                
                // 如果不存在，尝试创建对应的Binder服务
                binder = createBinderByCode(binderCode);
                if (binder != null) {
                    mBinderArray.put(binderCode, binder);
                }
                return binder;
            }
        }
        
        @Override
        public void addBinderService(int binderCode, IBinder binder) throws RemoteException {
            Log.d(TAG, "addBinderService: code = " + binderCode);
            
            synchronized (mBinderArray) {
                if (binder != null) {
                    mBinderArray.put(binderCode, binder);
                }
            }
        }
        
        @Override
        public void removeBinderService(int binderCode) throws RemoteException {
            Log.d(TAG, "removeBinderService: code = " + binderCode);
            
            synchronized (mBinderArray) {
                mBinderArray.remove(binderCode);
            }
        }
        
        @Override
        public boolean hasBinderService(int binderCode) throws RemoteException {
            synchronized (mBinderArray) {
                return mBinderArray.get(binderCode) != null;
            }
        }
    };
    
    private IBinder createBinderByCode(int binderCode) {
        Log.d(TAG, "createBinderByCode: code = " + binderCode);
        
        switch (binderCode) {
            case BINDER_CODE_TEST:
                return new ITest.Stub() {
                    @Override
                    public int addNumbers(int num1, int num2) throws RemoteException {
                        Log.i("TAG", "addNumbers() called with: num1 = [" + num1 + "], num2 = [" + num2 + "]");
                        return num1 + num2;
                    }
                    
                    @Override
                    public java.util.List<String> getStringList() throws RemoteException {
                        return AIDLService.getList();
                    }
                    
                    @Override
                    public java.util.List<Person> getPersonList() throws RemoteException {
                        return new AIDLService().getPersons();
                    }
                    
                    @Override
                    public void placeCall(String number) throws RemoteException {
                        // 简化实现，实际项目中需要处理权限等
                        Log.d(TAG, "placeCall: " + number);
                    }
                    
                    @Override
                    public void involved(IPlayListener iPlayListener) throws RemoteException {
                        if (iPlayListener != null) {
                            android.os.Bundle bundle = new android.os.Bundle();
                            bundle.putString("user", "周杰伦");
                            bundle.putInt("age", 55);
                            iPlayListener.onSuccess("你好", bundle);
                        }
                    }
                };
                
            case BINDER_CODE_PLAY:
                return new IPlayListener.Stub() {
                    @Override
                    public void onSuccess(String name, android.os.Bundle bundle) throws RemoteException {
                        Log.d(TAG, "onSuccess: " + name + ", bundle: " + bundle);
                    }
                };
                
            default:
                Log.w(TAG, "Unknown binder code: " + binderCode);
                return null;
        }
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BinderPoolService onCreate");
        mServiceDestroyed.set(false);
        
        // 预加载一些常用的Binder服务
        synchronized (mBinderArray) {
            mBinderArray.put(BINDER_CODE_TEST, createBinderByCode(BINDER_CODE_TEST));
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "BinderPoolService onBind");
        return mBinderPool;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "BinderPoolService onUnbind");
        return super.onUnbind(intent);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BinderPoolService onDestroy");
        mServiceDestroyed.set(true);
        
        synchronized (mBinderArray) {
            mBinderArray.clear();
        }
    }
}