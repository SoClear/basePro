package com.example.xiaojin20135.basemodule.view.others;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xiaojin20135.basemodule.R;


public class BaseToast {

    private static Toast mToastNormal;
    private static View view;

    /**
     * 普通的toast提示
     * */
    public static void showNOrmalToast(Context mContext, String message){

        BaseToast.cancel();

        if(mToastNormal == null){
            mToastNormal=new Toast(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view =inflater.inflate(R.layout.toast_layout,null);
            TextView textView1=view.findViewById(R.id.toast_title);
            textView1.setText(message);

            mToastNormal.setView(view);
            mToastNormal.setGravity(Gravity.CENTER,0,0);
            mToastNormal.setDuration(Toast.LENGTH_LONG);
//            mToastNormal = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        }

        mToastNormal.show();

    }

    /**
     * 普通的toast提示
     * */
    public static void showNOrmalToast(Context mContext, int message){

        BaseToast.cancel();

        if(mToastNormal == null){
            mToastNormal=new Toast(mContext);
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view =inflater.inflate(R.layout.toast_layout,null);
            TextView textView1=view.findViewById(R.id.toast_title);
            textView1.setText(message);

            mToastNormal.setView(view);
            mToastNormal.setGravity(Gravity.CENTER,0,0);
            mToastNormal.setDuration(Toast.LENGTH_LONG);
//            mToastNormal = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
        }

        mToastNormal.show();

    }
    /**
     *toast取消
     */
    public static void cancel(){

        if(mToastNormal != null){
            mToastNormal.cancel();
            mToastNormal = null;
        }

    }

}
