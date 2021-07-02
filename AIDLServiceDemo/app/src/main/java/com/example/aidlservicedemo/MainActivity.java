package com.example.aidlservicedemo;

import java.util.ArrayList;
import java.util.List;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import android.Manifest;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 主界面
 * 
 * @author Jin
 * @since 2021/7/1
 */
public class MainActivity extends AppCompatActivity {
    private static List<String> country;
    private static List<Person> person;

    public static List<String> getList() {
        country = new ArrayList<>();
        country.add("India");
        country.add("Bhutan");
        country.add("Nepal");
        country.add("USA");
        country.add("Canada");
        country.add("China");
        return country;
    }

    public static List<Person> getPersons() {
        person = new ArrayList<>();
        person.add(new Person("A", 10));
        person.add(new Person("B", 20));
        person.add(new Person("C", 30));
        person.add(new Person("D", 40));
        person.add(new Person("E", 50));
        person.add(new Person("F", 60));
        return person;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withActivity(this).withPermission(Manifest.permission.CALL_PHONE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {}

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {}

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }
}
