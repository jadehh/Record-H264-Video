package com.serenegiant.usb.common;


public class H264QueueObject {
    public int index;
    public int weight_index;
    public byte[] data;

    public void setWeight_index(int weight_index) {
        this.weight_index = weight_index;
    }


    public void setIndex(int index) {
        this.index = index;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public H264QueueObject(byte[] data,int index, int weight_index){
        setData(data);
        setIndex(index);
        setWeight_index(weight_index);
    }
}
