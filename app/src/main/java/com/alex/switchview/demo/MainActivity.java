package com.alex.switchview.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.alex.baseui.BaseActivity;
import org.alex.switchview.SwitchView;
import org.alex.util.LogUtil;

public class MainActivity extends BaseActivity {
    private SwitchView switchView;
    private Button button;

    @Override
    public int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onCreateData(Bundle bundle) {
        setOnClickListener(R.id.bt);
        button = findView(R.id.bt);
        switchView = findView(R.id.sw);
        button.setText(switchView.isOpened() ? "状态 = 开" : "状态 = 关");
        switchView.onChangeListener(new MyOnChangeListener());
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (R.id.bt == v.getId()) {
            boolean isOpened = switchView.isOpened();
            if (isOpened) {
                switchView.close();
            } else {
                switchView.open();
            }
        }
    }

    private final class MyOnChangeListener implements SwitchView.OnChangeListener {

        @Override
        public void onChange(SwitchView switchView, boolean isOpened) {
            LogUtil.e("isOpened = " + isOpened);
            button.setText(isOpened ? "状态 = 开" : "状态 = 关");
        }
    }
}
