package com.googlecode.aviator.runtime.type;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.aviator.exception.ExpressionRuntimeException;


/**
 * A Aviator regular expression pattern
 * 
 * @author dennis
 * 
 */
public class AviatorPattern extends AviatorObject {

    final Pattern pattern;


    public AviatorPattern(String expression) {
        super();
        this.pattern = Pattern.compile(expression);
    }


    @Override
    public AviatorObject add(AviatorObject other, Map<String, Object> env) {
        switch (other.getAviatorType()) {
        case String:
            return new AviatorString(this.pattern.pattern() + ((AviatorString) other).lexeme);
        case JavaType:
            AviatorJavaType javaType = (AviatorJavaType) other;
            final Object otherValue = javaType.getValue(env);
            if (otherValue instanceof String || otherValue instanceof Character) {
                return new AviatorString(this.pattern.pattern() + otherValue.toString());
            }
            else {
                return super.add(other, env);
            }
        default:
            return super.add(other, env);

        }
    }


    @Override
    public AviatorObject match(AviatorObject other, Map<String, Object> env) {
        switch (other.getAviatorType()) {
        case String:
            AviatorString aviatorString = (AviatorString) other;
            Matcher m = this.pattern.matcher(aviatorString.lexeme);
            if (m.matches()) {
                if (env != null && env != Collections.EMPTY_MAP) {
                    int groupCount = m.groupCount();
                    for (int i = 0; i <= groupCount; i++) {
                        env.put("$" + i, m.group(i));
                    }
                }
                return AviatorBoolean.TRUE;
            }
            else {
                return AviatorBoolean.FALSE;
            }
        case JavaType:
            AviatorJavaType javaType = (AviatorJavaType) other;
            final Object javaValue = javaType.getValue(env);
            if (javaValue instanceof String) {
                return match(new AviatorString((String) javaValue), env);
            }
            else if (javaValue instanceof Character) {
                return match(new AviatorString(String.valueOf(javaValue)), env);
            }
            else {
                throw new ExpressionRuntimeException(this + " could not match " + other);
            }
        default:
            throw new ExpressionRuntimeException(this + " could not match " + other);
        }

    }


    @Override
    public int compare(AviatorObject other, Map<String, Object> env) {
        switch (other.getAviatorType()) {
        case Pattern:
            return this.pattern.pattern().compareTo(((AviatorPattern) other).pattern.pattern());
        case JavaType:
            if (other.getValue(env) == null) {
                return 1;
            }
            else {
                throw new ExpressionRuntimeException("Could not compare Pattern with " + other.getAviatorType());
            }
        case Nil:
            return 1;
        default:
            throw new ExpressionRuntimeException("Could not compare Pattern with " + other.getAviatorType());
        }
    }


    @Override
    public AviatorType getAviatorType() {
        return AviatorType.Pattern;
    }


    @Override
    public Object getValue(Map<String, Object> env) {
        return "/" + pattern.pattern() + "/";
    }

}