package com.jadehh;

import android.app.Application;
import android.hardware.usb.UsbDevice;
import android.os.Environment;
import android.view.View;

import com.example.jade.JadeLog;
import com.example.jade.JadeToast;
import com.example.jade.JadeTools;
import com.jadehh.utils.PackFile;
import com.jadehh.utils.VideoConfig;
import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;


public class JavaBridgeUVC {
    private static final String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "temp/";
    private String newRootPath = ""; //动态获取的
    private boolean isRequest;
    private static final boolean DEBUG = true;    // TODO set false on release

    private CameraInterface cameraListener;
    private UVCCameraHelper mCameraHelper;
    private CameraViewInterface mUVCCameraView;

    private static Application activity;
    private JadeTools jTools;
    private JSONObject jsonObject;
    private double[] weight_init;
    private int exposureValue;

    //录制视频回调
    public interface OnEncodeResultListener {
        void onRecordResult(VideoConfig videoConfig);
    }

    //回调给安卓前端摄像头的状态和压缩包的体制
    public interface CameraInterface {
        //摄像头的参数回调 0 为正常打开摄像头，
        //其余都为非正常打开摄像头
        public void onConnectCamera(boolean result);
        public void onDisconnectCamera(String path);
    }


    /**
     * Init.
     *
     * @param mTextureView the m texture view
     * @param line_y       the line y
     * @param xmin         the xmin
     * @param ymin         the ymin
     * @param xmax         the xmax
     * @param ymax         the ymax
     */
    public void init(View mTextureView, int line_y, int xmin, int ymin, int xmax, int ymax) {
        mUVCCameraView = (CameraViewInterface) mTextureView;
        mCameraHelper = new UVCCameraHelper();
        jTools = new JadeTools();
        JadeToast.e(activity,"初始化成功");
    }
    

    /**
     * Open camera.
     *
     * @param exposureValue the exposure value 曝光值
     * @param weight_begin  the weight begin 初始重量
     * @param jsonObject1   the json object 1 json配置文件
     */
    public void open_camera(int exposureValue, double[] weight_begin, JSONObject jsonObject1) {
        jsonObject = jsonObject1;
        weight_init = weight_begin;
        if (!mCameraHelper.ismIsOpening()) {
            //初始化相机之前,需要先删除文件夹
//            File temp = new File(ROOT_PATH);
//            jTools.deleteDirWihtFile(temp);
            JadeLog.e(this,"相机没有打开，需要打开摄像头");
            this.exposureValue = exposureValue;
            isRequest = false;
            mCameraHelper.initUSBMonitor(activity, mUVCCameraView, listener,videoListener);
            mCameraHelper.registerUSB();
        }else{
            JadeLog.e(this,"相机正常工作.不需要在重新打开");
            cameraListener.onConnectCamera(mCameraHelper.ismIsOpening());
        }

    }
    //USB摄像头的监听函数
    private UVCCameraHelper.OnMyDevConnectListener listener = new UVCCameraHelper.OnMyDevConnectListener() {
        @Override
        public void onAttachDev(UsbDevice device) {
            if (mCameraHelper == null || mCameraHelper.getUsbDeviceCount() == 0) {
                JadeLog.e(this, "check no usb camera");
                if (cameraListener != null) {
                    cameraListener.onConnectCamera(false);
                }
                return;
            }
            // request open permission
            if (!isRequest) {
                isRequest = true;
                if (mCameraHelper != null) {
                    mCameraHelper.requestPermission(-1,cameraListener);
                }
            }
        }

        @Override
        public void onDettachDev(UsbDevice device) {
            if (isRequest) {
                isRequest = false;
                mCameraHelper.setmIsOpening(false);
                cameraListener.onConnectCamera(false);
                JadeLog.e(this, device.getDeviceName() + " is out");
            }
        }

        @Override
        public void onConnectDev(UsbDevice device, final boolean isConnected) {
            if (!isConnected) {
                JadeLog.e(this, "fail to connect,please check resolution params");
            } else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        mCameraHelper.startPreview();
                        // 开启预览
                        mCameraHelper.setExposureMode(1);
                        mCameraHelper.setExposureValue(exposureValue);
                        startMediaCodec();
                        cameraListener.onConnectCamera(isConnected);
                    }
                }).start();
            }
        }

        @Override
        public void onDisConnectDev(UsbDevice device) {
            JadeLog.e(this,"On DisConnectDev : "+device.getDeviceName()+"disConnnect");
        }
    };
    


    /**
     * Start media codec.开启编码器,新建一个目录
     */
    public void startMediaCodec(){
        createFiles();
        String videoName = jTools.getTime();
        String videoPath = newRootPath + videoName;
        RecordParams params = new RecordParams(newRootPath,videoPath,videoName,0,true);
        JadeLog.e(this, "开启编码器");
        mCameraHelper.setRecordParmas(params);
        JadeLog.e( this,"完成编码器");
    }


    /**
     * Record video.
     */
    public void recordVideo() {
        if (mCameraHelper == null && !mCameraHelper.ismIsOpening()) {
            JadeLog.e(this, "sorry,camera open failed");
            return;
        }
        mCameraHelper.startRecording();
        mCameraHelper.changeWeightValue(weight_init);
        JadeToast.e(activity,"开始录制视频");
    }

    //录制视频结束
    private OnEncodeResultListener videoListener = new OnEncodeResultListener(){
        @Override
        public void onRecordResult(VideoConfig videoConfig) {
            //写入文件,压缩，和删除源文件
            writeConfig(videoConfig);
            JadeLog.e(this,"zip  Path"+ videoConfig.zipPath);
            cameraListener.onDisconnectCamera(videoConfig.zipPath);
            startMediaCodec();
            JadeToast.e(activity,"视频录制结束");
        }
    };

    /**
     * Stop.关门在传一次重量
     *
     * @param weight_end the weight end
     */
    public void stop(double[] weight_end) {
        mCameraHelper.changeWeight();
        mCameraHelper.changeWeightValue(weight_end);
        FileUtils.releaseFile();
        mCameraHelper.stopRecording();
        isRequest = false;
    }

    /**
     * Write config.
     *
     * @param config the config
     */
    private void writeConfig(VideoConfig config) {
        config.setJsonObject(jsonObject);
        PackFile file = new PackFile(jTools,config);
        file.writeConfig();
        file.zipFiles();
        file.deleteFiles();
    }
    private void createFiles() {
        try {
            jTools.createDir(ROOT_PATH);
            String fileName = jTools.getTime();
            newRootPath = ROOT_PATH + fileName + "/";
            try {
                jTools.createDir(newRootPath);
            } catch (IOException e) {
                JadeLog.e(this, e.getMessage());
            }
        } catch (IOException e) {
            JadeLog.e(this, e.getMessage());
        }
    }

    /**
     * Instantiates a new Java bridge abstract uvc.
     *
     * @param context  the context
     * @param listener the listener
     */
    public JavaBridgeUVC(final Application context, final CameraInterface listener) {
        activity = context;
        if (DEBUG) {
            JadeLog.e(this, "USBMonitor:Constructor");
        }
        if (listener == null) {
            throw new IllegalArgumentException("OnDeviceConnectListener should not null.");
        }
        cameraListener = listener;
    }


    /**
     * Release.释放资源,资源释放需要重新初始化
     */
    public void release(){
        if (mCameraHelper != null){
            mCameraHelper.release();
            mUVCCameraView = null;
            mCameraHelper = null;
        }
    }

}
