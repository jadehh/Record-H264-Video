package com.jadehh;

import android.app.Application;
import android.graphics.SurfaceTexture;
import android.hardware.usb.UsbDevice;

import com.example.jade.JadeLog;
import com.jiangdg.libusbcamera.R;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.widget.CameraViewInterface;

import java.util.List;

/**
 * The type Uvc camera helper.
 */
public class UVCCameraHelper {
    private Application mActivity;
    private static final String TAG = "UVCCameraHelper";
    private CameraViewInterface mCamView;
    private USBMonitor mUSBMonitor;
    private UVCJavaCamera uvcJavaCamera;



    public interface OnMyDevConnectListener {
        void onAttachDev(UsbDevice device);

        void onDettachDev(UsbDevice device);

        void onConnectDev(UsbDevice device, boolean isConnected);

        void onDisConnectDev(UsbDevice device);
    }

    /**
     * Init usb monitor.
     *
     * @param activity   the activity
     * @param cameraView the camera view
     * @param listener   the listener
     */
    public void initUSBMonitor(Application activity, CameraViewInterface cameraView, final OnMyDevConnectListener listener,final JavaBridgeUVC.OnEncodeResultListener videoListener) {
        this.mActivity = activity;
        this.mCamView = cameraView;
        mUSBMonitor = new USBMonitor(activity.getApplicationContext(), new USBMonitor.OnDeviceConnectListener() {

            // called by checking usb device
            // do request device permission
            @Override
            public void onAttach(UsbDevice device) {
                if (listener != null) {
                    listener.onAttachDev(device);
                }
            }
            // called by taking out usb device
            // do close camera
            @Override
            public void onDettach(UsbDevice device) {
                if (listener != null) {
                    listener.onDettachDev(device);
                }
            }

            // called by connect to usb camera
            // do open camera,start previewing
            @Override
            public void onConnect(final UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                openCamera(ctrlBlock);
                if (listener != null) {
                    listener.onConnectDev(device, true);
                }
            }

            // called by disconnect to usb camera
            // do nothing
            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                if (listener != null) {
                    listener.onDisConnectDev(device);
                }
            }

            @Override
            public void onCancel(UsbDevice device) {

            }
        });
        createUVCCamera(activity,videoListener);
    }

    private void createUVCCamera(final Application application,JavaBridgeUVC.OnEncodeResultListener videoListener) {
        uvcJavaCamera = new UVCJavaCamera(application,1280, 720, 1, (float) 1.0,videoListener);
    }


    private void openCamera(USBMonitor.UsbControlBlock ctrlBlock) {
        if (uvcJavaCamera != null) {
            uvcJavaCamera.openCamera(ctrlBlock);
        }
    }

    /**
     * Ism is opening boolean.
     *
     * @return the boolean
     */
    public boolean ismIsOpening() {
        if (uvcJavaCamera != null){
            return uvcJavaCamera.ismIsOpening();
        }
        return false;
    }

    /**
     * Sets is opening.
     *
     * @param mIsOpening the m is opening
     */
    public void setmIsOpening(boolean mIsOpening) {
        if (uvcJavaCamera != null){
            uvcJavaCamera.setmIsOpening(mIsOpening);
        }
    }

    /**
     * Gets usb device count.
     *
     * @return the usb device count
     */
    public int getUsbDeviceCount() {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return 0;
        }
        return devList.size();
    }

    /**
     * Gets usb device list.
     *
     * @return the usb device list
     */
    public List<UsbDevice> getUsbDeviceList() {
        List<DeviceFilter> deviceFilters = DeviceFilter
                .getDeviceFilters(mActivity.getApplicationContext(), R.xml.device_filter);
        if (mUSBMonitor == null || deviceFilters == null)
            return null;
        return mUSBMonitor.getDeviceList(deviceFilters.get(0));
    }

    /**
     * Register usb.
     */
    public void registerUSB() {
        if (mUSBMonitor != null) {
            mUSBMonitor.register();
        }
    }

    /**
     * Request permission.
     *
     * @param index    the index
     * @param listener the listener
     */
    public void requestPermission(int index, JavaBridgeUVC.CameraInterface listener) {
        List<UsbDevice> devList = getUsbDeviceList();
        if (devList == null || devList.size() == 0) {
            return;
        }
        int count = devList.size();
        if (index >= count)
            new IllegalArgumentException("index illegal,should be < devList.size()");
        if (mUSBMonitor != null) {
            List<UsbDevice> usbDevices = getUsbDeviceList();
            for (int i=0;i<usbDevices.size();i++){
                JadeLog.e(this,usbDevices.get(i).getProductName());
                if (usbDevices.get(i).getProductName().contains("Camera")){
                    index = i;
                    break;
                }
            }
            if (index!=-1){
                mUSBMonitor.requestPermission(getUsbDeviceList().get(index));
            }else{
                JadeLog.e(this,"没有找到可用的USB Camera");
                listener.onConnectCamera(false);
            }

        }
    }

    /**
     * Start preview.
     */
    public void startPreview() {
        SurfaceTexture st = mCamView.getSurfaceTexture();
        if (uvcJavaCamera != null) {
            uvcJavaCamera.startPreview(st);
        }
    }

    /**
     * Start recording.
     */
    public void startRecording() {
        if (uvcJavaCamera != null) {
            if (!uvcJavaCamera.isRecording()) {
                uvcJavaCamera.setRecording(true);
            }
        }
    }

    /**
     * Stop recording.
     */
    public void stopRecording() {
        if (uvcJavaCamera != null) {
            if (uvcJavaCamera.isRecording()) {
                uvcJavaCamera.stopRecording(false);
            }
        }
    }

    /**
     * Set exposure mode.
     *
     * @param exposureMode the exposure mode
     */
    public void setExposureMode(int exposureMode) {
        if (uvcJavaCamera != null) {
            uvcJavaCamera.setExposureMode(exposureMode);
        }
    }

    /**
     * Set exposure value.
     * 设置曝光值
     * @param exposureValue the exposure value
     */
    public void setExposureValue(int exposureValue){
        if (uvcJavaCamera != null){
            uvcJavaCamera.setExposureValue(exposureValue);
        }
    }


    /**
     * Set record parmas.
     *
     * @param parmas the parmas
     */
    public void setRecordParmas(RecordParams parmas){
        if (uvcJavaCamera != null){
            uvcJavaCamera.setRecordParmas(parmas);
        }
    }

    /**
     * Change weight value.
     *
     * @param weight the weight
     */
    public void changeWeightValue(double[] weight){
        if (uvcJavaCamera != null){
//            uvcJavaCamera.changeWeightValue(weight);
        }
    }

    /**
     * Change weight.
     */
    public void changeWeight(){
        uvcJavaCamera.changeWeight();
    }

    /**
     * Release. 释放资源
     */
    public void release(){
        uvcJavaCamera.release();
    }


}

