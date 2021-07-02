// IAdd.aidl
package com.example.aidlservicedemo;
import com.example.aidlservicedemo.Person;
import com.example.aidlservicedemo.IPlayListener;
// Declare any non-default types here with import statements

interface IAdd {
    int addNumbers(int num1,int num2);
    List<String> getStringList();
    List<Person> getPersonList();
    void placeCall(String number);
    void involved(IPlayListener iPlayListener);
}
