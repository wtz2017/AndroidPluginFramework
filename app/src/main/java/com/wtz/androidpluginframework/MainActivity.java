package com.wtz.androidpluginframework;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.wtz.pluginsdk.IListener;
import com.wtz.pluginsdk.PluginApi;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.proxy_activity_main);

        Button button1 = (Button) findViewById(R.id.button1);
        Button button2 = (Button) findViewById(R.id.button2);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

        initPluginFramework();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        PluginApi.instance().destroy();
        super.onDestroy();
    }

    private void initPluginFramework() {
        if (PluginApi.instance().isIniting() || PluginApi.instance().isInitSuccessful()) {
            return;
        }

        Map<String, Object> params = new HashMap<String, Object>();
        params.put(PluginApi.PROXY_ACTIVITY, ProxyActivity.class);
        PluginApi.instance().init(this, params, new IListener() {
            @Override
            public void onResult(boolean successful, String info) {
                Log.d(TAG, "init onResult: successful = " + successful
                        + ", info = " + info);

            }
        });
    }

    @Override
    public void onClick(View v) {
        int resId = v.getId();
        switch (resId) {
            case R.id.button1:
                Log.d(TAG, "onClick:button1");
                if (!canOperate()) {
                   return;
                }
                // update plugin
                PluginApi.instance().updatePlugin(this, "plugin1", new IListener() {
                    @Override
                    public void onResult(boolean successful, String info) {
                        Log.d(TAG, "updatePlugin:" + successful + " " + info);
                        Toast.makeText(MainActivity.this, "updatePlugin:" + successful, Toast.LENGTH_LONG).show();
                    }
                });
                break;
            case R.id.button2:
                Log.d(TAG, "onClick:button2");
                if (!canOperate()) {
                    return;
                }
                // open plugin
                PluginApi.instance().openPlugin(this, "plugin1", null, -1, new IListener() {
                    @Override
                    public void onResult(boolean successful, String info) {
                        Log.d(TAG, "openPlugin:" + successful + " " + info);
                        Toast.makeText(MainActivity.this, "openPlugin:" + successful, Toast.LENGTH_LONG).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    private boolean canOperate() {
        if (PluginApi.instance().isIniting()) {
            Log.d(TAG, "is initing");
            Toast.makeText(this, "is initing", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!PluginApi.instance().isInitSuccessful()) {
            Log.d(TAG, "not init successful");
            Toast.makeText(this, "not init successful", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }
}
