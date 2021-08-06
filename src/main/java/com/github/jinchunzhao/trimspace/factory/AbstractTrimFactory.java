package com.github.jinchunzhao.trimspace.factory;

import java.lang.reflect.Field;

/**
 * 处理各种类型入参空格
 *
 * @author JinChunZhao
 * @version 1.0 date 2020-07-12 10:30
 */
public abstract class AbstractTrimFactory implements TrimFactory {

    /**
     * 处理字符串类型的数据
     *
     * @param object
     *            对象
     * @param field
     *            属性
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimStringFiled(Object object, Field field) throws IllegalAccessException;

    /**
     * 处理实体对象类型的数据
     *
     * @param object
     *            对象
     * @param field1
     *            字段
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimObjectFiled(Object object, Field field1) throws IllegalAccessException;

    /**
     * 处理Collection类型的数据
     *
     * @param object
     *            对象
     * @param field
     *            字段
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimListFiled(Object object, Field field) throws IllegalAccessException;

    /**
     * 处理数组类型的数据
     *
     * @param object
     *            对象
     * @param field
     *            字段
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimArrayFiled(Object object, Field field) throws IllegalAccessException;

    /**
     * 处理MAP类型的数据
     *
     * @param object
     *            对象
     * @param field
     *            字段
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimMapFiled(Object object, Field field) throws IllegalAccessException;

    /**
     * 处理JsonMAP类型的数据
     *
     * @param object
     *            对象
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimJsonMap(Object object) throws IllegalAccessException;

    /**
     * 处理paramList类型的数据
     *
     * @param object
     *            对象
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimParamListOrArray(Object object) throws IllegalAccessException;

    /**
     * 处理Json String类型的数据
     *
     * @param object
     *            对象
     * @return 结果
     * @throws IllegalAccessException
     *             异常
     */
    abstract Object trimJsonString(Object object) throws IllegalAccessException;

}
