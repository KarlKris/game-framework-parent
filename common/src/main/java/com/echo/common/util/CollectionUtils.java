package com.echo.common.util;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ReflectUtil;

import java.util.*;

/**
 * 扩展 hutool CollectionUtil
 * @author: li-yuanwen
 */
public class CollectionUtils extends CollectionUtil {

    /**
     * Returns given object as {@link Collection}. Will return the {@link Collection} as is if the source is a
     * {@link Collection} already, will convert an array into a {@link Collection} or simply create a single element
     * collection for everything else.
     *
     * @param source must not be {@literal null}.
     * @return never {@literal null}.
     * @since 3.2
     */
    public static Collection<?> asCollection(Object source) {

        if (source instanceof Collection<?>) {
            return (Collection<?>) source;
        }

        return source.getClass().isArray() ? arrayToList(source) : Collections.singleton(source);
    }

    /**
     * Convert the supplied array into a List. A primitive array gets converted
     * into a List of the appropriate wrapper type.
     * <p><b>NOTE:</b> Generally prefer the standard {@link Arrays#asList} method.
     * This {@code arrayToList} method is just meant to deal with an incoming Object
     * value that might be an {@code Object[]} or a primitive array at runtime.
     * <p>A {@code null} source value will be converted to an empty List.
     * @param source the (potentially primitive) array
     * @return the converted List result
     * @see ObjectUtils#toObjectArray(Object)
     * @see Arrays#asList(Object[])
     */
    public static List<?> arrayToList(Object source) {
        return Arrays.asList(ObjectUtils.toObjectArray(source));
    }


    /**
     * 创建collection
     * @param collectionType collection 类型
     * @param elementType 泛型类型
     * @param capacity 初始容量
     * @return collection<E>
     * @param <E>
     */
    public static <E> Collection<E> create(Class<?> collectionType, Class<?> elementType, int capacity) {
        final Collection<E> list;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            // 抽象集合默认使用ArrayList
            list = new ArrayList<>(capacity);
        }

        // Set
        else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>(capacity);
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>(capacity);
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>((o1, o2) -> {
                // 优先按照对象本身比较，如果没有实现比较接口，默认按照toString内容比较
                if (o1 instanceof Comparable) {
                    return ((Comparable<E>) o1).compareTo(o2);
                }
                return CompareUtil.compare(o1.toString(), o2.toString());
            });
        } else if (collectionType.isAssignableFrom(EnumSet.class)) {
            list = (Collection<E>) EnumSet.noneOf(Assert.notNull((Class<Enum>) elementType));
        }

        // List
        else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>(capacity);
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        }

        // Others，直接实例化
        else {
            try {
                list = (Collection<E>) ReflectUtil.newInstance(collectionType);
            } catch (final Exception e) {
                // 无法创建当前类型的对象，尝试创建父类型对象
                final Class<?> superclass = collectionType.getSuperclass();
                if (null != superclass && collectionType != superclass) {
                    return create(superclass);
                }
                throw new UtilException(e);
            }
        }
        return list;
    }

    /**
     * Create the most appropriate map for the given map type.
     * <p>Delegates to {@link #createMap(Class, Class, int)} with a
     * {@code null} key type.
     * @param mapType the desired type of the target map
     * @param capacity the initial capacity
     * @return a new map instance
     * @throws IllegalArgumentException if the supplied {@code mapType} is
     * {@code null} or of type {@link EnumMap}
     */
    public static <K, V> Map<K, V> createMap(Class<?> mapType, int capacity) {
        return createMap(mapType, null, capacity);
    }

    /**
     * Create the most appropriate map for the given map type.
     * <p><strong>Warning</strong>: Since the parameterized type {@code K}
     * is not bound to the supplied {@code keyType}, type safety cannot be
     * guaranteed if the desired {@code mapType} is {@link EnumMap}. In such
     * scenarios, the caller is responsible for ensuring that the {@code keyType}
     * is an enum type matching type {@code K}. As an alternative, the caller
     * may wish to treat the return value as a raw map or map keyed by
     * {@link Object}. Similarly, type safety cannot be enforced if the
     * desired {@code mapType} is {@link MultiValueMap}.
     * @param mapType the desired type of the target map (never {@code null})
     * @param keyType the map's key type, or {@code null} if unknown
     * (note: only relevant for {@link EnumMap} creation)
     * @param capacity the initial capacity
     * @return a new map instance
     * @throws IllegalArgumentException if the supplied {@code mapType} is
     * {@code null}; or if the desired {@code mapType} is {@link EnumMap} and
     * the supplied {@code keyType} is not a subtype of {@link Enum}
     * @since 4.1.3
     * @see java.util.LinkedHashMap
     * @see java.util.TreeMap
     * @see java.util.EnumMap
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <K, V> Map<K, V> createMap(Class<?> mapType, Class<?> keyType, int capacity) {
        Assert.notNull(mapType, "Map type must not be null");
        if (mapType.isInterface()) {
            if (Map.class == mapType) {
                return new LinkedHashMap<>(capacity);
            }
            else if (SortedMap.class == mapType || NavigableMap.class == mapType) {
                return new TreeMap<>();
            }
//            else if (MultiValueMap.class == mapType) {
//                return new LinkedMultiValueMap();
//            }
            else {
                throw new IllegalArgumentException("Unsupported Map interface: " + mapType.getName());
            }
        }
        else if (EnumMap.class == mapType) {
            Assert.notNull(keyType, "Cannot create EnumMap for unknown key type");
            return new EnumMap(asEnumType(keyType));
        }
        else {
            if (!Map.class.isAssignableFrom(mapType)) {
                throw new IllegalArgumentException("Unsupported Map type: " + mapType.getName());
            }
            try {
                return (Map<K, V>) ReflectionUtils.accessibleConstructor(mapType).newInstance();
            }
            catch (Throwable ex) {
                throw new IllegalArgumentException("Could not instantiate Map type: " + mapType.getName(), ex);
            }
        }
    }


    /**
     * Cast the given type to a subtype of {@link Enum}.
     * @param enumType the enum type, never {@code null}
     * @return the given type as subtype of {@link Enum}
     * @throws IllegalArgumentException if the given type is not a subtype of {@link Enum}
     */
    @SuppressWarnings("rawtypes")
    private static Class<? extends Enum> asEnumType(Class<?> enumType) {
        Assert.notNull(enumType, "Enum type must not be null");
        if (!Enum.class.isAssignableFrom(enumType)) {
            throw new IllegalArgumentException("Supplied type is not an enum: " + enumType.getName());
        }
        return enumType.asSubclass(Enum.class);
    }

}
