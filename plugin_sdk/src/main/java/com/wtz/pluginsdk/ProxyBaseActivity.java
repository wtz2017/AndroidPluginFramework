package com.wtz.pluginsdk;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import dalvik.system.DexClassLoader;


public class ProxyBaseActivity extends FragmentActivity {

    private static final String TAG = "ProxyBaseActivity";

    private String mPluginName;
    private HashMap<String, Serializable> mParams;

    private AssetManager mAssetManager;
    private Resources mResources;
    private Theme mTheme;

    private ClassLoader mClassLoader;
    private PluginBaseActivity mRemote;
    private HashMap<String, Method> mLifeMethods = new HashMap<String, Method>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null && !bundle.containsKey(PluginApi.PLUGIN_NAME)) {
            Log.d(TAG, "find not plugin name key");
            finish();
            return;
        }

        Object o = bundle.get(PluginApi.PLUGIN_NAME);
        if (o == null || !(o instanceof String)) {
            Log.d(TAG, "bundle get plugin name failed");
            finish();
            return;
        }

        mPluginName = (String) o;

        o = bundle.get(PluginApi.PLUGIN_PARAMS);
        if (o != null && o instanceof HashMap) {
            mParams = (HashMap) bundle.get(PluginApi.PLUGIN_PARAMS);
        }

        File dexFile = new File(PluginApi.getPluginDirPath(this), mPluginName + ".apk");
        if (!dexFile.exists()) {
            Log.d(TAG, "dexFile does not exists");
            finish();
            return;
        }

        Log.d(TAG, "onCreate plugin name = " + mPluginName +
                ", file: " + dexFile.getAbsolutePath());

        loadRemoteResources(dexFile);

        loadRemoteActivity(dexFile);

        onCreateRemoteActivity(savedInstanceState);
    }

    protected void loadRemoteResources(File dexFile) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexFile.getAbsolutePath());
            mAssetManager = assetManager;
        } catch (Exception e) {
            finish();
            e.printStackTrace();
        }
        Resources superRes = super.getResources();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
    }

    protected void loadRemoteActivity(File dexFile) {
        mClassLoader = new DexClassLoader(dexFile.getAbsolutePath(), PluginApi.getDexPath(this).getAbsolutePath(), null, this.getClassLoader());

        if (mClassLoader == null) {
            Log.d(TAG, "mClassLoader is null");
            finish();
            return;
        }

        ActivityInfo actInfo = PluginApi.getEntryActivityInfo(this, dexFile.getAbsolutePath());
        if (actInfo == null || TextUtils.isEmpty(actInfo.name)) {
            Log.d(TAG, "ActivityInfo is null");
            finish();
            return;
        }

        Log.d(TAG, "actInfo.name: " + actInfo.name);
        try {
            Class<?> mainClass = mClassLoader.loadClass(actInfo.name);
            Constructor<?> constructor = mainClass.getConstructor(new Class[]{});
            Object instance = constructor.newInstance(new Object[]{});
            if (instance == null) {
                Log.d(TAG, "instance is null");
                finish();
                return;
            }

            mRemote = (PluginBaseActivity) instance;
            mRemote.setRealActivity(this, mParams);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public AssetManager getAssets() {
        Log.d(TAG, "getAssets: mAssetManager=" + mAssetManager);
        return mAssetManager == null ? super.getAssets() : mAssetManager;
    }

    @Override
    public Resources getResources() {
        Log.d(TAG, "getResources: mResources=" + mResources);
        return mResources == null ? super.getResources() : mResources;
    }

    @Override
    public Theme getTheme() {
        Log.d(TAG, "getTheme: mTheme=" + mTheme);
        return mTheme == null ? super.getTheme() : mTheme;
    }

    private void onCreateRemoteActivity(Bundle savedInstanceState) {
        mRemote.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        mRemote.onStart();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart");
        super.onRestart();
        mRemote.onRestart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mRemote.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        mRemote.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        mRemote.onPause();
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState");
        mRemote.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        mRemote.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        mRemote.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult resultCode = " + resultCode);
        mRemote.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish");
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown: keyCode = " + keyCode + ", action = " + event.getAction());
        boolean ret = mRemote.onKeyDown(keyCode, event);
        return ret || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyUp: keyCode = " + keyCode + ", action = " + event.getAction());
        boolean ret = mRemote.onKeyUp(keyCode, event);
        return ret || super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyLongPress: keyCode = " + keyCode + ", action = " + event.getAction());
        boolean ret = mRemote.onKeyLongPress(keyCode, event);
        return ret || super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent: action = " + event.getAction());
        boolean ret = mRemote.onTouchEvent(event);
        return ret || super.onTouchEvent(event);
    }

}
