package com.lantu.common.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class L {

    public static <T, R> Map<R, List<T>> groupBy(List<T> list, Function<T, R> fun) {

        Map<R, List<T>> result = new LinkedHashMap();

        if (list != null) {

            list.forEach((t) -> {

                R r = fun.apply(t);

                List<T> l = (List)result.get(r);

                if (l == null) {

                    l = new ArrayList();

                    result.put(r, l);

                }



                ((List)l).add(t);

            });

        }



        return result;

    }



    public static <T, R> List<R> collect(List<T> list, Function<T, R>... fun) {

        List<R> result = new ArrayList();

        if (list != null) {

            list.forEach((t) -> {
                for(Function<T, R> function : fun){
                    result.add(function.apply(t));
                }
            });

        }
        return result;

    }


    public static <T> List<T> grep(List<T> list, Function<T, Boolean> fun) {

        List<T> result = new ArrayList();

        if (list != null) {

            list.forEach((t) -> {

                if ((Boolean)fun.apply(t)) {

                    result.add(t);

                }



            });

        }



        return result;

    }



    public static <K, V> Map<K, V> toMap(List<V> list, Function<V, K> fun) {

        Map<K, V> result = new LinkedHashMap();

        if (list != null) {

            list.forEach((v) -> {

                result.put(fun.apply(v), v);

            });

        }



        return result;

    }

    public static <K, V, J> Map<K, J> toMap(List<V> list, Function<V, K> fun ,Function<V, J> fun2) {

        Map<K, J> result = new LinkedHashMap();

        if (list != null) {

            list.forEach((v) -> {

                result.put(fun.apply(v), fun2.apply(v));

            });

        }



        return result;

    }

    public static boolean isNullOrBlank(String s){
        return !StringUtils.isNotBlank(s);
    }

    public static boolean isNotNullOrBlank(String s){
        return !L.isNullOrBlank(s);
    }
}
