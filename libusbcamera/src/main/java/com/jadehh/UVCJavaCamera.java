package com.jadehh;
import android.app.Application;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.example.jade.JadeLog;

import com.jadehh.utils.VideoConfig;
import com.serenegiant.usb.IFrameCallback;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;

import java.nio.ByteBuffer;

import com.example.jade.JadeTools;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.encoder.biz.H264EncodeConsumer;
import com.serenegiant.usb.encoder.biz.Mp4MediaMuxer;


public class UVCJavaCamera {
    private static final boolean DEBUG = true;
    private boolean mIsPreviewing;
    private UVCCamera mUVCCamera;
    private final Object mSync = new Object();
    private float mBandwidthFactor;
    private boolean mIsRecording,mIsOpening;
    private JadeTools jTools;
    private int mWidth,mHeight,mPreviewMode,index,weights_index;
    private H264EncodeConsumer h264EncodeConsumer;
    private H264Encoder encoder;
    private JavaBridgeUVC.OnEncodeResultListener videoListener;
    private Mp4MediaMuxer mMuxer;
    private H264EncodeConsumer mH264Consumer;

    public UVCJavaCamera(final Application parent,int width, int height, int preViewMode, float bandwidthFactor, JavaBridgeUVC.OnEncodeResultListener videoListener){
        this.mWidth = width;
        this.mHeight = height;
        this.mPreviewMode = preViewMode;
        this.mBandwidthFactor = bandwidthFactor;
        this.videoListener = videoListener;
        mIsRecording = false;
        index = 0;
        jTools = new JadeTools();
    }

    /**
     * Sets params.设置参数
     *
     * @param params the params
     */
    public void setRecordParmas(RecordParams params) {
        encoder = new H264Encoder(mWidth,mHeight);
        h264EncodeConsumer = encoder.startVideoRecord();
        h264EncodeConsumer.start();
        encoder.setRecordParams(params);
        mMuxer = encoder.getmMuxer();
        mH264Consumer = encoder.getmH264Consumer();
    }


    /**
     * Open camera. 打开摄像头
     *
     * @param ctrlBlock the ctrl block
     */
    public void openCamera(final USBMonitor.UsbControlBlock ctrlBlock){
        JadeLog.v(this,"open camera");
            try {
                final UVCCamera camera = new UVCCamera();
                camera.open(ctrlBlock);
                synchronized (mSync) {
                    mUVCCamera = camera;
                }
                mIsOpening = true;
            } catch (final Exception e) {
                mIsOpening = false;
            }

    }

    //回调函数是不会卡死的
    private final IFrameCallback mIFrameCallback = new IFrameCallback() {
        @Override
        public void onFrame(final ByteBuffer frame) {
            //视频编码
            if (mIsRecording) {
                long start_time = jTools.getTimeStamp();
                int len = frame.capacity();
                final byte[] yuv = new byte[len];
                frame.get(yuv);
                H264Encoder(yuv);
                long end_time = jTools.getTimeStamp();
                if (DEBUG) {
                    JadeLog.e(this, "正在读取第" + String.valueOf(index) + "帧,耗时" + String.valueOf(end_time - start_time) + "ms");
                }
                index = index + 1;
            }
        }
    };

    /**
     * Start preview.
     *
     * @param surface the surface
     */
    public void startPreview(final Object surface) {
        if (DEBUG) JadeLog.v(this, "startPreview:");
        if ((mUVCCamera == null) || mIsPreviewing) return;
        try {
            mUVCCamera.setPreviewSize(mWidth, mHeight, 1, 31, mPreviewMode, mBandwidthFactor);
            mUVCCamera.setFrameCallback(mIFrameCallback, UVCCamera.PIXEL_FORMAT_YUV420SP);
        } catch (final IllegalArgumentException e) {
            JadeLog.e(this,"设置Preview失败");
        }
        if (surface instanceof SurfaceHolder) {
            mUVCCamera.setPreviewDisplay((SurfaceHolder) surface);
        }
        if (surface instanceof Surface) {
            mUVCCamera.setPreviewDisplay((Surface) surface);
        } else {
            mUVCCamera.setPreviewTexture((SurfaceTexture) surface);
        }
        mUVCCamera.startPreview();
        mUVCCamera.updateCameraParams();
        synchronized (mSync) {
            mIsPreviewing = true;
        }
        JadeLog.v(this,"设置Preview成功");
    }

    public boolean ismIsOpening() {
        return mIsOpening;
    }

    public void setmIsOpening(boolean mIsOpening) {
        this.mIsOpening = mIsOpening;
    }

    /**
     * Sets recording.
     *
     * @param recording the recording
     */
    public void setRecording(boolean recording) {
        JadeLog.v(this, "Recording:");
        if ((mUVCCamera == null) || mIsRecording){
            if (DEBUG) JadeLog.v(this, "Recording failed:");
            return;
        }
        mIsRecording = recording;
    }

    /**
     * Is recording boolean.
     *
     * @return the boolean
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * Stop recording.
     *
     * @param recording the recording
     */
    public void stopRecording(boolean recording){
        JadeLog.v(this, "stopRecording:");
        if ((mUVCCamera == null) || !mIsRecording) return;
        mIsRecording = recording;
        clear();
        JadeLog.e(this, "停止写入图片");
        stopRecord();
        RecordParams params = encoder.getRecordParams();
        VideoConfig config = new VideoConfig(null,params.getSavePath(),null,params.getVideoName(),params.getRecordPath(),null,null);
        //录制完成，返回录制结果
        videoListener.onRecordResult(config);
        clear();
    }

    private void stopRecord(){
        if (mMuxer != null) {
            mMuxer.release();
            mMuxer = null;
            JadeLog.v(this,  "停止本地录制");
        }
        stopVideoRecord();
    }
    private void stopVideoRecord() {
        if (mH264Consumer != null) {
            mH264Consumer.exit();
            mH264Consumer.setTmpuMuxer(null);
            try {
                Thread t2 = mH264Consumer;
                mH264Consumer = null;
                if (t2 != null) {
                    t2.interrupt();
                    t2.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void H264Encoder(byte[] yuv) {
        h264EncodeConsumer.setRawYuv(yuv, mWidth, mHeight);
    }
    /**
     * Set exposure mode.
     *
     * @param mode the mode
     */
    public void setExposureMode(int mode){
        if (mUVCCamera == null) return;
        mUVCCamera.setExposureMode(mode);
    }
    /**
     * Set exposure value.
     *
     * @param exposureValue the exposure value
     */
    public void setExposureValue(int exposureValue){
        if (mUVCCamera == null) return;
        mUVCCamera.setExposureValue(exposureValue);
    }
    public void changeWeight(){
        weights_index = weights_index + 1;
    }
    /**
     * Clear 释放下表
     */
    public void clear(){
        index = 0;
        weights_index = 0;
    }

    public void  release(){
        if (mUVCCamera != null){
            mIsOpening = false;
            mUVCCamera.close();
        }

    }

}
