// IBinderPool.aidl
package com.example.aidlservicedemo;

interface IBinderPool {
    // 根据Binder代码获取对应的Binder对象
    IBinder queryBinder(int binderCode);
    
    // 添加Binder服务到连接池
    void addBinderService(int binderCode, IBinder binder);
    
    // 移除Binder服务
    void removeBinderService(int binderCode);
    
    // 检查Binder服务是否存在
    boolean hasBinderService(int binderCode);
}