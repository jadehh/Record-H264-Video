package com.example.jade;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 作者：Create on 2019/7/17 10:18  by  jadehh
 * 邮箱：
 * 描述：TODO
 * 最近修改：2019/7/17 10:18 modify by jadehh
 */
public class JadeToast {
    private static boolean DEBUG = true;
    public static void e(Context activity, String message){
        if (DEBUG){
            Toast toast=Toast.makeText(activity,message,Toast.LENGTH_SHORT );
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
    public static void e(Application activity, String message){
        if (DEBUG){
            Toast toast=Toast.makeText(activity,message,Toast.LENGTH_SHORT );
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }
}
