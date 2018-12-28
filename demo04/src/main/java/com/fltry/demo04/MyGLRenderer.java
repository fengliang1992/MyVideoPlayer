package com.fltry.demo04;


import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle triangle;

    public void onDrawFrame(GL10 unused) {
        triangle.draw( new float[16]);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        triangle = new Triangle();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
    }
}