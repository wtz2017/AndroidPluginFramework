package com.wtz.plugin_framework;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.wtz.pluginsdk.IFrameworkApi;
import com.wtz.pluginsdk.IListener;
import com.wtz.pluginsdk.PluginApi;
import com.wtz.pluginsdk.util.FileUtil;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.wtz.pluginsdk.PluginApi.getSoPath;

/**
 * Created by WTZ on 2018/2/9.
 */

public class PluginFramework implements IFrameworkApi {
    private final static String TAG = "PluginFramework";

    private static PluginFramework mInstance;

    private boolean isIniting;
    private boolean isInitSuccessful;

    private Class mProxyActivity;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private PluginFramework() {
    }

    public static PluginFramework instance() {
        if (mInstance == null) {
            synchronized (PluginFramework.class) {
                if (mInstance == null) {
                    mInstance = new PluginFramework();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void init(Context context, Map<String, Object> params, final IListener listener) {
        if (isIniting || isInitSuccessful) {
            Log.d(TAG, "isIniting: " + isIniting + ", isInitSuccessful: " + isInitSuccessful);
            return;
        }
        isIniting = true;

        if (params == null || !params.containsKey(PluginApi.PROXY_ACTIVITY)) {
            Log.d(TAG, "find not proxy activity");
            initFailed(listener, "find not proxy activity");
            return;
        }
        mProxyActivity = (Class) params.get(PluginApi.PROXY_ACTIVITY);

        boolean ret = updatePlugin(context, "plugin1", null);
        if (!ret) {
            Log.d(TAG, "update plugin failed");
            initFailed(listener, "update plugin failed");
            return;
        }

        isIniting = false;
        isInitSuccessful = true;
        Log.d(TAG, "init successfull");
        if (listener != null) {
            postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(true, "pluginFramework init successfull");
                }
            });
        }
    }

    private void initFailed(final IListener listener, final String err) {
        isIniting = false;
        isInitSuccessful = false;
        if (listener == null) {
            return;
        }
        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                listener.onResult(false, err);
            }
        });
    }

    @Override
    public void destroy() {
        isIniting = false;
        isInitSuccessful = false;
    }

    @Override
    public boolean isInitSuccessful() {
        return isInitSuccessful;
    }

    @Override
    public boolean isIniting() {
        return isIniting;
    }

    @Override
    public void openPlugin(Context context, String pluginName, HashMap<String, Serializable> params, int requestCode, final IListener listener) {
        if (context == null) {
            openPluginFailed(listener, "context == null");
            return;
        }
        if (TextUtils.isEmpty(pluginName)) {
            openPluginFailed(listener, "pluginName is empty");
            return;
        }

        File pluginFile = new File(PluginApi.getPluginDirPath(context), pluginName + ".apk");
        if (!pluginFile.exists()) {
            openPluginFailed(listener, pluginFile.getAbsolutePath() + " does not exists");
            return;
        }

        Intent intent = new Intent(context, mProxyActivity);
        intent.setPackage(context.getPackageName());
        intent.putExtra(PluginApi.PLUGIN_NAME, pluginName);
        intent.putExtra(PluginApi.PLUGIN_PARAMS, params);
        try {
            if (requestCode < 0 || !(context instanceof Activity)) {
                Log.d(TAG, "startActivity: " + pluginName);
                context.startActivity(intent);
            } else {
                Log.d(TAG, "startActivityForResult: " + pluginName + ", requestCode: " + requestCode);
                Activity act = (Activity) context;
                act.startActivityForResult(intent, requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            openPluginFailed(listener, pluginFile.getAbsolutePath() + "startActivity failed: " + e.getMessage());
            return;
        }

        Log.d(TAG, "open plugin successful");
        if (listener != null) {
            postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(true, "open plugin successful");
                }
            });
        }
    }

    private void openPluginFailed(final IListener listener, final String err) {
        Log.d(TAG, "openPluginFailed: " + err);
        if (listener == null) {
            return;
        }
        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                listener.onResult(false, err);
            }
        });
    }

    @Override
    public boolean updatePlugin(Context context, String pluginName, final IListener listener) {
        String sourceDir = "/sdcard/plugins";
        String destDir = PluginApi.getPluginDirPath(context);
        File sourceFile = new File(sourceDir, pluginName + ".apk");
        File destFile = new File(destDir, pluginName + ".apk");
        Log.d(TAG, "begin to get plugin from local: " + sourceFile.getAbsolutePath()
                + " to " + destFile.getAbsolutePath());

        if (!FileUtil.copy(sourceFile, destFile)) {
            Log.d(TAG, "copy plugin failed");
            updatePluginFailed(listener, "copy plugin failed");
            return false;
        }

        // 删除旧的dex、oat、so文件
        deleteOldBin(context, pluginName);

        File soDir = PluginApi.getSoPath(context, pluginName);
        FileUtil.saveSoFromApk(destFile.getAbsolutePath(), soDir.getAbsolutePath());

        Log.d(TAG, "update pulugin successful");
        if (listener != null) {
            postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(true, "update pulugin successful");
                }
            });
        }
        return true;
    }

    private void updatePluginFailed(final IListener listener, final String err) {
        if (listener == null) {
            return;
        }
        postOnMainThread(new Runnable() {
            @Override
            public void run() {
                listener.onResult(false, err);
            }
        });
    }

    private void deleteOldBin(Context context, String pluginName) {
        File dexDir = PluginApi.getDexPath(context);
        File soDir = getSoPath(context, pluginName);

        FileUtil.deleteFile(new File(dexDir, pluginName + ".dex"));
        FileUtil.deleteFile(new File(dexDir, pluginName + ".oat"));
        FileUtil.deleteFile(soDir);
    }

    private void postOnMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
