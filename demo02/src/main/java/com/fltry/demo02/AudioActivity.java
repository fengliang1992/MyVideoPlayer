package com.fltry.demo02;

import android.Manifest;
import android.annotation.TargetApi;
import android.databinding.DataBindingUtil;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.fltry.demo02.databinding.ActivityAudioBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.fltry.demo02.GlobalConfig.AUDIO_FORMAT;
import static com.fltry.demo02.GlobalConfig.AUDIO_SOURCE;
import static com.fltry.demo02.GlobalConfig.CHANNEL_CONFIG_IN;
import static com.fltry.demo02.GlobalConfig.CHANNEL_CONFIG_OUT;
import static com.fltry.demo02.GlobalConfig.SAMPLE_RATE_INHZ;

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {
    ActivityAudioBinding dataBinding;
    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private String[] permissions = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    AudioRecord audioRecord;
    private FileInputStream is;
    private AudioTrack audioTrack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_audio);
        checkPermissions();
        dataBinding.setRecording(false);
        dataBinding.setPlaying(false);
        dataBinding.setMsg(getTime() + "：输出日志");
        dataBinding.audioClearLog.setOnClickListener(this);
        dataBinding.audioStartRecorder.setOnClickListener(this);
        dataBinding.audioStopRecorder.setOnClickListener(this);
        dataBinding.audioTrackStart.setOnClickListener(this);
        dataBinding.audioTrackStop.setOnClickListener(this);
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST);
        }
    }

    public void startRecord() {
        log("准备录音");
        dataBinding.setRecording(true);
        final int bufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);
        audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_INHZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT, bufferSizeInBytes);
        log("初始化audioRecord完成");
        final byte[] data = new byte[bufferSizeInBytes];
        final File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        if (!file.mkdir()) {
            log(Environment.getExternalStorageDirectory() + "文件夹未创建");
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
                    log(e.getMessage());
                }
                if (null != os) {
                    log("正在写入文件：" + dataBinding.getRecording());
                    while (dataBinding.getRecording()) {
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
        }).start();
    }

    public void stopRecord() {
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
        log("录音已停止");
        log("准备将pcm文件转换成wav文件");
        PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_IN, AUDIO_FORMAT);
        File pcmFile = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        File wavFile = new File(Environment.getExternalStorageDirectory(), "test.wav");
        if (!wavFile.mkdirs()) {
            log("wavFile Directory not created");
        }
        if (wavFile.exists()) {
            wavFile.delete();
            log("删除原wav文件");
        }
        log("开始转换");
        String result = pcmToWavUtil.pcmToWav(pcmFile.getAbsolutePath(), wavFile.getAbsolutePath());
        log(result);
        dataBinding.setRecording(false);
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
            case R.id.audio_track_start:
                startPlayRecord();
                break;
            case R.id.audio_track_stop:
                stopPlayRecord();
                break;
        }
    }

    public String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void startPlayRecord() {
       /*
        * SAMPLE_RATE_INHZ 对应pcm音频的采样率
        * channelConfig 对应pcm音频的声道
        * AUDIO_FORMAT 对应pcm音频的格式
        * */
        log("开始播放录音");
        dataBinding.setPlaying(true);
        final int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG_OUT, AUDIO_FORMAT);
        audioTrack = new AudioTrack(
                new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                new AudioFormat.Builder().setSampleRate(SAMPLE_RATE_INHZ)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_CONFIG_OUT)
                        .build(),
                minBufferSize,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE);
        audioTrack.play();

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");
        try {
            is = new FileInputStream(file);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] tempBuffer = new byte[minBufferSize];
                        while (is.available() > 0) {
                            int readCount = is.read(tempBuffer);
                            if (readCount == AudioTrack.ERROR_INVALID_OPERATION ||
                                    readCount == AudioTrack.ERROR_BAD_VALUE) {
                                continue;
                            }
                            if (readCount != 0 && readCount != -1) {
                                audioTrack.write(tempBuffer, 0, readCount);
                            }
                        }
                        is.close();
                        is = null;
                        log("播放结束");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlayRecord() {
        if (null != audioTrack) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
        dataBinding.setPlaying(false);
        log("停止播放");
    }
}
