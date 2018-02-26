package com.wtz.pluginsdk;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.wtz.pluginsdk.util.FileUtil;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Created by WTZ on 2018/2/9.
 */

public class PluginApi {
    private final static String TAG = "PluginApi";

    private static PluginApi mInstance;

    private final static String CLASS_FRAMEWORK_API= "com.wtz.plugin_framework.PluginFramework";
    private final static String FRAMEWORK_DIR = "framework";
    private final static String FRAMEWORK_JAR_NAME = "framework";
    private final static String FRAMEWORK_JAR_SUFFIX= ".jar";
    private final static String PLUGIN_DIR = "plugins";

    public final static String PLUGIN_NAME = "plugin_name";
    public final static String PLUGIN_PARAMS = "plugin_params";
    public final static String PROXY_ACTIVITY = "proxy_activity";

    private IFrameworkApi mFrameworkApi;

    private boolean isIniting;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private PluginApi() {
    }

    public static PluginApi instance() {
        if (mInstance == null) {
            synchronized (PluginApi.class) {
                if (mInstance == null) {
                    mInstance = new PluginApi();
                }
            }
        }
        return mInstance;
    }

    public void init(final Context context, final Map<String, Object> params, final IListener listener) {
        if (mFrameworkApi != null) {
            if (mFrameworkApi.isIniting() || mFrameworkApi.isInitSuccessful()) {
                Log.d(TAG, "is initing or init successfull");
                return;
            }
            mFrameworkApi.init(context, params, listener);
        } else {
            if (isIniting) {
                Log.d(TAG, "is initing");
                return;
            }
            isIniting = true;
            updateFramework(context, new IListener() {
                @Override
                public void onResult(boolean successful, final String info) {
                    Log.d(TAG, "updateFramework...onResult:" + successful + " " + info);
                    if (successful) {
                        mFrameworkApi.init(context, params, listener);
                    } else {
                       if (listener != null) {
                           postOnMainThread(new Runnable() {
                               @Override
                               public void run() {
                                   listener.onResult(false, info);
                               }
                           });
                       }
                    }
                }
            });
        }
    }

    public void destroy() {
        if (mFrameworkApi != null) {
            mFrameworkApi.destroy();
            mFrameworkApi = null;
        }
        isIniting = false;
    }

    public boolean isInitSuccessful() {
        if (mFrameworkApi != null) {
            return mFrameworkApi.isInitSuccessful();
        }
        return false;
    }

    public boolean isIniting() {
        if (mFrameworkApi != null) {
            return mFrameworkApi.isIniting();
        }
        return isIniting;
    }

    public void openPlugin(Context context, String pluginName, HashMap<String, Serializable> params, int requestCode, IListener listener) {
        if (mFrameworkApi != null) {
            mFrameworkApi.openPlugin(context, pluginName, params, requestCode, listener);
        } else {
            if (listener != null) {
                listener.onResult(false, "mFrameworkApi is null");
            }
        }
    }

    public void updatePlugin(Context context, String pluginName, IListener listener) {
        if (mFrameworkApi != null) {
            mFrameworkApi.updatePlugin(context, pluginName, listener);
        } else {
            if (listener != null) {
                listener.onResult(false, "mFrameworkApi is null");
            }
        }
    }

    private void updateFramework(Context context, final IListener listener) {
        if (context == null) {
            updateFrameworkFailed(listener, "context is null");
            return;
        }

        String sourceJarDir = "/sdcard/framework";
        String destJarDir = getFrameworkDirPath(context);
        File sourceFile = new File(sourceJarDir, FRAMEWORK_JAR_NAME + FRAMEWORK_JAR_SUFFIX);
        File destFile = new File(destJarDir, FRAMEWORK_JAR_NAME + FRAMEWORK_JAR_SUFFIX);
        Log.d(TAG, "begin to get jar from local: " + sourceFile.getAbsolutePath()
                + " to " + destFile.getAbsolutePath());

        if (!FileUtil.copy(sourceFile, destFile)) {
            updateFrameworkFailed(listener, "copy jar failed");
            return;
        }

        // 删除旧的dex、oat、so文件
        deleteOldBin(context);

        File soDir = getSoPath(context, FRAMEWORK_JAR_NAME);
        FileUtil.saveSoFromApk(destFile.getAbsolutePath(), soDir.getAbsolutePath());

        DexClassLoader cl = new DexClassLoader(destFile.getAbsolutePath(), getDexPath(context).getAbsolutePath(), null, context.getClassLoader());
        boolean loadSuccessful = false;
        try {
            Class frameworkApi = cl.loadClass(CLASS_FRAMEWORK_API);
            Method instanceMethod = frameworkApi.getMethod("instance", new Class[]{});
            mFrameworkApi = (IFrameworkApi) instanceMethod.invoke(frameworkApi, new Object[] {});
            loadSuccessful = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (!loadSuccessful || mFrameworkApi == null) {
            updateFrameworkFailed(listener, "load jar failed");
            return;
        }

        isIniting = false;
        Log.d(TAG, "load jar successful");
        if (listener != null) {
            postOnMainThread(new Runnable() {
                @Override
                public void run() {
                    listener.onResult(true, "load jar successful");
                }
            });
        }
    }

    private void deleteOldBin(Context context) {
        File dexDir = getDexPath(context);
        File soDir = getSoPath(context, FRAMEWORK_JAR_NAME);

        FileUtil.deleteFile(new File(dexDir, FRAMEWORK_JAR_NAME + ".dex"));
        FileUtil.deleteFile(new File(dexDir, FRAMEWORK_JAR_NAME + ".oat"));
        FileUtil.deleteFile(soDir);
    }

    public static String getFrameworkDirPath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + FRAMEWORK_DIR;
    }

    public static String getPluginDirPath(Context context) {
        return context.getFilesDir().getAbsolutePath() + File.separator + PLUGIN_DIR;
    }

    public static File getDexPath(Context context) {
        return context.getDir("dex", Context.MODE_PRIVATE);
    }

    public static File getSoPath(Context context, String name) {
        return context.getDir("so_" + name, Context.MODE_PRIVATE);
    }

    /**
     * 约定第一个注册的Activiy为入口Activity
     *
     * @param context
     * @param apkPath
     * @return
     */
    public static ActivityInfo getEntryActivityInfo(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = null;

        try {
            pkgInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (pkgInfo == null || pkgInfo.activities == null || pkgInfo.activities.length == 0) {
            return null;
        }

        return pkgInfo.activities[0];
    }

    private void updateFrameworkFailed(final IListener listener, final String err) {
        Log.d(TAG, "updateFrameworkFailed: " + err);
        isIniting = false;
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

    private void postOnMainThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
