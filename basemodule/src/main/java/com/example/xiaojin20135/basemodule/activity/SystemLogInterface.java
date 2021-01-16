package com.example.xiaojin20135.basemodule.activity;

import com.alibaba.android.arouter.facade.template.IProvider;

/**
 * @author 张龙飞1
 */
public interface SystemLogInterface extends IProvider {

    /**
     * @return 获取详细错误接口
     */
    String getDetailLog(Throwable throwable);
  /**
     * @return 获取详细错误接口带信息的
     */
    String getDetailLog(Throwable throwable,String message);

}
