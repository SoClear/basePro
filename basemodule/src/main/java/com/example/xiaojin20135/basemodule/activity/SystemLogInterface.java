package com.example.xiaojin20135.basemodule.activity;

/**
 * @author 张龙飞1
 */
public interface SystemLogInterface {

    /**
     * @return 获取详细错误接口
     */
    String getDetailLog(Throwable throwable);
  /**
     * @return 获取详细错误接口带信息的
     */
    String getDetailLog(Throwable throwable,String message);

}
