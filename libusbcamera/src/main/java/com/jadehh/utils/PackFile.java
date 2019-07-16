package com.jadehh.utils;

import com.example.jade.JadeLog;
import com.example.jade.JadeTools;
import com.example.jade.ZipFiles;
import com.google.gson.reflect.TypeToken;
import com.serenegiant.usb.common.ZipConfig;

import org.json.JSONException;

import java.io.File;
import java.util.List;

/**
 * 作者：Create on 2019/7/4 15:38  by  jadehh
 * 邮箱：
 * 描述：TODO
 * 最近修改：2019/7/4 15:38 modify by jadehh
 */
public class PackFile {
    private JadeTools jadeTools;
    private VideoConfig config;
    public PackFile(JadeTools jadeTools,VideoConfig videoConfig){
        this.jadeTools = jadeTools;
        this.config = videoConfig;
    }
    public void writeConfig() {
        try {
            //数据模型
            ZipConfig zipConfig = new ZipConfig();
            //找到文件路径
            String filepath = config.jsonObject.getString("DRIC_AREAS_V0");
            //转移文件
            File file = new File(filepath);
            if (file.exists()) {
                JadeLog.e(this, "DRIC_AREAS_V0 :" + filepath);
                String areafile = config.savePath + "/container.png";
                jadeTools.copyFile(filepath, areafile);
                zipConfig.setDRIC_AREAS_V0("container.png");
            }
            //时间
            zipConfig.setTIME_STAMP(config.TIME_STAMP);
            //丢帧的时间戳
//            zipConfig.setWRONG_TIME_STAMP(config.WRONG_TIME_STAMP);

            //视频文件名称
            zipConfig.setIMG_V0_PATH(config.videoName);
            //重量index list
            zipConfig.setWEIGHT_MEASURE_INDEX(config.weights_index_list);
            //重量list
            zipConfig.setWEIGHT_CHANGES(config.weights_list);
            //商品key
            List<String> keys;
            keys = JsonUtil.getInstance().parserList(config.jsonObject.get("GOODS_KEY").toString(), new TypeToken<List<String>>() {
            });
            zipConfig.setGOODS_KEY(keys);
            //商品重量
            List<Double> weights;
            weights = JsonUtil.getInstance().parserList(config.jsonObject.get("GOODS_WEIGHT").toString(), new TypeToken<List<Double>>() {
            });
            zipConfig.setGOODS_WEIGHT(weights);
            //用户配置
            ZipConfig.UserConfig userConfig = JsonUtil.getInstance().getGson().fromJson(config.jsonObject.get("USER_CONFIG").toString(), ZipConfig.UserConfig.class);
            zipConfig.setUSER_CONFIG(userConfig);
            jadeTools.writeTxtToFile(JsonUtil.getInstance().getGson().toJson(zipConfig), config.savePath, "config.json");
        } catch (JSONException e) {
            JadeLog.e(this, e.getMessage());
        }
    }

    public void zipFiles() {
        try {
            ZipFiles.zip(config.savePath.substring(0, config.savePath.length() - 1), config.savePath.substring(0, config.savePath.length() - 1) + ".zip");
            config.setZipPath(config.savePath.substring(0, config.savePath.length() - 1) + ".zip");
        } catch (Exception e) {
            JadeLog.e(this, e.getMessage());
        }
    }

    public void deleteFiles() {
        File dir = new File(config.savePath);
        jadeTools.deleteDirWihtFile(dir);
    }

}
