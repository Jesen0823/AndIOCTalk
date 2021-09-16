package com.jesen.needle_compiler_javapoet;

import com.google.auto.service.AutoService;
import com.jesen.needle.annotation_javapoet.JBindView;
import com.jesen.needle.annotation_javapoet.JOnClick;
import com.jesen.needle_compiler_javapoet.utils.EmptyUtils;
import com.jesen.needle_compiler_javapoet.utils.JConstants;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes({JConstants.BINDVIEW_ANNOTATION_TYPES, JConstants.ONCLICK_ANNOTATION_TYPES})
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ButterNeedlePoetProcess extends AbstractProcessor {

    // 操作Element工具类 (类、函数、属性都是Element)
    private Elements elementUtils;

    // type(类信息)工具类，包含用于操作TypeMirror的工具方法
    private Types typeUtils;

    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;

    // 文件生成器 类/资源，Filter用来创建新的类文件，class文件以及辅助文件
    private Filer filer;

    // key:类节点, value:被@BindView注解的属性集合
    private Map<TypeElement, List<VariableElement>> tempBindViewMap = new HashMap<>();

    // key:类节点, value:被@OnClick注解的方法集合
    private Map<TypeElement, List<ExecutableElement>> tempOnClickMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        // 初始化
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        messager.printMessage(Diagnostic.Kind.NOTE,
                "javapoet 注解处理器 初始化完成，开始处理注解----------->");
    }


    /**
     * 相当于main函数，开始处理注解
     * 注解处理器的核心方法，处理具体的注解，生成Java文件
     *
     * @param annotations 使用了支持处理注解的节点集合
     * @param roundEnv    当前或是之前的运行环境,可以通过该对象查找的注解。
     * @return true 表示后续处理器不会再处理（已经处理完成）
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 一旦有属性上使用@BindView注解
        if (!EmptyUtils.isEmpty(annotations)) {
            // 获取所有被 @BindView 注解的 元素集合
            Set<? extends Element> bindViewElements = roundEnv.getElementsAnnotatedWith(JBindView.class);
            // 获取所有被 @OnClick 注解的 元素集合
            Set<? extends Element> onClickElements = roundEnv.getElementsAnnotatedWith(JOnClick.class);

            if (!EmptyUtils.isEmpty(bindViewElements) || !EmptyUtils.isEmpty(onClickElements)) {
                try {
                    // 赋值临时map存储，用来存放被注解的属性集合
                    valueOfMap(bindViewElements, onClickElements);
                    // 生成类文件，如：
                    createJavaFile();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private void valueOfMap(Set<? extends Element> bindViewElements, Set<? extends Element> onClickElements) {
        if (!EmptyUtils.isEmpty(bindViewElements)) {
            for (Element element : bindViewElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@JBindView -----" + element.getSimpleName());
                if (element.getKind() == ElementKind.FIELD) {
                    VariableElement fieldElement = (VariableElement) element;
                    // 注解在属性之上，属性节点父节点是类节点
                    TypeElement enclosingElement = (TypeElement) fieldElement.getEnclosingElement();
                    // 如果map集合中的key：类节点存在，直接添加属性
                    if (tempBindViewMap.containsKey(enclosingElement)) {
                        tempBindViewMap.get(enclosingElement).add(fieldElement);
                    } else {
                        List<VariableElement> fields = new ArrayList<>();
                        fields.add(fieldElement);
                        tempBindViewMap.put(enclosingElement, fields);
                    }
                }
            }
        }

        if (!EmptyUtils.isEmpty(onClickElements)) {
            for (Element element : onClickElements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "@JOnClick -----" + element.getSimpleName());
                if (element.getKind() == ElementKind.METHOD) {
                    ExecutableElement methodElement = (ExecutableElement) element;
                    // 注解在属性之上，属性节点父节点是类节点
                    TypeElement enclosingElement = (TypeElement) methodElement.getEnclosingElement();
                    // 如果map集合中的key：类节点存在，直接添加属性
                    if (tempOnClickMap.containsKey(enclosingElement)) {
                        tempOnClickMap.get(enclosingElement).add(methodElement);
                    } else {
                        List<ExecutableElement> fields = new ArrayList<>();
                        fields.add(methodElement);
                        tempOnClickMap.put(enclosingElement, fields);
                    }
                }
            }
        }
    }

    private void createJavaFile() throws IOException {
        // 判断是否有需要生成的类文件
        if (!EmptyUtils.isEmpty(tempBindViewMap)) {
            // 获取ViewBinder接口类型（生成类文件需要实现的接口）
            TypeElement viewBinderType = elementUtils.getTypeElement(JConstants.VIEWBINDER);
            TypeElement clickListenerType = elementUtils.getTypeElement(JConstants.CLICKLISTENER);
            TypeElement viewType = elementUtils.getTypeElement(JConstants.VIEW);

            for (Map.Entry<TypeElement, List<VariableElement>> entry : tempBindViewMap.entrySet()) {

                // 类名
                ClassName className = ClassName.get(entry.getKey());

                // 接口泛型 [implements JViewBinder<MainActivity>]
                ParameterizedTypeName typeName = ParameterizedTypeName.get(
                        ClassName.get(viewBinderType),
                        ClassName.get(entry.getKey())
                );

                // 方法的参数
                ParameterSpec parameterSpec = ParameterSpec.builder(
                        ClassName.get(entry.getKey()), // [参数类型 MainActivity]
                        JConstants.TARGET_PARAMETER_NAME  // [参数名 target]
                )
                        .addModifiers(Modifier.FINAL) // [修饰符 final]
                        .build();  // 参数构建完成

                // 方法体 [public void build(Activity activity)]
                MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(JConstants.BIND_METHOD_NAME)
                        .addAnnotation(Override.class) // 重写方法符号[@ Override]
                        .addModifiers(Modifier.PUBLIC) // [public]
                        .addParameter(parameterSpec); // 方法参数

                // 开始构建方法的内容
                for (VariableElement variableElement : entry.getValue()) {
                    // 获取属性名字
                    String filedName = variableElement.getSimpleName().toString();
                    // 获取@JBindView注解的值
                    int annotationValue = variableElement.getAnnotation(JBindView.class).value();
                    String methodContent = "$N." + filedName + " = $N.findViewById($L)";
                    methodBuilder.addStatement(methodContent,
                            JConstants.TARGET_PARAMETER_NAME, // target
                            JConstants.TARGET_PARAMETER_NAME, // target
                            annotationValue); // R.id.xxx
                }

                if (!EmptyUtils.isEmpty(tempOnClickMap)) {
                    for (Map.Entry<TypeElement, List<ExecutableElement>> methodEntry : tempOnClickMap.entrySet()) {
                        // 类名
                        if (className.equals(ClassName.get(entry.getKey()))) {
                            for (ExecutableElement methodElement : methodEntry.getValue()) {
                                // 获取方法名
                                String methodName = methodElement.getSimpleName().toString();
                                // 获取@OnClick注解的值
                                int annotationValue = methodElement.getAnnotation(JOnClick.class).value();
                                /**
                                 * target.findViewById(2131165312).setOnClickListener(new DebouncingOnClickListener() {
                                 *      public void doClick(View view) {
                                 *          target.click(view);
                                 *      }
                                 * });
                                 */
                                methodBuilder.beginControlFlow("$N.findViewById($L).setOnClickListener(new $T()",
                                        JConstants.TARGET_PARAMETER_NAME, annotationValue, ClassName.get(clickListenerType))
                                        .beginControlFlow("public void doClick($T view)", ClassName.get(viewType))
                                        .addStatement("$N." + methodName + "(view)", JConstants.TARGET_PARAMETER_NAME)
                                        .endControlFlow() // 结束符"}"
                                        .endControlFlow(")")
                                        .build();
                            }
                        }
                    }
                }

                // 生成的文件必须是同包，因为使用注解的时候属性是没有修饰符的
                JavaFile.builder(
                        className.packageName(),
                        TypeSpec.classBuilder(className.simpleName() + JConstants.CLASS_END)
                                .addSuperinterface(typeName) // 实现JViewBinder接口
                                .addModifiers(Modifier.PUBLIC) // 修饰符public
                                .addMethod(methodBuilder.build()).build()) // 加入方法体
                        .build()
                        .writeTo(filer); // 文件生成器开始生成文件
            }
        }
    }
}
