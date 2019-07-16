package com.jadehh;

import com.jiangdg.usbcamera.utils.FileUtils;
import com.serenegiant.usb.encoder.RecordParams;
import com.serenegiant.usb.encoder.biz.H264EncodeConsumer;
import com.serenegiant.usb.encoder.biz.Mp4MediaMuxer;


/**
 * 作者：Create on 2019/7/2 14:22  by  jadehh
 * 邮箱：
 * 描述：TODO
 * 最近修改：2019/7/2 14:22 modify by jadehh
 */
public class H264Encoder {
    private H264EncodeConsumer mH264Consumer;
    private int mWidth,mHeight;
    private Mp4MediaMuxer mMuxer;
    private RecordParams recordParams;
    public H264Encoder(int width,int height){
        mWidth = width;
        mHeight = height;
    }

    public void setRecordParams(RecordParams recordParams) {
        this.recordParams = recordParams;
        StartMedieCodec();
    }

    public H264EncodeConsumer startVideoRecord() {
        mH264Consumer = new H264EncodeConsumer(mWidth,mHeight);
        mH264Consumer.setOnH264EncodeResultListener(new H264EncodeConsumer.OnH264EncodeResultListener() {
            @Override
            public void onEncodeResult(byte[] data, int offset, int length, long timestamp) {
                FileUtils.putFileStream(data);
            }
        });
        mH264Consumer.startMediaCodec();
        return mH264Consumer;
    }

    private void StartMedieCodec(){
        if (recordParams != null) {
            mMuxer = new Mp4MediaMuxer(recordParams.getRecordPath(),
                    recordParams.getRecordDuration() * 60 * 1000, recordParams.isVoiceClose());
        }
        AddMuxer();
    }

    public Mp4MediaMuxer getmMuxer() {
        return mMuxer;
    }

    public H264EncodeConsumer getmH264Consumer() {
        return mH264Consumer;
    }

    public void AddMuxer(){
        if (mMuxer != null) {
            if (mH264Consumer != null) {
                mH264Consumer.setTmpuMuxer(mMuxer);
            }
        }
    }

    public RecordParams getRecordParams() {
        return recordParams;
    }

}
