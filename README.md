# trim-space
trim-space

项目 介绍

trimSpace是一个使用Spring的拦截器 HandlerInterceptor，对进入controller的字符串参数做通用化去除前后空格处理。

原理

使用反射得到controller入参，并逐一进行甄别是否是字符串类型，进行去除字符串类型前后空格

注意事项

POST请求

支持入参是@RequestBody类型，对象的属性：基本包装类型、对象类型、set、List、Map集合、数组类型
支持入参是@RequestParam 类型
POST请求

支持入参是对象类型接收
支持入参是@RequestParam 类型
支持入参是@PathVariable类型
使用说明

引入依赖 在controller的方法上引入注解@TrimSpace xxxx 扩展
