package com.example.xiaojin20135.basemodule.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Autowired;
import com.alibaba.android.arouter.launcher.ARouter;
import com.example.xiaojin20135.basemodule.R;
import com.example.xiaojin20135.basemodule.retrofit.bean.ActionResult;
import com.example.xiaojin20135.basemodule.retrofit.bean.ResponseBean;
import com.example.xiaojin20135.basemodule.retrofit.helper.RetrofitManager;
import com.example.xiaojin20135.basemodule.retrofit.presenter.PresenterImpl;
import com.example.xiaojin20135.basemodule.retrofit.util.HttpError;
import com.example.xiaojin20135.basemodule.retrofit.view.IBaseView;
import com.example.xiaojin20135.basemodule.util.ConstantUtil;
import com.example.xiaojin20135.basemodule.view.others.BaseToast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import okhttp3.MultipartBody;

/**
 * @author lixiaojin
 * @create 2018-07-14
 * @Describe
 */
public abstract class BaseActivity extends AppCompatActivity implements IBaseView {
    public static List<Activity> activities = new ArrayList<>();
    public static String TAG = "";
    private static AlertDialog.Builder alertDialog;
    private static Toast toast;
    private SharedPreferences sharedPreferences;
    public static ProgressDialog progressDialog;
    private PresenterImpl presenterImpl;
    public boolean isShowProgressDialog = true;

    private String lastUrl = ""; //最后一次请求url
    private Map lastMap = new HashMap(); //最后一次请求参数
    private String lastMethodName = "";//最后一次请求的方法名
    private String lastErrorMethodName = "";//最后一次请求的错误方法名
    private MultipartBody.Part[] lastFilePart = null;//最后一次请求附件
    private String lastSuffix = "";//最后一次请求后缀

    private int lastReqCode = -1;
    SystemLogInterface mSystemLogInterface;
    private boolean isRestart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActivity(this);
        if (getLayoutId() != 0) {
            setContentView(getLayoutId());
        } else {
            //throw new IllegalArgumentException("返回一个正确的ContentView!");
        }
        mSystemLogInterface = ARouter.getInstance().navigation(SystemLogInterface.class);
        ButterKnife.bind(this);
        loadData();
        initView();
        initEvents();

        TAG = this.getLocalClassName();
        Log.d("BaseActivity", TAG);
        presenterImpl = new PresenterImpl(this, this);


    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected abstract void initEvents();

    protected abstract void loadData();

    @Override
    protected void onPause() {
        if (progressDialog != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (!this.isDestroyed()) {
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
            }
        }
        super.onPause();
    }

    private void addActivity(Activity activity) {
        if (activity != null && !activities.contains(activity)) {
            activities.add(activity);
            BaseApplication.setActivity(activity);
        }
        Log.d(TAG, "activities.size = " + activities.size());
    }

    private void removeActivity(Activity activity) {
        if (activity != null && activities.contains(activity)) {
            activities.remove(activity);
        }
    }

    public static List<Activity> getActivities() {
        return activities;
    }

    //退出
    public static void exit() {
        if (activities != null && activities.size() > 0) {
            for (Activity activity : activities) {
                activity.finish();
            }
        }
        System.exit(0);
    }

    public SharedPreferences getMySharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = getSharedPreferences(ConstantUtil.SHAREDNAME, MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public void showAlertDialog(Context context, String text, String title) {
        alertDialog = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        alertDialog.setTitle(title);
        alertDialog.setMessage(text);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        alertDialog.show();
    }

    public void showAlertDialog(Context context, String text) {
        alertDialog = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        alertDialog.setTitle("");
        alertDialog.setMessage(text);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        alertDialog.show();
    }

    public void showAlertDialog(Context context, int id) {
        alertDialog = new AlertDialog.Builder(context, R.style.BDAlertDialog);
        alertDialog.setTitle("");
        alertDialog.setMessage(id);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("好的", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ;
            }
        });
        alertDialog.show();
    }

    public static void showToast(Context mContext, String text) {
        /*if(toast == null){
            toast = Toast.makeText(mContext,text,Toast.LENGTH_LONG);
        }else{
            toast.setText(text);
        }
        toast.show();*/
//        Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
        BaseToast.showNOrmalToast(mContext, text);
    }

    public static void showToast(Context mContext, int id) {
        /*if(toast == null){
            toast = Toast.makeText(mContext,id,Toast.LENGTH_LONG);
        }else{
            toast.setText(id);
        }
        toast.show();*/
        BaseToast.showNOrmalToast(mContext, id);
    }

    /**
     * 返回键监听
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    //获取版本码
    public String getAppVersionName() {
        //获取版本码
        PackageManager packageManager = this.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.getPackageName(), 0);
            return packageInfo == null ? "" : packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void showProgress() {
        //等待框
        if (isShowProgressDialog) {
            if (progressDialog == null || !progressDialog.isShowing()) {
                progressDialog = new ProgressDialog(this, R.style.BDAlertDialog);
            }
//            Window window = getWindow();
//            window.setWindowAnimations(R.style.NoAnimationDialog); // 添加动画
            progressDialog.show();

        }
    }

    @Override
    public void setProgressText(String text) {
        if (progressDialog != null && progressDialog.isShowing()) {
            Log.d(TAG, "text = " + text);
            progressDialog.setMessage(text);
        }
    }

    @Override
    public void setProgressValue(int value) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setProgress(value);
        }
    }


    @Override
    public void showProgress(boolean hideTitle, String message, boolean cancled) {
        //等待框
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this, R.style.BDAlertDialog);
        }
        progressDialog.setMessage(message);
        if (!hideTitle) {
            progressDialog.setTitle(R.string.app_name);
        }
        progressDialog.setCancelable(cancled);
        progressDialog.show();
    }

    @Override
    public void dismissProgress() {

        try {
            if ((progressDialog != null) && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            progressDialog = null;
        }
    }


    /**
     * @author lixiaojin
     * @createon 2018-07-17 10:23
     * @Describe 请求数据 ，带完整路径，自定义回调方法
     */
    public void tryToGetData(String url, String methodName, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 1;
        lastUrl = url;
        lastMethodName = methodName;
        lastMap = paraMap;
        String urlTemp=url+".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, methodName, paraMap);
        // presenterImpl.loadData(url+".json",methodName,paraMap);
    }

    /**
     * @author lixiaojin
     * @createon 2018-07-17 10:23
     * @Describe 请求数据 ，带完整路径，自定义回调方法
     */
    public void tryToGetData(String url, String methodName, String errorMethodName, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 2;
        lastUrl = url;
        lastMethodName = methodName;
        lastErrorMethodName = errorMethodName;
        lastMap = paraMap;
        String urlTemp=url+".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, methodName, errorMethodName, paraMap);
        // presenterImpl.loadData(url+".json", methodName, errorMethodName, paraMap);
    }

    /**
     * @author lixiaojin
     * @createon 2018-07-17 10:23
     * @Describe 请求数据 ，带完整路径，固定回调方法
     */
    public void tryToGetData(String url, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 3;
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=url+".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, paraMap);
        // presenterImpl.loadData(url + ".json", paraMap);
    }

    /**
     * @Description: 平台2.0新请求方式
     * @Parames [url, paraMap]
     * @author 龙少
     * @date 2020/4/14
     * @version V1.0
     */
    public void HttpGetData(String url, String methodName, Map paraMap, RequestType ... requestTypes) {
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=url;
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.getData(urlTemp, methodName, paraMap);
        // presenterImpl.getData(url, methodName, paraMap);
    }

    /**
     * @Description: 平台2.0新请求方式
     * @Parames [url, paraMap]
     * @author 龙少
     * @date 2020/4/14
     * @version V1.0
     */
    public void HttpPostData(String url, String methodName, Map paraMap, RequestType ... requestTypes) {
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=url;
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.postData(urlTemp, methodName, paraMap);
        // presenterImpl.postData(url, methodName, paraMap);
    }

    /**
     * @Description: 平台2.0新请求方式
     * @Parames [url, paraMap]
     * @author 龙少
     * @date 2020/4/14
     * @version V1.0
     */
    public void HttpPutData(String url, String methodName, Map paraMap, RequestType ... requestTypes) {
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=url;
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.putData(urlTemp, methodName, paraMap);
        //presenterImpl.putData(url, methodName, paraMap);
    }

    /**
     * @author lixiaojin
     * @createon 2018-07-17 10:39
     * @Describe 请求数据，带请求方法，并自定义回调方法
     */
    public void getDataWithMethod(String url, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 4;
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, url, paraMap);
        // presenterImpl.loadData(RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json", url, paraMap);
    }

    /**
     * @author lixiaojin
     * @createon 2018-09-01 9:35
     * @Describe 上传文件
     */
    public void uploadFileWithMethod(String url, Map paraMap, MultipartBody.Part[] filePart, RequestType ... requestTypes) {
        lastReqCode = 5;
        lastUrl = url;
        lastMap = paraMap;
        lastFilePart = filePart;
        String urlTemp=RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.uploadFile(urlTemp, url, paraMap, filePart);
        // presenterImpl.uploadFile(RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json", url, paraMap, filePart);
    }


    /**
     * 文件上传，带完整地址
     *
     * @param url
     * @param paraMap
     * @param filePart
     */
    public void uploadFileWithTotalUrl(String url, Map paraMap, MultipartBody.Part[] filePart, RequestType ... requestTypes) {
        lastReqCode = 6;
        lastUrl = url;
        lastMap = paraMap;
        lastFilePart = filePart;
        String urlTemp=url+".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.uploadFile(urlTemp, url, paraMap, filePart);
        // presenterImpl.uploadFile(url + ".json", url, paraMap, filePart);
    }

    /**
     * @author lixiaojin
     * @createon 2018-07-17 10:39
     * @Describe 请求数据，带请求方法，固定回调方法
     */
    public void getDataWithCommonMethod(String url, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 7;
        lastUrl = url;
        lastMap = paraMap;
        String urlTemp=RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json";
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, paraMap);
        //presenterImpl.loadData(RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + ".json", paraMap);
    }


    /**
     * @author lixiaojin
     * @createon 2018-07-19 8:39
     * @Describe 请求数据，带请求方法和和后缀，自定义回调方法
     */
    public void getDataWithMethod(String url, String suffix, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 8;
        lastUrl = url;
        lastSuffix = suffix;
        lastMap = paraMap;
        String urlTemp=RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + suffix;
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, url, paraMap);
        // presenterImpl.loadData(RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + suffix, url, paraMap);
    }

    /**
     * @author lixiaojin
     * @createon 2018-07-19 8:39
     * @Describe 请求数据，带请求方法和和后缀，固定回调方法
     */
    public void getDataWithCommonMethod(String url, String suffix, Map paraMap, RequestType ... requestTypes) {
        lastReqCode = 9;
        lastUrl = url;
        lastSuffix = suffix;
        lastMap = paraMap;
        String urlTemp=RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + suffix;
        urlTemp=handleRequestType(urlTemp,requestTypes);
        presenterImpl.loadData(urlTemp, paraMap);
        // presenterImpl.loadData(RetrofitManager.RETROFIT_MANAGER.BASE_URL + url + suffix, paraMap);
    }


    @Override
    public void loadDataSuccess(Object tData) {
        Log.d(TAG, "loadDataSuccess");
    }

    @Override
    public void loadError(Throwable throwable) {
        Log.d(TAG, "loadDataError");
        requestError(getErrorMessage(throwable,HttpError.getErrorMessage(throwable)));
    }

    private String getErrorMessage(Throwable throwable,String message) {
        if (mSystemLogInterface != null) {
            return mSystemLogInterface.getDetailLog(throwable,message);
        } else {
            return message;
        }
    }

    @Override
    public void loadComplete() {
        Log.d(TAG, "loadDataComplete");
    }

    @Override
    public void loadSuccess(Object callBack) {
        Log.d(TAG, "loadSuccess");
        ResponseBean responseBean = (ResponseBean) callBack;
        ActionResult actionResult = responseBean.getActionResult();
        if (actionResult.getSuccess()) {
            loadDataSuccess(callBack);
            resetRequestUUID(responseBean.getRequestMineUrl()); // 改写UUID状态
        } else {
            requestError(responseBean);
        }
    }

    @Override
    public void loadSuccess(Object tData, String methodName) {
        int index = methodName.lastIndexOf("/");
        if (index < 0) {
            index = 0;
        } else {
            index++;
        }
        methodName = methodName.substring(index);
        Log.d(TAG, "methodName = " + methodName);
        ResponseBean responseBean = (ResponseBean) tData;
        ActionResult actionResult = responseBean.getActionResult();
        if (actionResult == null) {
            actionResult = new ActionResult();
            actionResult.setSuccess(false);
        }
        if (actionResult.getSuccess() || responseBean.isSuccess()) {
            resetRequestUUID(responseBean.getRequestMineUrl());
            if (methodName != null && !methodName.equals("")) {
                try {
                    Class c = this.getClass();
                    Method m1 = c.getDeclaredMethod(methodName, new Class[]{ResponseBean.class});
                    m1.invoke(this, new Object[]{responseBean});
                    Log.d(TAG, "调用自定义方法");
                } catch (Exception e) {
                    e.printStackTrace();
                    loadError(new Throwable("数据回调异常url：" + responseBean.getRequestMineUrl() + "方法名:" + methodName));
//                    showAlertDialog(this, "数据处理异常");
                }
            } else {
                showAlertDialog(this, "not found " + methodName + " method");
            }
        } else {
            requestError(responseBean);
        }
    }

    @Override
    public void loadSuccess(Object tData, String methodName, String errorMethodName) {

        ResponseBean responseBean = (ResponseBean) tData;
        ActionResult actionResult = responseBean.getActionResult();
        if (actionResult.getSuccess()) {
            int index = methodName.lastIndexOf("/");
            if (index < 0) {
                index = 0;
            } else {
                index++;
            }
            methodName = methodName.substring(index);
            Log.d(TAG, "methodName = " + methodName);
            if (methodName != null && !methodName.equals("")) {
                try {
                    Class c = this.getClass();
                    Method m1 = c.getDeclaredMethod(methodName, new Class[]{ResponseBean.class});
                    m1.invoke(this, new Object[]{responseBean});
                    Log.d(TAG, "调用自定义方法");
                } catch (Exception e) {
                    e.printStackTrace();
                    loadError(new Throwable("数据回调异常url：" + responseBean.getRequestMineUrl() + "方法名:" + methodName));
                }
            } else {
                showAlertDialog(this, "not found " + methodName + " method");
            }
            resetRequestUUID(responseBean.getRequestMineUrl());
        } else {
            int index = errorMethodName.lastIndexOf("/");
            if (index < 0) {
                index = 0;
            } else {
                index++;
            }
            errorMethodName = errorMethodName.substring(index);
            Log.d(TAG, "methodName = " + errorMethodName);
            if (errorMethodName != null && !errorMethodName.equals("")) {
                try {
                    Class c = this.getClass();
                    Method m1 = c.getDeclaredMethod(errorMethodName, new Class[]{ResponseBean.class});
                    m1.invoke(this, new Object[]{responseBean});
                    Log.d(TAG, "调用自定义方法");
                } catch (Exception e) {
                    e.printStackTrace();
                    loadError(new Throwable("数据回调异常url：" + responseBean.getRequestMineUrl() + "方法名:" + methodName));
                }
            } else {
                showAlertDialog(this, "not found " + errorMethodName + " method");
            }
        }
    }

    @Override
    public void requestError(ResponseBean responseBean) {
        if (responseBean.getActionResult() != null && responseBean.getActionResult().getMessage() != null) {
//            requestError(mSystemLogInterface.getDetailLog(new Throwable(responseBean.getRequestMineUrl()), responseBean.getActionResult().getMessage()));
            requestError(getErrorMessage(new Throwable(responseBean.getRequestMineUrl()), responseBean.getActionResult().getMessage()));
        } else if (responseBean.getMessage() != null) {
            requestError(getErrorMessage(new Throwable(responseBean.getRequestMineUrl()), responseBean.getMessage()));
        }
        if (responseBean.isTimeout()) {
            reStartApp();
        } else if (responseBean.getStatusCode() != null && responseBean.getStatusCode().equals("401")) {
            reStartApp();
        }


    }

    public void cancleRequest() {
        if (presenterImpl != null) {
            presenterImpl.unSubscribe();
        }
    }

    public void reStartApp() {
        cancleRequest();
        if (!isRestart) {
            isRestart = true;
            Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }

    @Override
    public void requestError(String message) {
        Log.e(TAG, "requestError : " + message);
        Log.e(TAG, "**********************************\r\n 当前请求信息：lastUrl = " + lastUrl);
        Log.e(TAG, "\r\n*lastMethodName = " + lastMethodName);
        Log.e(TAG, "\r\n*lastMap = " + lastMap.toString());
        Log.e(TAG, "\r\n*lastErrorMethodName = " + lastErrorMethodName);
        Log.e(TAG, "\r\n*lastFilePart = " + lastFilePart);
        Log.e(TAG, "\r\n*lastSuffix = " + lastSuffix);
        Log.e(TAG, "\r\n**********************************");
        showToast(this, message);
    }


    /*
     * @author lixiaojin
     * create on 2019-11-06 10:56
     * description: HTTP错误后重试
     */
    public void tryAgain() {
        switch (lastReqCode) {
            case 1:
                tryToGetData(lastUrl, lastMethodName, lastMap);
                break;
            case 2:
                tryToGetData(lastUrl, lastMethodName, lastErrorMethodName, lastMap);
                break;
            case 3:
                tryToGetData(lastUrl, lastMap);
                break;
            case 4:
                getDataWithMethod(lastUrl, lastMap);
                break;
            case 5:
                uploadFileWithMethod(lastUrl, lastMap, lastFilePart);
                break;
            case 6:
                uploadFileWithTotalUrl(lastUrl, lastMap, lastFilePart);
                break;
            case 7:
                getDataWithCommonMethod(lastUrl, lastMap);
                break;
            case 8:
                getDataWithMethod(lastUrl, lastSuffix, lastMap);
                break;
            case 9:
                getDataWithCommonMethod(lastUrl, lastSuffix, lastMap);
                break;
        }
    }


    /**
     * 界面跳转，不传参
     *
     * @param tClass
     */
    protected void canGo(Class<?> tClass) {
        canGo(tClass, null);
    }

    /**
     * 界面跳转，带参数
     *
     * @param tClass
     * @param bundle
     */
    protected void canGo(Class<?> tClass, Bundle bundle) {
        Intent intent = new Intent(this, tClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * @author lixiaojin
     * @create 2018-07-14
     * @Describe 跳转到目标页面并杀死当前页面
     */
    protected void canGoThenKill(Class<?> tClass) {
        canGoThenKill(tClass, null);
    }

    /**
     * @author lixiaojin
     * @create 2018-07-14
     * @Describe 跳转到目标页面，并杀死当前页面，带参数
     */
    protected void canGoThenKill(Class<?> tClass, Bundle bundle) {
        canGo(tClass, bundle);
        finish();
    }

    /**
     * @author lixiaojin
     * @create 2018-07-14
     * @Describe 跳转到目标位置，并返回结果
     */
    protected void canGoForResult(Class<?> tClass, int requestCode) {
        Intent intent = new Intent(this, tClass);
        startActivityForResult(intent, requestCode);
    }

    /**
     * @author lixiaojin
     * @create 2018-07-14
     * @Describe 带参数跳转到目标位置并返回结果，
     */
    protected void canGoForResult(Class<?> tClass, int requestCode, Bundle bundle) {
        Intent intent = new Intent(this, tClass);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /*
     * @author lixiaojin
     * create on 2019-10-21 15:51
     * description: 获取软键盘
     */
    public boolean getIsOpen() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();
    }

    /*
     * @author lixiaojin
     * create on 2019-10-21 16:00
     * description:
     */
    public void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            //强制隐藏键盘
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }


    public void hideSoftinput() {
        try {
            if (getIsOpen()) {
                hideInput();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @Description: 页面关闭后取消网络请求
     * @Parames
     * @author 龙少
     * @date 2020/3/7
     * @version V1.0
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancleRequest();
    }

    /*
     * @author lixiaojin
     * create on 2019-11-05 13:38
     * description: 处理HTTP错误
     */
    private void managerHTTPError(Throwable throwable) {

    }

    /**********----------幂等提交控制-----------------*****************/
    /*
     *RequestUUID 用于记录每个网络请求的UUID以及是否需要刷新，解决重复提交问题 拼接UUID:Android_工号_UUID
     *hashmapUUID 用于映射网络请求和RequestUUID key:请求url value:RequestUUID
     */
    class RequestUUID {
        String UUID;
        boolean idNeedFlush; // 网络返回成功后，置为true，下次相同请求需要重新生成UUID，并且更改idNeedFlush状态为false

        public String getUUID() {
            return UUID;
        }

        public void setUUID(String UUID) {
            this.UUID = UUID;
        }

        public boolean isIdNeedFlush() {
            return idNeedFlush;
        }

        public void setIdNeedFlush(boolean idNeedFlush) {
            this.idNeedFlush = idNeedFlush;
        }

        public RequestUUID(String UUID, boolean idNeedFlush) {
            this.UUID = UUID;
            this.idNeedFlush = idNeedFlush;
        }
    }

    HashMap<String,RequestUUID> hashMapUUID=new HashMap(); // 完整url映射的UUID
    HashMap<String,String> hashMapURL=new HashMap<>(); // 未拼接之前url映射拼接完的UUID


    /**
     * 生成复合UUID逻辑 Android_工号_UUID
     * @param
     * @return
     */
    private RequestUUID generateRequestUUID() {
          RequestUUID requestUUIDNew=new RequestUUID("Android_"+getMySharedPreferences().getString(ConstantUtil.loginName,"")+"_"+UUID.randomUUID().toString(),false);
          return requestUUIDNew;

    }

    /**
     * 网络请求成功后需要修改此UUID的state 为true，下次调用必须刷新
     * @param url 完整url
     */
    private void resetRequestUUID(String url) {
        Log.d("resetRequestUUID","*********");
        if(url!=null&&hashMapUUID.containsKey(url)) {
            hashMapUUID.get(url).setIdNeedFlush(true);
        }
    }

    /**
     * 子类进行传值，基类进行判断是否添加UUID或者后续其他操作
     */
    enum RequestType {
        INSERT, UPDATE, DELETE
    }

    /**
     * 此方法属于控制是否拼接uuid的核心方法，承上启下。
     * @param url 未拼接的url
     * @param types
     * @return 返回是否各种情形需要的完整url
     */
    private String  handleRequestType(String url, RequestType [] types) {
        Log.d("handleRequestType","url"+url+"typesize:"+types.length);
        if(types.length==0) {
            Log.d("handleRequestType","不需要拼接uuid");
            return  url; // 不需要添加uuid的请求原路返回url
        }
        else { //需要拼接的
           if(hashMapURL.containsKey(url)) {
                if(hashMapUUID.get(hashMapURL.get(url)).idNeedFlush){ // 需要重置路径
                    Log.d("handleRequestType","需要拼接uuid--重置UUID路径");
                    String urlFullupdate=generateFullUrl(url);
                    hashMapURL.put(url,urlFullupdate); //覆盖之前的值
                    return  urlFullupdate;

                }else { //uuid 未被消耗，也就是幂等控制生效路径
                    Log.d("handleRequestType","需要拼接uuid--幂等生效路径");
                    return hashMapURL.get(url);
                }
            }else { //第一次需要生成路径
               Log.d("handleRequestType","需要拼接uuid--第一次生成路径");
               String urlFullNew=generateFullUrl(url);
               hashMapURL.put(url,urlFullNew);
               return urlFullNew;
         }
        }
    }

    /**
     * 根据部分url拼接完整url
     * @param urlPart
     * @return
     */
    private String generateFullUrl(String urlPart){
        RequestUUID requestUUID=generateRequestUUID();
        String urlFull=urlPart+"billuid?"+requestUUID.getUUID();
        hashMapURL.put(urlPart,urlFull);
        hashMapUUID.put(urlFull,requestUUID); // 每次生成UUID都要去覆盖或者推入此hashMap
        return urlFull;
    }

}

