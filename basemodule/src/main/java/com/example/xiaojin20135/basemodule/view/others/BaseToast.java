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

    private static String oldMsg;
    private static long oneTime=0;
    private static long twoTime=0;
    private static TextView sTextView1;

    /**
     * 普通的toast提示
     * */
    public static void showNOrmalToast(Context mContext, String message){

        if(mToastNormal == null){
            mToastNormal=new Toast(mContext.getApplicationContext());
            LayoutInflater inflater = LayoutInflater.from(mContext.getApplicationContext());
            View view =inflater.inflate(R.layout.toast_layout,null);
            sTextView1 = view.findViewById(R.id.toast_title);
            sTextView1.setText(message);
            mToastNormal.setView(view);
            mToastNormal.setGravity(Gravity.CENTER,0,0);
            mToastNormal.setDuration(Toast.LENGTH_LONG);
            mToastNormal.show();
            oneTime=System.currentTimeMillis();
        }else {
            twoTime=System.currentTimeMillis();
            if(message.equals(oldMsg)){
                if(twoTime-oneTime>Toast.LENGTH_LONG){
                    mToastNormal.show();
                }
            }else{
                oldMsg = message;
                sTextView1.setText(message);
                mToastNormal.show();
            }
        }



    }

    /**
     * 普通的toast提示
     * */
    public static void showNOrmalToast(Context mContext, int message){
        showNOrmalToast(mContext,mContext.getString(message));
    }

}
