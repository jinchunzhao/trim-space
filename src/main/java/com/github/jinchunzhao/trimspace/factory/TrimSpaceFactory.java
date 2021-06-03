package com.github.jinchunzhao.trimspace.factory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
 * date 2020-07-13 11:19
 */
public class TrimSpaceFactory extends AbstractTrimFactory {

    /**
     * 非字符串类型的原始数据类型
     */
    private static final List<Class<?>> WRAPPER =
        Arrays.asList(Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class);

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
    private static final List<Class<?>> JSON_MAP_PARAM =
        Arrays.asList(JSONObject.class, Map.class, LinkedHashMap.class, HashMap.class);

    private static TrimSpaceFactory INSTANCE = null;

    //私有化构造子,阻止外部直接实例化对象
    private TrimSpaceFactory(){

    }

    /**
     * 获取类的单例实例
     * @return
     *   返回类的唯一实例
     */
    public static TrimSpaceFactory getInstance(){
        if(INSTANCE == null){
            synchronized (TrimSpaceFactory.class) {
                if(INSTANCE == null){
                    INSTANCE = new TrimSpaceFactory();
                }
            }
        }
        return INSTANCE;
    }

    @Override Object trimStringFiled(Object object, Field field) throws IllegalAccessException {
        Class<?> filedTypeClazz = field.getType();
        Object objFiled = field.get(object);
        if (Objects.isNull(objFiled)) {
            return object;
        }
        String filedValue = String.valueOf(objFiled);
        Boolean isJson = isJson(filedValue);
        if (isJson) {
            Object obj = JSON.parse(filedValue);
            if (obj instanceof JSONObject) {
                field.set(object, setJsonArrayMap(object, null));
            } else if (obj instanceof JSONArray) {
                JSONArray objects = JSONObject.parseArray(filedValue);
                List<Object> list = new ArrayList<>();
                for (int i = 0, len = objects.size(); i < len; i++) {
                    Object item = objects.get(i);
                    if (Objects.isNull(item)) {
                        continue;
                    }
                    list.add(setJsonArrayMap(object, item));
                }
                String json = JSONObject.toJSONString(list);
                Object objVal = JSON.parseObject(json, filedTypeClazz);
                field.set(object, objVal);
            } else {
                return object;
            }
        } else {
            field.setAccessible(true);
            if (Objects.nonNull(objFiled)) {
                field.set(object, String.valueOf(objFiled).trim());
            }
        }
        return object;
    }

    @Override Object trimObjectFiled(Object object, Field field1) throws IllegalAccessException {
        Object filedValue1 = field1.get(object);
        Class<?> filed1Clazz = field1.getType();
        if (Objects.isNull(field1)) {
            return field1;
        }
        if (REQUEST_PARAM.contains(filed1Clazz)) {
            return object;
        } else if (WRAPPER.contains(filed1Clazz)) {
            // 为非String Char包装类型，不做处理
            return object;
        } else if (STRING_PER.contains(filed1Clazz)) {
            // 字符串类型
            return trimJsonString(object);
        } else if (JSON_MAP_PARAM.contains(filed1Clazz)) {
            // map json类型
            return trimJsonMap(object);
        } else if (filed1Clazz.isArray()) {
            // 数组类型
            return trimParamListOrArray(object);
        }

        if (object instanceof Collection) {
            return trimParamListOrArray(object);
        }
        // 字段为private时，无法修改字段值，需要反射
        Field[] fields = filed1Clazz.getDeclaredFields();
        if (Objects.isNull(fields) || fields.length == 0) {
            return object;
        }

        Class<?> superclass = filed1Clazz.getSuperclass();
        List<Field> fieldsList = new ArrayList<>(Arrays.asList(fields));
        //是否继承父类
        if (Objects.nonNull(superclass)) {
            Field[] superFields = superclass.getDeclaredFields();
            fieldsList.addAll(Arrays.asList(superFields));
        }
        for (Field field : fieldsList) {
            field.setAccessible(true);
            Object filedValue = field.get(filedValue1);
            if (Objects.nonNull(filedValue)) {
                Class<?> aClass = filedValue.getClass();
                if (filedValue instanceof List) {
                    trimListFiled(filedValue1, field);
                } else if (aClass.isArray()) {
                    trimArrayFiled(filedValue1, field);
                } else if (filedValue instanceof Map) {
                    trimMapFiled(filedValue1, field);
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
            if (!Objects.equals(filedType, String.class)) {
                continue;
            }
            if (WRAPPER.contains(filedType)) {
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

    @Override Object trimListFiled(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Object[] objs = ((Collection) filedValue).toArray();
        if (Objects.isNull(objs)) {
            return object;
        }
        if (objs.length == 0) {
            return object;
        }
        List<Object> list = new ArrayList<>();
        for (int index = 0; index < objs.length; index++) {
            Object e1 = objs[index];
            if (Objects.isNull(e1)) {
                continue;
            }
            if (Objects.equals(e1.getClass(), String.class)) {
                String trim = String.valueOf(e1).trim();
                list.add(trim);
            } else {
                list.add(trimExe(e1));
            }
        }
        if (Objects.nonNull(list) && list.size() > 0) {
            field.set(object, list);
        }
        return object;
    }

    @Override Object trimArrayFiled(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Object[] objs = (Object[]) filedValue;
        if (Objects.isNull(objs)) {
            return object;
        }
        if (objs.length == 0) {
            return object;
        }
        for (int index = 0; index < objs.length; index++) {
            Object e1 = objs[index];
            if (Objects.isNull(e1)) {
                continue;
            }
            if (Objects.equals(e1.getClass(), String.class)) {
                String trim = String.valueOf(e1).trim();
                objs[index] = trim;
            } else {
                objs[index] = trimExe(e1);
            }
        }
        if (Objects.nonNull(objs) && objs.length > 0) {
            field.set(object, objs);
        }
        return object;
    }

    @Override Object trimMapFiled(Object object, Field field) throws IllegalAccessException {
        Object filedValue = field.get(object);
        Map<Object, Object> filedValueMap = (Map<Object, Object>) filedValue;
        if (CollectionUtils.isEmpty(filedValueMap)) {
            return object;
        }
        for (Map.Entry<Object, Object> entry : filedValueMap.entrySet()) {
            Object entryValue = entry.getValue();
            if (Objects.isNull(entryValue)) {
                continue;
            }
            Class<?> aClass1 = entryValue.getClass();
            if (Objects.equals(aClass1, String.class)) {
                String trim = String.valueOf(entryValue).trim();
                entry.setValue(trim);
            } else {
                entry.setValue(trimExe(entryValue));
            }
        }
        if (!CollectionUtils.isEmpty(filedValueMap)) {
            field.set(object, filedValueMap);
        }
        return object;
    }

    @Override Object trimJsonMap(Object object) throws IllegalAccessException {
        Class<? extends Object> clazz = object.getClass();
        if (Objects.isNull(object)) {
            return object;
        }
        Map<Object, Object> hashMap = JSONObject.parseObject(JSON.toJSON(object).toString(), HashMap.class);
        if (CollectionUtils.isEmpty(hashMap)) {
            return object;
        }
        for (Map.Entry<Object, Object> entry : hashMap.entrySet()) {
            Object entryValue = entry.getValue();
            if (Objects.isNull(entryValue)) {
                continue;
            }
            Class<?> valClass = entryValue.getClass();
            if (Objects.equals(valClass, String.class)) {
                String trim = String.valueOf(entryValue).trim();
                entry.setValue(trim);
            } else if (WRAPPER.contains(valClass)) {
                continue;
            } else {
                entry.setValue(trimExe(entryValue));
            }
        }
        String json = JSONObject.toJSONString(hashMap);
        object = JSON.parseObject(json, clazz);
        return object;
    }

    @Override Object trimParamListOrArray(Object object) throws IllegalAccessException {
        Class<? extends Object> clazz = object.getClass();
        Object[] objs = null;
        if (clazz.isArray()) {
            objs = (Object[]) object;
        } else {
            objs = ((Collection) object).toArray();
        }
        if (Objects.isNull(objs)) {
            return object;
        }
        if (objs.length == 0) {
            return object;
        }
        List<Object> list = new ArrayList<>();
        for (int index = 0; index < objs.length; index++) {
            Object e1 = objs[index];
            if (Objects.isNull(e1)) {
                continue;
            }
            if (Objects.equals(e1.getClass(), String.class)) {
                String trim = String.valueOf(e1).trim();
                list.add(trim);
            } else if (WRAPPER.contains(e1.getClass())) {
                continue;
            } else {
                list.add(trimExe(e1));
            }
        }
        String json = JSONObject.toJSONString(list);
        object = JSON.parseObject(json, clazz);
        return object;
    }

    @Override Object trimJsonString(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        if (Objects.isNull(object)) {
            return object;
        }
        String objValue = String.valueOf(object);
        Boolean isJson = isJson(objValue);
        if (isJson) {
            Object obj = JSON.parse(objValue);
            if (obj instanceof JSONObject) {
                // setJsonArrayMap(object,null);
                return setJsonArrayMap(object, null);
            } else if (obj instanceof JSONArray) {
                JSONArray objects = JSONObject.parseArray(objValue);
                List<Object> list = new ArrayList<>();
                for (int i = 0, len = objects.size(); i < len; i++) {
                    Object item = objects.get(i);
                    if (Objects.isNull(item)) {
                        continue;
                    }
                    list.add(setJsonArrayMap(object, item));
                }
                String json = JSONObject.toJSONString(list);
                return JSON.parseObject(json, clazz);
            } else {
                return object;
            }
        } else {
            // 在原有的对象上设置去除首尾空格的新值
            return String.valueOf(object).trim();
        }
    }

    @Override public Object trimExe(Object object) throws IllegalAccessException {
        if (Objects.isNull(object)) {
            return object;
        }
        Class<? extends Object> clazz = object.getClass();
        if (REQUEST_PARAM.contains(clazz)) {
            return object;
        } else if (WRAPPER.contains(clazz)) {
            // 为非String Char包装类型，不做处理
            return object;
        } else if (STRING_PER.contains(clazz)) {
            // 字符串类型
            return trimJsonString(object);
        } else if (JSON_MAP_PARAM.contains(clazz)) {
            // map json类型
            return trimJsonMap(object);
        } else if (clazz.isArray()) {
            // 数组类型
            return trimParamListOrArray(object);
        }

        // list set类型
        if (object instanceof Collection) {
            return trimParamListOrArray(object);
        }
        // 字段为private时
        Field[] fields = clazz.getDeclaredFields();
        if (Objects.isNull(fields) || fields.length == 0) {
            return object;
        }

        Class<?> superclass = clazz.getSuperclass();
        List<Field> fieldsList = new ArrayList<>(Arrays.asList(fields));
        //是否有父类
        if (!Objects.isNull(superclass)) {
            Field[] superFields = superclass.getDeclaredFields();
            fieldsList.addAll(Arrays.asList(superFields));
        }
        for (Field field : fieldsList) {
            field.setAccessible(true);
            Object filedValue = field.get(object);
            if (Objects.nonNull(filedValue)) {
                Class<?> aClass = filedValue.getClass();
                if (filedValue instanceof Collection) {
                    trimListFiled(object, field);
                } else if (aClass.isArray()) {
                    trimArrayFiled(object, field); // 数组中含对象时有bug
                } else if (filedValue instanceof Map) {
                    trimMapFiled(object, field);
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

    /**
     * 判断字符串是否是json格式
     *
     * @param string 字符串
     * @return true: 是 false:否
     */
    private boolean isJson(String string) {
        try {
            Object obj = JSON.parse(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 设置json 类型 Array、Map
     *
     * @param object object
     * @param item   每一项
     * @return object
     * @throws IllegalAccessException 异常
     */
    private Object setJsonArrayMap(Object object, Object item) throws IllegalAccessException {
        Class<?> aClass = null;
        Map<Object, Object> hashMap = null;
        if (Objects.isNull(item)) {
            aClass = object.getClass();
            hashMap = JSONObject.parseObject(JSON.toJSON(object).toString(), HashMap.class);
        } else {
            aClass = item.getClass();
            hashMap = JSONObject.parseObject(JSON.toJSON(item).toString(), HashMap.class);

        }
        if (CollectionUtils.isEmpty(hashMap)) {
            return object;
        }
        for (Map.Entry<Object, Object> entry : hashMap.entrySet()) {
            Object entryValue = entry.getValue();
            if (Objects.isNull(entryValue)) {
                continue;
            }
            Class<?> valClass = entryValue.getClass();
            if (Objects.equals(valClass, String.class)) {
                String trim = String.valueOf(entryValue).trim();
                entry.setValue(trim);
            } else if (WRAPPER.contains(valClass)) {
                continue;
            } else {
                entry.setValue(trimExe(entryValue));
            }
        }
        String json = JSONObject.toJSONString(hashMap);
        object = JSON.parseObject(json, aClass);
        return object;
    }
}
