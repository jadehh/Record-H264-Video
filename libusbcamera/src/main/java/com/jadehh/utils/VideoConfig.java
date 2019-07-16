package com.jadehh.utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：Create on 2019/7/4 17:15  by  jadehh
 * 邮箱：
 * 描述：TODO
 * 最近修改：2019/7/4 17:15 modify by jadehh
 */
public class VideoConfig {
    public JSONObject jsonObject;
    public String savePath,videoName,videoPath;
    public List<Integer> TIME_STAMP;
    public List<Integer> weights_index_list;
    public ArrayList<double[]> weights_list;
    public String zipPath;

    public VideoConfig(JSONObject jsonObject, String savePath, List<Integer> TIME_STAMP, String videoName, String videoPath, List<Integer> weights_index_list, ArrayList<double[]> weights_list) {
        setJsonObject(jsonObject);
        setSavePath(savePath);
        setTIME_STAMP(TIME_STAMP);
        setVideoName(videoName);
        setVideoPath(videoPath);
        setWeights_index_list(weights_index_list);
        setWeights_list(weights_list);
    }

    public void setZipPath(String zipPath) {
        this.zipPath = zipPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }


    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public void setJsonObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public void setTIME_STAMP(List<Integer> TIME_STAMP) {
        this.TIME_STAMP = TIME_STAMP;
    }

    public void setWeights_index_list(List<Integer> weights_index_list) {
        this.weights_index_list = weights_index_list;
    }

    public void setWeights_list(ArrayList<double[]> weights_list) {
        this.weights_list = weights_list;
    }

    public String getZipPath() {
        return zipPath;
    }
}
