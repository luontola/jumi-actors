// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import javax.annotation.concurrent.Immutable;
import java.util.*;

@Immutable
public class Immutables {

    public static <T> List<T> list(List<T> mutable) {
        return Collections.unmodifiableList(new ArrayList<>(mutable));
    }

    public static <T> List<T> list(T[] mutable) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, mutable);
        return Collections.unmodifiableList(list);
    }

    public static <K, V> Map<K, V> map(Map<K, V> mutable) {
        return Collections.unmodifiableMap(new HashMap<>(mutable));
    }

    public static Map<String, String> map(Properties mutable) {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : mutable.entrySet()) {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }
}
