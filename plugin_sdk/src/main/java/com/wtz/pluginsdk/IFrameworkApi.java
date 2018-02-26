package com.wtz.pluginsdk;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by WTZ on 2018/2/9.
 */

public interface IFrameworkApi {

    void init(Context context, Map<String, Object> params, IListener listener);

    void destroy();

    boolean isInitSuccessful();

    boolean isIniting();

    void openPlugin(Context context, String pluginName, HashMap<String, Serializable> params, int requestCode, IListener listener);

    boolean updatePlugin(Context context, String pluginName, IListener listener);

}
