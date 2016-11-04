package im.vinci.server.user.service;

import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.user.domain.UserCounts;
import im.vinci.server.user.persistence.UserCountMapper;
import im.vinci.server.utils.BizTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 用户计数类操作
 * 这个类中一般不对userid做检验,因为都是内部调用
 * Created by tim@vinci on 16/8/29.
 */
@Service
public class UserCountService {

    @Autowired
    private UserCountMapper userCountMapper;

    @Autowired
    private Environment environment;

    @Transactional
    public Boolean insertUserCount(final long userId) {
        return new BizTemplate<Boolean>("userCounts.insertUserCount") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                return userCountMapper.insertUserCount(userId) > 0;
            }
        }.execute();
    }

    /**
     * 调整用户计数表,关注和粉丝
     *
     * @param userId   关注人
     * @param toUserId 被关注人
     */
    @Transactional
    public Boolean adjustAttentionCount(long userId, long toUserId, boolean isDelete) {
        return new BizTemplate<Boolean>("userCounts.adjustAttentionCount") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                int delta = (isDelete ? -1 : 1);
                int maxAttentionCount = environment.getProperty("mobile.attention.max_count", Integer.class, 200) - delta;
                return userCountMapper.addAttentionerCount(userId, delta, maxAttentionCount) > 0
                        && userCountMapper.addFollowerCount(toUserId, delta) > 0;
            }
        }.execute();
    }

    @Transactional
    public Boolean adjustFeedCount(long userId, boolean isDelete) {
        return new BizTemplate<Boolean>("userCounts.adjustFeedCount") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                return userCountMapper.addFeedCount(userId, isDelete ? -1 : 1) > 0;
            }
        }.execute();
    }
    @Transactional
    public Boolean adjustMessageUnreadCount(long userId, int delta) {
        return new BizTemplate<Boolean>("userCounts.adjustMessageUnreadCount") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                return userCountMapper.addMessageUnreadCount(userId, delta) > 0;
            }
        }.execute();
    }
    /**
     * 返回UserCounts
     */
    public UserCounts getUserCount(final long id) {
        return new BizTemplate<UserCounts>("getUserCount") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected UserCounts process() throws Exception {
                return userCountMapper.getUserCount(id);
            }
        }.execute();
    }

    public Map<Long, UserCounts> getUserCountsMap(final Collection<Long> uidList) {
        return new BizTemplate<Map<Long, UserCounts>>("getUserCountsMap") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Map<Long, UserCounts> process() throws Exception {
                if (CollectionUtils.isEmpty(uidList)) {
                    return Collections.emptyMap();
                }

                return userCountMapper.getUserCountsMap(uidList);
            }
        }.execute();
    }
}
