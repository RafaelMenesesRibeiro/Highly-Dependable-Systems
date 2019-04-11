package hds.server.controllers.controllerHelpers;

import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMap<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_CACHED_ENTRIES = 128;

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > MAX_CACHED_ENTRIES;
    }
}