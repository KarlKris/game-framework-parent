package com.echo.common.expression;

import cn.hutool.core.convert.Convert;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.ObjectContext;

/**
 * 表达式解析
 *
 * @author: li-yuanwen
 */
public class Jexl3 {

    private final static JexlEngine ENGINE = new JexlBuilder().create();

    public static <T> T eval(String expression, Object context, Class<T> type) {
        JexlExpression jexlExpression = ENGINE.createExpression(expression);
        ObjectContext<Object> c = new ObjectContext<>(ENGINE, context);
        Object result = jexlExpression.evaluate(c);
        if (type.isInstance(result)) {
            return (T) result;
        }
        return Convert.convert(type, result);
    }

}
