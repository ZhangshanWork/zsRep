package im.vinci.server.utils.cache;

import com.whalin.MemCached.MemCachedClient;

import java.util.Date;

/**
 * Created by tim@vinci on 16/3/21.
 */
public class MemcachedCache extends Cache {

    private final MemCachedClient cachedClient;

    public MemcachedCache(MemCachedClient client) {
        super(true);
        this.cachedClient = client;
    }

    @Override
    protected Object lookup(Object key) {
        if (key == null) {
            return null;
        }
        return cachedClient.get(key.toString());
    }

    @Override
    public String getName() {
        return "memcached";
    }

    @Override
    public Object getNativeCache() {
        return cachedClient;
    }

    @Override
    public void put(Object key, Object value) {
    }
    @Override
    public void put(String key , String value , Date expire) {
        cachedClient.set(key,value,expire);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return null;
    }

    @Override
    public void evict(Object key) {
        if (key != null) {
            cachedClient.delete(key.toString());
        }
    }

    @Override
    public void clear() {
        cachedClient.flushAll();
    }
}
