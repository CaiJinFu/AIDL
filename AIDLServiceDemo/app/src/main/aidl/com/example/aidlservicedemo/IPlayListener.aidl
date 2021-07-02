// IPlayListener.aidl
package com.example.aidlservicedemo;

// Declare any non-default types here with import statements
import android.os.Bundle;

interface IPlayListener {
   void onSuccess(String name,inout Bundle bundle);
}