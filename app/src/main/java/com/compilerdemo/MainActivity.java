package com.compilerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import org.annotation.EchoEnable;
import org.annotation.Event;
import org.annotation.ViewById;
import org.api.ViewUtils;

public class MainActivity extends AppCompatActivity {


    //click1
    @ViewById(R.id.click1)
    public Button click1;
    //click2
    @ViewById(R.id.click2)
    public Button click2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewUtils.bindActivity(this);
    }


    @Event({R.id.click1, R.id.click2})
    @EchoEnable(1000)
    public void click1Click(Button click1) {
        Toast.makeText(this, "click1", Toast.LENGTH_SHORT).show();
    }

    @Event(R.id.click2)
    public void click2Click(Button click2) {
        Toast.makeText(
                this,
                "click2",
                Toast.LENGTH_SHORT
        ).show();
    }
}
