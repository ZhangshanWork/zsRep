package im.vinci.server.user.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.user.domain.UserAttention;
import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.persistence.UserAttentionMapper;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Created by mayuchen on 16/8/3.
 */
@Service
public class UserAttentionService {

    @Autowired
    private UserLoginAndBindDeviceService userLoginAndBindDeviceService;

    @Autowired
    private UserCountService userCountService;

    @Autowired
    private UserAttentionMapper userAttentionMapper;


    /**
     * 添加用户关注
     *
     * @return 返回关注是否成功
     */
    @Transactional
    public boolean insertUserAttention(final long userId, final long attentionUid) {
        return new BizTemplate<Boolean>("insertUserAttention") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(userId)) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户为空", "用户为空");
                }
                if (StringUtils.isEmpty(attentionUid)) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "关注用户为空", "被关注用户为空");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                UserInfo userInfo;
                UserAttention userAttention = new UserAttention();

                //验证被关注用户是否存在
                userInfo = userLoginAndBindDeviceService.checkUserInfo(attentionUid);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_USER_NOT_FOUND
                            , String.format("被关注用户不存在:uid:%s", attentionUid), "被关注用户不存在");
                }
                if (userId == userInfo.getId()) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_INSERT_YOURSELF
                            , String.format("用户%s不能关注自己", userInfo.getId()), "不能自己关注自己");
                }
                userAttention.setUserId(userId).setAttentionUserId(userInfo.getId());
                //验证用户是否已经添加对某用户的关注
                int num = userAttentionMapper.getUserAttentionBy_user_attention_id(userId, userInfo.getId());
                if (num > 0) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_ALREADY_INSERT
                            , String.format("用户%s已被用户%s关注", userInfo.getId(), userId), "用户已被用户关注");
                }
                userAttentionMapper.insertUserAttention(userAttention);
                if (!userCountService.adjustAttentionCount(userId,userInfo.getId(),false)) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_REACH_MAX_COUNT,"关注到达最大","关注到达最大");
                }
                return true;
            }
        }.execute();
    }

    /**
     * 删除用户关注
     *
     * @return 返回删除用户关注是否成功
     */
    @Transactional
    public boolean deleteUserAttention(final long userId, final long attentionUid) {
        return new BizTemplate<Boolean>("deleteUserAttention") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(userId)) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户为空", "用户为空");
                }
                if (StringUtils.isEmpty(attentionUid)) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "关注用户为空", "被关注用户为空");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                UserInfo userInfo;

                //验证被关注用户是否存在
                userInfo = userLoginAndBindDeviceService.checkUserInfo(attentionUid);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_USER_NOT_FOUND
                            , String.format("被关注用户不存在:uid:%s", attentionUid), "被关注用户不存在");
                }

                //验证用户是否已经添加对某用户的关注
                int num = userAttentionMapper.getUserAttentionBy_user_attention_id(userId, userInfo.getId());
                if (num == 0) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_NOT_EXIST
                            , String.format("用户%s未被用户%s关注", userInfo.getId(), userId), "用户未被用户关注,无法取消关注");
                }
                try {
                    userAttentionMapper.deleteUserAttention(userId, userInfo.getId());
                    userCountService.adjustAttentionCount(userId,userInfo.getId(),true);
                } catch (Exception e) {
                    logger.error(userId + " " + userInfo.getId() + ", 数据库错误", e);
                    throw new VinciException(ErrorCode.USER_ATTENTION_INSERT_ERROR, "删除数据项错误" + e.getMessage(), "删除用户关注错误");
                }
                return true;
            }
        }.execute();
    }

    /**
     * 获取一个用户所有关注列表
     */
    public List<Long> getUserAttentionListOnly(final long userId) {
        List<Long> list = userAttentionMapper.getUserAttetionListOnly(userId);
        if (list == null) {
            list = Lists.newArrayList();
        }
        list.add(userId);
        return list;
    }
    /**
     * 获取用户关注列表对应的用户信息表
     *
     * @return 返回用户关注列表对应的用户信息表
     */
    public List<UserAttention> getUserAttentionerList(final long userId, final long lastAttentionId, final long pageSize) {
        return new BizTemplate<List<UserAttention>>("getUserAttentionList") {

            @Override
            protected void checkParams() throws VinciException {
                if (userId<=0) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户为空", "用户为空");
                }
                if ( lastAttentionId < 0 ) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "页面偏移参数错误", "页面偏移参数错误");
                }
                if (pageSize > 50 || pageSize < 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的页面大小过大或者页面参数错误", "传入的页面大小过大或者页面参数错误");
                }
            }

            @Override
            protected List<UserAttention> process() throws Exception {
                //验证用户是否存在
                UserInfo userInfo = userLoginAndBindDeviceService.checkUserInfo(userId);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_USER_NOT_FOUND
                            , String.format("用户不存在:uid:%s", userId), "用户不存在");
                }
                try {
                    List<UserAttention> userAttentionList;
                    //先查询关注表
                    if(lastAttentionId == 0) {
                        userAttentionList = userAttentionMapper.getUserAttentionList(userId, pageSize);
                    } else{
                        userAttentionList = userAttentionMapper.getLaterAttentionId(userId, lastAttentionId, pageSize);
                    }
                    return mergeUserInfo(userAttentionList, true);

                } catch (Exception e) {
                    logger.error(userId + ", 数据库错误", e);
                    throw new VinciException(ErrorCode.USER_ATTENTION_INSERT_ERROR, "获取用户关注列表错误对应的用户信息表错误" + e.getMessage(), "获取用户关注列表错误,请重试");
                }
            }
        }.execute();
    }


    /**
     * 获取用户粉丝列表
     *
     * @return 返回用户粉丝列表
     */
    public List<UserAttention> getUserFollowerList(final long attentionUid, final long lastAttentionId, final long pageSize) {
        return new BizTemplate<List<UserAttention>>("getUserFollowList") {
            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(attentionUid)) {
                    throw new VinciException(ErrorCode.NEED_LOGIN, "用户为空", "用户为空");
                }
                if ( lastAttentionId < 0 ) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "页面偏移参数错误", "页面偏移参数错误");
                }
                if (pageSize > 50 || pageSize < 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的页面大小过大或者页面参数错误", "传入的页面大小过大或者页面参数错误");
                }

            }

            @Override
            protected List<UserAttention> process() throws Exception {


                //验证用户是否存在
                UserInfo userInfo = userLoginAndBindDeviceService.checkUserInfo(attentionUid);
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.USER_ATTENTION_USER_NOT_FOUND
                            , String.format("用户不存在:uid:%s", attentionUid), "用户不存在");
                }

                try {
                    List<UserAttention> userFollowers;
                    if(lastAttentionId == 0) {
                        userFollowers = userAttentionMapper.getUserFollowList(userInfo.getId(), pageSize);
                    } else{
                        userFollowers = userAttentionMapper.getLaterFollowList(userInfo.getId(), lastAttentionId, pageSize);
                    }
                    if (CollectionUtils.isEmpty(userFollowers)) {
                        return Collections.emptyList();
                    }
                    userFollowers = mergeUserInfo(userFollowers, false);
                    List<UserInfo> userInfos = Lists.newArrayListWithCapacity(userFollowers.size());
                    for (UserAttention attention : userFollowers) {
                        userInfos.add(attention.getUserInfo());
                    }
                    //查询我有没有关注我的粉丝
                    checkIsAttention(UserContext.getUserInfo().getId(),userInfos);

                    return userFollowers;

                } catch (Exception e) {
                    logger.error(userInfo.getId() + ", 数据库错误", e);
                    throw new VinciException(ErrorCode.USER_ATTENTION_INSERT_ERROR, "获取用户粉丝列表错误" + e.getMessage(), "获取用户粉丝列表错误");
                }
            }
        }.execute();
    }

    public Collection<UserInfo> checkIsAttention(long uid, Collection<UserInfo> userInfos) {
        if (CollectionUtils.isEmpty(userInfos)) {
            return userInfos;
        }
        Set<Long> attentions = Sets.newHashSet(getUserAttentionListOnly(uid));
        for (UserInfo info : userInfos) {
            if (info != null && (attentions.contains(info.getId()) || info.getId() == uid)) {
                info.setAttention(true);
            }
        }
        return userInfos;
    }

    private List<UserAttention> mergeUserInfo(List<UserAttention> attentions, boolean isAttention) {
        if (attentions == null || CollectionUtils.isEmpty(attentions)) {
            return Collections.emptyList();
        }
        List<Long> userIds = Lists.newArrayListWithCapacity(attentions.size());
        //再查询对应的用户信息表
        for(UserAttention userAttention : attentions){
            if (isAttention) {
                userIds.add(userAttention.getAttentionUserId());
            } else {
                userIds.add(userAttention.getUserId());
            }
        }
        Map<Long,UserInfo> map = userLoginAndBindDeviceService.getUserInfoMap(userIds);
        Iterator<UserAttention> iterator = attentions.iterator();
        while(iterator.hasNext()) {
            UserAttention attention = iterator.next();
            UserInfo userInfo = map.get(isAttention?attention.getAttentionUserId():attention.getUserId());
            if (userInfo == null) {
                iterator.remove();
            } else {
                attention.setUserInfo(userInfo);
                if (isAttention) {
                    userInfo.setAttention(true);
                }
            }
        }
        return attentions;
    }

}
