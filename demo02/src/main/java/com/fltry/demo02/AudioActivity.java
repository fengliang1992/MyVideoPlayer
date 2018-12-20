package com.fltry.demo02;

import android.Manifest;
import android.databinding.DataBindingUtil;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fltry.demo02.databinding.ActivityAudioBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fltry.demo02.GlobalConfig.AUDIO_FORMAT;
import static com.fltry.demo02.GlobalConfig.AUDIO_SOURCE;
import static com.fltry.demo02.GlobalConfig.CHANNEL_CONFIG;
import static com.fltry.demo02.GlobalConfig.SAMPLE_RATE_INHZ;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAudioBinding dataBinding;
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    AudioRecord audioRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_audio);
        checkPermissions();
        dataBinding.setRecording(false);
        dataBinding.setMsg(getTime() + "：输出日志--------------------------");
        dataBinding.audioClearLog.setOnClickListener(this);
        dataBinding.audioStartRecorder.setOnClickListener(this);
        dataBinding.audioStopRecorder.setOnClickListener(this);
        dataBinding.audioTrack.setOnClickListener(this);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    public void startRecord() {
        log("准备录音---------------------");
        dataBinding.setRecording(true);
        final int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSizeInBytes);
        log("初始化audioRecord完成");
        final byte[] data = new byte[bufferSizeInBytes];
        final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        if (!file.mkdir()) {
            log(Environment.getExternalStorageDirectory() + "文件夹不存在");
        }
        if (file.exists()) {
            log("已删除存在的文件");
            file.delete();
        }
        audioRecord.startRecording();
        log("正在录音");

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream os = null;
                try {
                    os = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (null != os) {
                    log("正在写入文件");
                    while (!dataBinding.getRecording()) {
                        int read = audioRecord.read(data, 0, bufferSizeInBytes);
                        // 如果读取音频数据没有出现错误，就将数据写入到文件
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            try {
                                os.write(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        log("关闭输出流");
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    log("写入失败，输出流为空");
                }
            }
        });
    }

    public void stopRecord() {
        dataBinding.setRecording(false);
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        log("录音已停止---------------------");
    }

    public void log(String msg) {
        dataBinding.setMsg(dataBinding.getMsg() + "\n" + getTime() + "：" + msg);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.audio_clear_log:
                dataBinding.setMsg("");
                break;
            case R.id.audio_start_recorder:
                startRecord();
                break;
            case R.id.audio_stop_recorder:
                stopRecord();
                break;
            case R.id.audio_track:

                break;
        }
    }

    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }
}
