package com.alex.speedup.mock;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import org.mockito.InjectMocks;
import org.mockito.configuration.AnnotationEngine;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * xianshuangzhang@gmail.com
 */
public class MockitoDIListener extends DependencyInjectionTestExecutionListener {

    private static final Map<String, MockObject> mockObject   = new HashMap<String, MockObject>();
    private static final List<Field> injectFields = new ArrayList<Field>();

    @Override
    protected void injectDependencies(final TestContext testContext) throws Exception {
        super.injectDependencies(testContext);
        init(testContext);
        AnnotationEngine annotationEngine = new GlobalConfiguration().getAnnotationEngine();
        annotationEngine.process(testContext.getTestInstance().getClass(), testContext.getTestInstance());
        init(testContext);
    }


    protected void injectMock(final TestContext testContext) throws Exception {
        AutowireCapableBeanFactory beanFactory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
        for (Field field : injectFields) {
            field.setAccessible(true);
            Object o = beanFactory.getBean(field.getType());
            if (null != o) {
                Object targetClass = AopTargetUtils.getTarget(o);
                Field[] targetFields = targetClass.getClass().getDeclaredFields();

                for(Field field1:targetFields){
                    for (Iterator<Map.Entry<String, MockObject>> it = mockObject.entrySet().iterator(); it.hasNext();) {
                        Map.Entry<String, MockObject> entry = it.next();
                        Class mockObjType=entry.getValue().getType();

                        if(field1.getType().getName().equals(mockObjType.getName())){
                            setField(targetClass,field1.getName(),entry.getValue().getObj());
                        }
                    }
                }
            }
        }
    }
    public static void setField(Object target,String fieldName,Object fieldValue) throws Exception {
        Object targetClass = AopTargetUtils.getTarget(target);
        Field[] targetFields = targetClass.getClass().getDeclaredFields();
        // 如果是代理的话，找到真正的对象
        if (targetClass == null) {
            // 可能是远程实现
            return;
        }
        for (int i = 0; i < targetFields.length; i++) {
            if (targetFields[i].getName().equals(fieldName)) {
                targetFields[i].setAccessible(true);
                targetFields[i].set(targetClass,fieldValue);
            }
        }
    }
    private void init(final TestContext testContext) throws Exception {
        Object bean = testContext.getTestInstance();
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation antt : annotations) {
                // 只对autowire并且InjectMocks重新注入
                boolean isInjeckMocks = Iterators.any(Arrays.asList(annotations).iterator(), new Predicate<Annotation>() {
                    @Override
                    public boolean apply(Annotation annotation) {
                        return annotation.annotationType().equals(InjectMocks.class);
                    }
                });

                if (antt instanceof org.mockito.Mock) {
                    // 注入mock实例
                    MockObject obj = new MockObject();
                    field.setAccessible(true);
                    Object o= ReflectionUtils.getField(field, bean);
                    obj.setType(field.getType());
                    if(o==null) {
                        obj.setObj(mock(field.getType()));
                        field.setAccessible(true);
                        field.set(bean, obj.getObj());
                        mockObject.put(field.getName(), obj);
                    }else{
                        obj.setObj(o);
                        mockObject.put(field.getName(), obj);
                    }
                } else if ((antt instanceof Autowired)&& isInjeckMocks) {
                    injectFields.add(field);
                }
            }
        }

        injectMock(testContext);
    }

    public static class MockObject {
        private Object   obj;
        private Class<?> type;

        public MockObject(){
        }

        public Object getObj() {
            return obj;
        }

        public void setObj(Object obj) {
            this.obj = obj;
        }

        public Class<?> getType() {
            return type;
        }

        public void setType(Class<?> type) {
            this.type = type;
        }
    }

    public static Map<String, MockObject> getMockobject() {
        return mockObject;
    }

    public static List<Field> getInjectfields() {
        return injectFields;
    }
}  