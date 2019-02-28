package com.example.qhhbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.qhheventbus.QhhEventBusHelper;
import com.example.qhheventbus.listener.IEventListener;

public class Main2Activity extends AppCompatActivity implements IEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        QhhEventBusHelper.getInstance().register(this);
    }

    @Override
    public void onMessage(Object object) {

    }
}
