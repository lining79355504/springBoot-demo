package com.demo.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;


@UtilityClass
public class FunctionUtil {

    /**
     * 转化列表
     * @return
     */
    public <P, T> List<T> convertList(Collection<P> collection, Function<P, T> convertFunc) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(convertFunc)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转化列表 并且去重
     * @return
     */
    public <P, T> List<T> convertListDistinct(Collection<P> collection, Function<P, T> convertFunc) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .map(convertFunc)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 过滤列表
     * @return
     */
    public <P> List<P> filterList(Collection<P> collection, Predicate<? super P> predicate) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * 查找最大元素
     * @return
     */
    public <P> P findMax(Collection<P> collection, Comparator<? super P> comparator) {
        if (CollectionUtils.isEmpty(collection)) {
            return null;
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .max(comparator)
                .orElse(null);
    }

    /**
     * 列表数据转map
     *
     * @param collection 集合对象
     * @param keySelector key方法
     * @param valueSelector value方法
     * @return
     */
    public <P, K, V> Map<K, V> convert2Map(
            Collection<P> collection, Function<P, K> keySelector, Function<P, V> valueSelector) {
        // 默认取最新
        return convert2Map(collection, keySelector, valueSelector, (v1, v2) -> v2);
    }

    /**
     * 列表数据转map
     *
     * @param collection 集合对象
     * @param keySelector key方法
     * @param valueSelector value方法
     * @param mergeFunc 合并策略
     * @return
     */
    public <P, K, V> Map<K, V> convert2Map(
            Collection<P> collection,
            Function<P, K> keySelector,
            Function<P, V> valueSelector,
            BinaryOperator<V> mergeFunc) {
        if (CollectionUtils.isEmpty(collection)) {
            return Collections.emptyMap();
        }
        return collection.stream()
                .filter(Objects::nonNull)
                .filter(item -> Objects.nonNull(keySelector.apply(item)))
                .collect(Collectors.toMap(keySelector, valueSelector, mergeFunc));
    }

    /**
     * 列表数据转map
     * @return
     */
    public <T, O, P, Q> Map<O, Map<P, Q>> convert2Map(
            Collection<T> data,
            Function<T, O> key1Func,
            Function<T, P> key2Func,
            Function<T, Q> valueFunc) {
        return Optional.ofNullable(data).orElse(Collections.emptyList()).stream()
                .filter(Objects::nonNull)
                .filter(d -> Objects.nonNull(key1Func.apply(d)))
                .filter(d -> Objects.nonNull(key2Func.apply(d)))
                .filter(d -> Objects.nonNull(valueFunc.apply(d)))
                .collect(
                        Collectors.groupingBy(
                                key1Func, Collectors.toMap(key2Func, valueFunc, (v1, v2) -> v2)));
    }

    public  <T> List<T> splitByComma(String str, Function<String, T> convertFunc) {
        if (StringUtils.isBlank(str)){
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(",|，"))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .map(convertFunc)
                .collect(Collectors.toList());
    }


    public <T> String joinByByComma(List<T> items, Function<T, String> convertFunc) {
        return joinList(items,convertFunc,",");
    }

    public <T> String joinList(List<T> items, Function<T, String> convertFunc,String delimiter) {
        if (CollectionUtils.isEmpty(items)){
            return null;
        }
        return items.stream()
                .filter(Objects::nonNull)
                .map(convertFunc)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(delimiter));
    }

    public void setNoNUll(BiConsumer<String, Object> consumer, Supplier supplier, String property) {
        if (null != supplier.get()) {
            consumer.accept(property, supplier.get());
        }
    }

}
