# Android 音视频开发入门指南
### 一、在Android平台绘制一张图片，使用至少 3 种不同的 API，ImageView，SurfaceView，自定义 View
[View](https://github.com/fengliang1992/MyVideoPlayer/blob/master/demo01/src/main/java/com/fltry/demo01/MyImageView.java)<br>
[ImageView](https://github.com/fengliang1992/MyVideoPlayer/blob/master/demo01/src/main/res/layout/activity_demo1.xml)<br>
[SurfaceView](https://github.com/fengliang1992/MyVideoPlayer/blob/master/demo01/src/main/java/com/fltry/demo01/MySurfaceView.java)
### 二、在 Android 平台使用 AudioRecord 和 AudioTrack API 完成音频 PCM 数据的采集和播放，并实现读写音频 wav 文件
#### 录制及存储
1、获取读写权限及录音权限。<br>
2、初始化AudioRecord对象。<br>
3、调用AudioRecord的startRecording()方法开始录制。<br>
4、另起一个线程持续的写入SD卡。<br>
5、将pcm文件转换成Wav文件.

