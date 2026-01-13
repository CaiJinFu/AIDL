package com.example.aidlclientdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.aidlservicedemo.IBinderPool;

import java.util.concurrent.CountDownLatch;

/**
 * Binder连接池客户端
 * 用于管理Binder服务的连接和获取
 */
public class BinderPool {
    private static final String TAG = "BinderPool";
    
    public static final int BINDER_CODE_TEST = 1;
    public static final int BINDER_CODE_PLAY = 2;
    public static final int BINDER_CODE_CALC = 3;
    
    private static volatile BinderPool sInstance;
    private final Context mContext;
    private IBinderPool mBinderPool;
    private CountDownLatch mConnectBinderPoolCountDownLatch;
    
    private BinderPool(Context context) {
        mContext = context.getApplicationContext();
        connectBinderPoolService();
    }
    
    public static BinderPool getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BinderPool.class) {
                if (sInstance == null) {
                    sInstance = new BinderPool(context);
                }
            }
        }
        return sInstance;
    }
    
    private synchronized void connectBinderPoolService() {
        mConnectBinderPoolCountDownLatch = new CountDownLatch(1);
        
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.aidlservicedemo", 
                "com.example.aidlservicedemo.BinderPoolService"));
        
        boolean success = mContext.bindService(intent, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
        
        // 异步连接，不阻塞UI线程
        if (success) {
            new Thread(() -> {
                try {
                    // 设置超时时间，避免无限等待
                    boolean connected = mConnectBinderPoolCountDownLatch.await(5, java.util.concurrent.TimeUnit.SECONDS);
                    if (!connected) {
                        Log.w(TAG, "BinderPool连接超时");
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "connectBinderPoolService interrupted", e);
                }
            }).start();
        } else {
            Log.e(TAG, "bindService failed");
        }
    }
    
    private final ServiceConnection mBinderPoolConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinderPool = IBinderPool.Stub.asInterface(service);
            try {
                // 设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "linkToDeath failed", e);
            }
            mConnectBinderPoolCountDownLatch.countDown();
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w(TAG, "BinderPool service disconnected");
            mBinderPool = null;
        }
    };
    
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.w(TAG, "BinderPool service died");
            if (mBinderPool != null) {
                mBinderPool.asBinder().unlinkToDeath(this, 0);
            }
            mBinderPool = null;
            // 重新连接
            connectBinderPoolService();
        }
    };
    
    /**
     * 查询Binder服务
     */
    public IBinder queryBinder(int binderCode) {
        IBinder binder = null;
        try {
            if (mBinderPool != null) {
                binder = mBinderPool.queryBinder(binderCode);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "queryBinder failed", e);
        }
        return binder;
    }
    
    /**
     * 销毁连接池
     */
    public void destroy() {
        if (mBinderPool != null && mBinderPool.asBinder().isBinderAlive()) {
            mBinderPool.asBinder().unlinkToDeath(mDeathRecipient, 0);
        }
        mContext.unbindService(mBinderPoolConnection);
        sInstance = null;
    }
}