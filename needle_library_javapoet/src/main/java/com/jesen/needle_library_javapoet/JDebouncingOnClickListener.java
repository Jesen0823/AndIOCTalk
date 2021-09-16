package com.jesen.needle_library_javapoet;

import android.view.View;

/**
 * 点击事件封装抽象类
 */
public abstract class JDebouncingOnClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
        // 调用抽象方法
        doClick(v);
    }

    // 抽象方法，子类new必须实现该方法
    protected abstract void doClick(View v);
}