package com.wtz.plugin;

import android.os.Bundle;
import android.util.Log;

import com.wtz.pluginsdk.PluginBaseActivity;

public class MainActivity extends PluginBaseActivity {
    private final static String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.plugin1_main);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }
}
