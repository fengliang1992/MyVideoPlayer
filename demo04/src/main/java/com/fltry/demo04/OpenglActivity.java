package com.fltry.demo04;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OpenglActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyGLSurfaceView myGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(myGLSurfaceView);
    }
}
