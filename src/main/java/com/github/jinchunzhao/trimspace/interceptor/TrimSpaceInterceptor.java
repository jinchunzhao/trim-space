package com.github.jinchunzhao.trimspace.interceptor;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.github.jinchunzhao.trimspace.factory.TrimFactory;
import com.github.jinchunzhao.trimspace.factory.TrimSpaceFactory;

/**
 * 自定义拦截器--实现去除首尾空格处理
 *
 * @author JinChunZhao
 * @version 1.0 date 2020-06-23 19:50
 */
@Component
@Aspect
public class TrimSpaceInterceptor {

    /**
     * 环绕处理
     *
     * @param proceedingJoinPoint
     *            连接点
     * @return 结果
     * @throws Throwable
     *             异常
     */
    @Around(value = "@annotation(com.github.jinchunzhao.trimspace.annotation.TrimSpace)")
    public Object trimAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        // RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        // ServletRequestAttributes sra = (ServletRequestAttributes) ra;
        // HttpServletRequest request = sra.getRequest();

        // String url = request.getRequestURL().toString();
        // String method = request.getMethod();
        // String uri = request.getRequestURI();
        // String queryString = request.getQueryString();
        // log.info("使用注解去除controller入参前后空格的请求来了url:【{}】,方法:【[]】,uri:【{}】,queryString:【{}】", url, method, uri,
        // queryString);

        // 获取连接点的入参
        Object[] objects = proceedingJoinPoint.getArgs();
        TrimFactory trimFactory = TrimSpaceFactory.getInstance();
        if (Objects.nonNull(objects) && objects.length > 0) {
            // 处理接口传参：去除首尾空格
            for (int i = 0, len = objects.length; i < len; i++) {
                if (Objects.isNull(objects[i])) {
                    continue;
                }
                objects[i] = trimFactory.trimExe(objects[i]);
            }
        }
        // 放行
        return proceedingJoinPoint.proceed(objects);
    }
}
