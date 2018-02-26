package com.wtz.pluginsdk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by WTZ on 2018/2/11.
 */

public class PluginBaseActivity extends FragmentActivity {

    private Activity mRealActivity;
    private HashMap<String, Serializable> mParams;

    public void setRealActivity(Activity activity, HashMap<String, Serializable> params) {
        mRealActivity = activity;
        mParams = params;
    }

    protected Activity getRealActivity() {
        return mRealActivity;
    }

    @Override
    public void setContentView( int layoutResID) {
        mRealActivity.setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        mRealActivity.setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mRealActivity.setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        mRealActivity.addContentView(view, params);
    }

    @Override
    public <T extends View> T findViewById(int id) {
        return mRealActivity.findViewById(id);
    }

    @Override
    public Resources getResources() {
        return mRealActivity.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return mRealActivity.getAssets();
    }

    @Override
    public Resources.Theme getTheme() {
        return mRealActivity.getTheme();
    }

    @Override
    public FragmentManager getSupportFragmentManager() {
        if (mRealActivity instanceof FragmentActivity) {
            return ((FragmentActivity) mRealActivity).getSupportFragmentManager();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    }

    @Override
    protected void onNewIntent(Intent intent) {
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onRestart() {
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    }

    @Override
    protected void onResume() {
    }

    @Override
    protected void onPause() {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onStop() {
    }

    @Override
    protected void onDestroy() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void finish() {
        mRealActivity.finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }
}
