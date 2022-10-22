package com.hmdp.cacheUtils;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * @author rth
 * @description: TODO
 * @date 2022/10/22
 */
public class CacheUtils {

    /**
     *  解析生成redis最终key
     * @param prefix
     * @param filed
     * @return
     */
    public static String parseKey(String prefix, String filed, ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();

        // SpEL表达式为空默认返回前缀
        if (StringUtils.isBlank(filed)) {
            return prefix;
        }
        // _号分隔
        String spEL = filed.replace("_", "+'_'+");
        // 获得被拦截方法的参数列表
        LocalVariableTableParameterNameDiscoverer nd = new LocalVariableTableParameterNameDiscoverer();
        String[] parameterNames = nd.getParameterNames(method);
        // 使用SPEL进行key的解析
        SpelExpressionParser parser = new SpelExpressionParser();
        // spEL上下文
        StandardEvaluationContext context = new StandardEvaluationContext();
        // 把方法参数放入SpEL上下文中
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        String value = parser.parseExpression(spEL).getValue(context, String.class);
        return prefix + value;
    }

}
