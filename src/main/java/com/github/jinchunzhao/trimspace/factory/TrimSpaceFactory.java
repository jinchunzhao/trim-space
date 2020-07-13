package com.github.jinchunzhao.trimspace.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 处理各种类型入参空格实现
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2020-07-13 11:19
 */
public class TrimSpaceFactory extends AbstractTrimFactory {

    /**
     * 非字符串类型的原始数据类型
     */
    private static final List<Class<?>> WRAPPER = Arrays.asList(Byte.class, Short.class,
        Integer.class, Long.class, Float.class, Double.class, Boolean.class);

    /**
     * 请求参数--request response
     */
    private static final List<Class<?>> REQUEST_PARAM = Arrays.asList(RequestFacade.class, ResponseFacade.class);
    /**
     * 符串类型的原始数据类型
     */
    private static final List<Class<?>> STRING_PER = Arrays.asList(Character.class, String.class);

    /**
     * json 参数，map参数
     */
    private static final List<Class<?>> JSON_MAP_PARAM = Arrays.asList(JSONObject.class, Map.class, LinkedHashMap.class,
        HashMap.class);

    @Override
    Object trimStringFiled(Object object, Field field) throws IllegalAccessException {
        Class<?> filedType = field.getType();
        if (filedType == String.class || filedType == Character.class) {
            field.setAccessible(true);
            if (Objects.nonNull(field.get(object))) {
                // 在原有的对象上设置去除首尾空格的新值
                field.set(object, String.valueOf(field.get(object)).trim());
            }
        }
        return object;
    }

    @Override
    Object trimObjectFiled(Object object, Field field1) throws IllegalAccessException {
        Object filedValue1 = field1.get(object);
        Class<?> filedType1 = field1.getType();
        if (Objects.isNull(field1)) {
            return field1;
        }
        // 为非String Char包装类型，不做处理
        if (WRAPPER.contains(filedType1)) {
            return object;
        }
        if (filedType1 == String.class || filedType1 == Character.class) {
            return filedValue1.toString().trim();
        }
        // 字段为private时，无法修改字段值，需要反射
        Field[] fields = filedType1.getDeclaredFields();
        if (Objects.isNull(fields) || fields.length == 0) {
            return object;
        }
        for (Field field : fields) {
            field.setAccessible(true);
            Object filedValue = field.get(filedValue1);
            if (Objects.nonNull(filedValue)) {
                Class<?> aClass = filedValue.getClass();
                if (filedValue instanceof List) {
                    trimList(filedValue1, field);
                } else if (aClass.isArray()) { //判断这个对象是否是数组
                    trimArray(filedValue1, field);
                } else if (filedValue instanceof Map) {
                    trimMap(filedValue1, field);
                } else if (filedValue instanceof String || filedValue instanceof Character) {
                    trimStringFiled(filedValue1, field);
                } else if (WRAPPER.contains(aClass)) {
                    continue;
                } else {
                    trimObjectFiled(filedValue1, field);
                }
            }
            Class<?> filedType = field.getType();
            // 只处理字符串类型
            if (filedType != String.class) {
                continue;
            }
            if (filedType == String.class || filedType == Character.class) {
                // 去除private权限，变为可更改
                field.setAccessible(true);
                if (Objects.nonNull(field.get(filedValue1))) {
                    // 在原有的对象上设置去除首尾空格的新值
                    field.set(filedValue1, String.valueOf(field.get(filedValue1)).trim());
                }
            }
        }
        return object;
    }

    @Override
    Object trimList(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Object[] objs = ((Collection) filedValue).toArray();
        if (Objects.isNull(objs)) {
            return object;
        }
        if (objs.length == 0) {
            return object;
        }
        List<String> list = new ArrayList<>();
        for (int index = 0; index < objs.length; index++) {
            Object e1 = objs[index];
            if (e1.getClass() == String.class) {
                String trim = String.valueOf(e1).trim();
                list.add(trim);
            } else {
                trimExe(e1);
            }
        }
        if (Objects.nonNull(list) && list.size() > 0) {
            field.set(object, list);
        }
        return object;
    }

    @Override
    Object trimMap(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Map<Object, Object> filedValueMap = (Map<Object, Object>) filedValue;
        if (CollectionUtils.isEmpty(filedValueMap)) {
            return object;
        }
        for (Map.Entry<Object, Object> entry : filedValueMap.entrySet()) {
            Object entryValue = entry.getValue();
            Class<?> aClass1 = entryValue.getClass();
            if (aClass1 == String.class) {
                String trim = String.valueOf(entryValue).trim();
                entry.setValue(trim);
            } else {
                trimExe(entryValue);
            }
        }
        if (!CollectionUtils.isEmpty(filedValueMap)) {
            field.set(object, filedValueMap);
        }
        return object;
    }

    @Override
    Object trimArray(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Object[] objs = (Object[]) filedValue;
        if ( Objects.isNull(objs)) {
            return object;
        }
        if (objs.length == 0) {
            return object;
        }
        List<String> list = new ArrayList<>();
        for (int index = 0; index < objs.length; index++) {
            Object e1 = objs[index];
            if (e1.getClass() == String.class) {
                String trim = String.valueOf(e1).trim();
                list.add(trim);
                objs[index] = trim;
            } else {
                trimExe(e1);
            }
        }
        if (Objects.nonNull(objs) && objs.length > 0) {
            field.set(object, objs);
        }
        return object;
    }

    @Override
    Object trimJsonMap(Object object) throws IllegalAccessException {
        if (Objects.isNull(object)){
            return object;
        }

        //        if(object instanceof JSONObject){
        //
        //        }else if (object instanceof Map){
        //
        //        }
        HashMap<Object,Object> hashMap = JSONObject.parseObject(JSON.toJSON(object).toString(), HashMap.class);
        if (CollectionUtils.isEmpty(hashMap)) {
            return object;
        }
        for (Map.Entry<Object,Object> entry : hashMap.entrySet()) {
            Object entryValue = entry.getValue();
            Class<?> valClass = entryValue.getClass();
            if (Objects.equals(valClass,String.class)) {
                String trim = String.valueOf(entryValue).trim();
                entry.setValue(trim);
            } else {
                trimExe(entryValue);
            }
        }
        object = JSONObject.toJSON(hashMap);
        return object;
    }

    @Override
    public Object trimExe(Object object) throws IllegalAccessException {
        if (Objects.isNull(object)) {
            return object;
        }
        Class<? extends Object> clazz = object.getClass();
        if (REQUEST_PARAM.contains(clazz)){
            return object;
        }
        // 为非String Char包装类型，不做处理
        if (WRAPPER.contains(clazz)) {
            return object;
        }
        if (STRING_PER.contains(clazz)) {
            return object.toString().trim();
        }
        if(JSON_MAP_PARAM.contains(clazz)){
            return trimJsonMap(object);
        }
        // 字段为private时
        Field[] fields = clazz.getDeclaredFields();
        if (Objects.isNull(fields) || fields.length == 0) {
            return object;
        }
        for (Field field : fields) {
            field.setAccessible(true);
            Object filedValue = field.get(object);
            if (Objects.nonNull(filedValue)) {
                Class<?> aClass = filedValue.getClass();
                if (filedValue instanceof Collection) {
                    trimList(object, field);
                } else if (aClass.isArray()) {
                    trimArray(object, field); //数组中含对象时有bug
                } else if (filedValue instanceof Map) {
                    trimMap(object, field);
                } else if (filedValue instanceof String || filedValue instanceof Character) {
                    trimStringFiled(object, field);
                } else if (WRAPPER.contains(aClass)) {
                    continue;
                } else {
                    trimObjectFiled(object, field);
                }
            }
        }

        return object;
    }
}
