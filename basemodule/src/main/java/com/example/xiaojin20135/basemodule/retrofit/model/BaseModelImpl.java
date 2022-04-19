package com.example.xiaojin20135.basemodule.retrofit.model;

import android.content.Context;
import android.util.Log;

import com.example.xiaojin20135.basemodule.retrofit.api.IServiceApi;
import com.example.xiaojin20135.basemodule.retrofit.bean.ResponseBean;
import com.google.gson.Gson;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by lixiaojin on 2018-07-12.
 * 功能描述：
 */

public class BaseModelImpl extends BaseModel implements IBaseModel<ResponseBean> {
    private final static String TAG = "BaseModelImpl";
    private Context context;
    private IServiceApi iServiceApi;
    private CompositeDisposable compositeDisposable;
    private Gson gson;
    //是否需要重试，应该设置为接口参数可配置，由具体的业务自行决定,此处为临时方案
    private final int RETRY_TIMES = 3;//重试次数
    private final int RETRY_TIMES_DELAY = 500;//延迟重试间隔
    private final String EXCEPTION_RETRY = "exception_retry";//重试，不需要延迟

    public BaseModelImpl(Context context){
        this.context = context;
        iServiceApi = retrofitManager.getService ();
        compositeDisposable = new CompositeDisposable();
        gson=new Gson();
    }


    @Override
    public void loadData (final String url, final String methodName, Map<String, String> paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    return iServiceApi.load(url, paraMap);
                } )
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }

                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result,methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

    @Override
    public void getData(final String url, final String methodName, Map<String, String> paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> iServiceApi.get(url, paraMap))
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                })).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result,methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

    @Override
    public void postData(final String url, final String methodName, Map paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        String obj=gson.toJson(paraMap);
        final RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),obj);
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> iServiceApi.post(url, body))
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                })).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result,methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

    @Override
    public void putData(final String url, final String methodName, Map paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        String obj=gson.toJson(paraMap);
        RequestBody body=RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),obj);
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> iServiceApi.put(url, body))
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                })).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result,methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

    @Override
    public void loadData (final String url, final String methodName, final String errorMethodName, Map<String, String> paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        Log.d (TAG,"paraMap = " + paraMap.toString ());
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> iServiceApi.load(url,paraMap))
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                })).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result,methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }
    @Override
    public void loadData (final String url, Map<String, String> paraMap, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        Log.e(TAG, "接口调用前：" + new Gson().toJson(paraMap) + "==" + url);
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> {
                    Log.e(TAG, "接口调用时：" + new Gson().toJson(paraMap) + "==" + url);
                    return iServiceApi.load(url,paraMap);
                })
                .map( responseBean -> {
                    Log.d (TAG,"response = " + new Gson().toJson(responseBean).toString ());
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            Log.e(TAG, "接口返回结果：" + new Gson().toJson(result));
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

   
    @Override
    public void upload (final String url, final String methodName, Map<String, RequestBody> paraMap, MultipartBody.Part[] filePart, final IBaseRequestCallBack<ResponseBean> iBaseRequestCallBack) {
        iBaseRequestCallBack.beforeRequest ();
        AtomicInteger currentRetryTime = new AtomicInteger();
        Log.d (TAG,"paraMap = " + paraMap.toString ());
        Log.d (TAG,"filePart = " + filePart.toString ());
        compositeDisposable.add(Observable.just("")
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .flatMap(s -> iServiceApi.uploadFile (url,paraMap,filePart))
                .map( responseBean -> {
                    if (isNeedTry(responseBean.getStatusCode())) {
                        throw new Exception(EXCEPTION_RETRY);
                    } else {
                        return responseBean;
                    }
                }).retryWhen(observable -> observable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                    currentRetryTime.getAndIncrement();
                    if (isNeedTryDelay(new Gson().toJson(throwable)) && currentRetryTime.get() < RETRY_TIMES) {
                        return Observable.timer(RETRY_TIMES_DELAY, TimeUnit.MILLISECONDS);
                    } else if (EXCEPTION_RETRY.equals(throwable.getMessage()) && currentRetryTime.get() < RETRY_TIMES){
                        return Observable.just("");
                    } else {
                        throw new Exception("接口调用失败");
                    }
                })).observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            result.setRequestMineUrl(url);
                            iBaseRequestCallBack.requestSuccess (result, methodName);
                            iBaseRequestCallBack.requestComplete ();
                        },
                        throwable -> {
                            iBaseRequestCallBack.requestError (new Throwable("错误信息:"+url,throwable));
                        }
                ));
    }

    @Override
    public void onUnsubscribe () {
        compositeDisposable.clear();
    }

    /**
     * 500：服务器内部错误（用户权限、数据库连接等问题）
     * 503：服务器暂停
     * 504：网关超时
     * @param errMsg 服务器返回错误信息
     * @return
     */
    private boolean isNeedTryDelay(String errMsg) {
        return errMsg != null && (errMsg.contains("500") || errMsg.contains("503") || errMsg.contains("504"));
    }

    /**
     * -1001：服务器请求超时
     * -1004：服务器连接失败
     * -1005：服务器连接被中断
     * @param code
     * @return
     */
    private boolean isNeedTry(String code) {
        return "-1001".equals(code) || "-1004".equals(code) || "-1005".equals(code);
    }
}
