package com.jesen.needle_compiler_javapoet.utils;

public class JConstants {

    // 注解处理器中支持的注解类型
    public static final String BINDVIEW_ANNOTATION_TYPES = "com.jesen.needle.annotation_javapoet.JBindView";
    public static final String ONCLICK_ANNOTATION_TYPES = "com.jesen.needle.annotation_javapoet.JOnClick";

    // 布局、控件绑定实现接口
    public static final String VIEWBINDER = "com.jesen.needle_library_javapoet.JViewBinder";

    public static final String CLICKLISTENER = "com.jesen.needle_library_javapoet.JDebouncingOnClickListener";

    public static final String VIEW = "android.view.View";

    public static final String CLASS_END = "$JViewBinder";

    // bind方法名
    public static final String BIND_METHOD_NAME = "bind";

    // bind方法的参数名target
    public static final String TARGET_PARAMETER_NAME = "target";
}
