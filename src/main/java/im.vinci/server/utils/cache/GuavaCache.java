package im.vinci.server.utils.cache;


import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.springframework.util.Assert;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * copy from org.springframework.cache.guava.GuavaCache
 * Created by tim@vinci on 16/4/5.
 */
public class GuavaCache extends Cache{
    private final String name;

    private final com.google.common.cache.Cache<Object, Object> cache;


    /**
     * Create a {@link GuavaCache} instance with the specified name and the
     * given internal {@link com.google.common.cache.Cache} to use.
     * @param name the name of the cache
     * @param cache the backing Guava Cache instance
     */
    public GuavaCache(String name, com.google.common.cache.Cache<Object, Object> cache) {
        this(name, cache, true);
    }

    /**
     * Create a {@link GuavaCache} instance with the specified name and the
     * given internal {@link com.google.common.cache.Cache} to use.
     * @param name the name of the cache
     * @param cache the backing Guava Cache instance
     * @param allowNullValues whether to accept and convert {@code null}
     * values for this cache
     */
    public GuavaCache(String name, com.google.common.cache.Cache<Object, Object> cache, boolean allowNullValues) {
        super(allowNullValues);
        Assert.notNull(name, "Name must not be null");
        Assert.notNull(cache, "Cache must not be null");
        this.name = name;
        this.cache = cache;
    }


    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final com.google.common.cache.Cache<Object, Object> getNativeCache() {
        return this.cache;
    }

    @Override
    public ValueWrapper get(Object key) {
        if (this.cache instanceof LoadingCache) {
            try {
                Object value = ((LoadingCache<Object, Object>) this.cache).get(key);
                return toValueWrapper(value);
            }
            catch (ExecutionException ex) {
                throw new UncheckedExecutionException(ex.getMessage(), ex);
            }
        }
        return super.get(key);
    }

    @Override
    protected Object lookup(Object key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public void put(Object key, Object value) {
        this.cache.put(key, toStoreValue(value));
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, final Object value) {
        try {
            PutIfAbsentCallable callable = new PutIfAbsentCallable(value);
            Object result = this.cache.get(key, callable);
            return (callable.called ? null : toValueWrapper(result));
        }
        catch (ExecutionException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void evict(Object key) {
        this.cache.invalidate(key);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }


    private class PutIfAbsentCallable implements Callable<Object> {

        private final Object value;

        private boolean called;

        public PutIfAbsentCallable(Object value) {
            this.value = value;
        }

        @Override
        public Object call() throws Exception {
            this.called = true;
            return toStoreValue(this.value);
        }
    }

}
