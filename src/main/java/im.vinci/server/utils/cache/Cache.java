package im.vinci.server.utils.cache;

import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.Date;

/**
 * 扩展一个直接带过期时间的方法
 * Created by tim@vinci on 16/4/5.
 */
public abstract class Cache extends AbstractValueAdaptingCache {
    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param allowNullValues whether to allow for {@code null} values
     */
    protected Cache(boolean allowNullValues) {
        super(allowNullValues);
    }

    public void put(String key , String value , Date expire) {
        this.put(key,value);
    }
}
