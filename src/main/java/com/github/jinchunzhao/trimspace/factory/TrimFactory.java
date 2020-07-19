package com.github.jinchunzhao.trimspace.factory;

/**
 * 去除空格API入口
 *
 * @author JinChunZhao
 * @version 1.0
 * date 2020-07-12 10:00
 */
public interface TrimFactory {

    /**
     * 处理所有类型参数:基本数据类型、实体对象、List、数组、set、map，去除字符串首尾空格
     *
     * @param object 对象
     * @return 结果
     * @throws IllegalAccessException 异常
     */
    Object trimExe(Object object) throws IllegalAccessException;
}
