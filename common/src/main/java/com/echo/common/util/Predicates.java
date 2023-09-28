package com.echo.common.util;


import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;

/**
 * @author: li-yuanwen
 */
public class Predicates {

    Predicate<Member> IS_ENUM_MEMBER = member -> member.getDeclaringClass().isEnum();
    // should
    // go
    // into
    // JPA
    Predicate<Member> IS_OBJECT_MEMBER = member -> Object.class.equals(member.getDeclaringClass());
    Predicate<Member> IS_NATIVE = member -> Modifier.isNative(member.getModifiers());
    Predicate<Member> IS_PRIVATE = member -> Modifier.isPrivate(member.getModifiers());
    Predicate<Member> IS_PROTECTED = member -> Modifier.isProtected(member.getModifiers());
    Predicate<Member> IS_PUBLIC = member -> Modifier.isPublic(member.getModifiers());
    Predicate<Member> IS_SYNTHETIC = Member::isSynthetic;

    Predicate<Member> IS_STATIC = member -> Modifier.isStatic(member.getModifiers());

    Predicate<Method> IS_BRIDGE_METHOD = Method::isBridge;

    /**
     * A {@link Predicate} that yields always {@code true}.
     *
     * @return a {@link Predicate} that yields always {@code true}.
     */
    public static <T> Predicate<T> isTrue() {
        return t -> true;
    }

    /**
     * A {@link Predicate} that yields always {@code false}.
     *
     * @return a {@link Predicate} that yields always {@code false}.
     */
    public static <T> Predicate<T> isFalse() {
        return t -> false;
    }

    /**
     * Returns a {@link Predicate} that represents the logical negation of {@code predicate}.
     *
     * @return a {@link Predicate} that represents the logical negation of {@code predicate}.
     */
    public static <T> Predicate<T> negate(Predicate<T> predicate) {

        if (predicate == null) {
            throw new IllegalArgumentException("Predicate must not be null");
        }
        return predicate.negate();
    }


}
