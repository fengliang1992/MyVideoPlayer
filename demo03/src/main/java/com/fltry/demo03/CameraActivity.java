package com.fltry.demo03;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;

import com.fltry.demo03.databinding.ActivityCameraBinding;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraActivity extends AppCompatActivity {
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    ActivityCameraBinding dataBinding;

    private CameraManager manager;
    private Handler childHandler, mainHandler;
    private CameraDevice mCamera;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private ImageReader mImageReader;
    // 创建拍照需要的CaptureRequest.Builder
    private CaptureRequest.Builder captureRequestBuilder;
    private SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        checkPermissions();

        init();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void init() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();
        mainHandler = new Handler(getMainLooper());
        childHandler = new Handler(handlerThread.getLooper());
        holder = dataBinding.cameraSv.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(new SurfaceHolder.Callback() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //获取可用相机设备列表
                //打开相机
                try {
                    String[] CameraIdList = manager.getCameraIdList();
                    if (ActivityCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    manager.openCamera(CameraIdList[0], mCameraDeviceStateCallback, mainHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        //设置照片的大小
        mImageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(CameraActivity.this),
                ScreenUtils.dip2px(CameraActivity.this, 500), ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(imageReader -> {
            // 拿到拍照照片数据
            Image image = imageReader.acquireNextImage();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);//由缓冲区存入字节数组
            image.close();
            //saveBitmap(bytes);//保存照片的处理
        }, mainHandler);

        dataBinding.cameraBtn.setOnClickListener(v -> {
            startActivity(new Intent(CameraActivity.this, CameraActivity2.class));
        });
    }

    /**
     * 摄像头创建监听
     */
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(CameraDevice camera) {//打开摄像头
            try {
                //开启预览
                mCamera = camera;
                startPreview(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(CameraDevice camera) {
            //关闭摄像头
            if (mCamera != null) {
                mCamera.close();
                mCamera = null;
            }
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            //发生错误
        }
    };

    //开始预览，主要是camera.createCaptureSession这段代码很重要，创建会话
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startPreview(final CameraDevice camera) throws CameraAccessException {
        try {
            // 创建预览需要的CaptureRequest.Builder
            mPreviewBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // 将SurfaceView的surface作为CaptureRequest.Builder的目标
            mPreviewBuilder.addTarget(holder.getSurface());
//            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            //设置拍摄图像时相机设备是否使用光学防抖（OIS）。
            mPreviewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            //感光灵敏度
            mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, 1600);
            //曝光补偿//
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            // 创建CameraCaptureSession，该对象负责管理处理预览请求和拍照请求
            camera.createCaptureSession(Arrays.asList(holder.getSurface(), mImageReader.getSurface()), mSessionStateCallback, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 会话状态回调
     */
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            if (mCamera != null && captureRequestBuilder == null) {
                try {
                    captureRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                    // 将imageReader的surface作为CaptureRequest.Builder的目标
                    captureRequestBuilder.addTarget(mImageReader.getSurface());
                    //关闭自动对焦
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                    //设置拍摄图像时相机设备是否使用光学防抖（OIS）。
                    captureRequestBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE, CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
//                    captureRequestBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, valueISO);
                    //曝光补偿//
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
            try {
                updatePreview(session);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

    /**
     * 更新会话，开启预览
     *
     * @param session
     * @throws CameraAccessException
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview(CameraCaptureSession session) throws CameraAccessException {
        session.setRepeatingRequest(mPreviewBuilder.build(), mCaptureCallback, childHandler);
    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            //需要连拍时，循环保存图片就可以了
        }
    };

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, 1000);
        }
    }
}
