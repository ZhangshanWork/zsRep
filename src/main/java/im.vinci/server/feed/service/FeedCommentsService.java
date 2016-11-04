package im.vinci.server.feed.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.FeedComments;
import im.vinci.server.feed.domain.wrapper.ListFeedCommentsResponse;
import im.vinci.server.feed.persistence.FeedCommentsMapper;
import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.service.UserAttentionService;
import im.vinci.server.user.service.UserLoginAndBindDeviceService;
import im.vinci.server.user.service.UserMessageService;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.LocalIdGenerator;
import im.vinci.server.utils.StringContentUtils;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Feed评论服务
 * Created by ASUS on 2016/8/10.
 */
@Service
public class FeedCommentsService {

    @Autowired
    private FeedCommentsMapper feedCommentsMapper;

    @Autowired
    private FeedService feedService;

    @Autowired
    private UserAttentionService userAttentionService;

    @Autowired
    private UserLoginAndBindDeviceService userService;

    @Autowired
    private UserMessageService userMessageService;

    /**
     * 发表Feed评论
     */
    @Transactional
    public boolean publishComments(final FeedComments feedComments) {
        return new BizTemplate<Boolean>("feedComments.publish") {
            @Override
            protected void checkParams() throws VinciException {
                if (feedComments == null) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入参数", "没有传入参数");
                }
                int length = StringContentUtils.countRealLength(feedComments.getCommentText());
                if (length <= 0 || length > 240) {
                    throw new VinciException(ErrorCode.FEED_COMMENTS_CONTENT_UNMATCH_RULE, "长度不符合规则", "内容长度不符合规则");
                }
                if (StringContentUtils.hasUnshowChar(feedComments.getCommentText())) {
                    throw new VinciException(ErrorCode.FEED_COMMENTS_CONTENT_UNMATCH_RULE, "内容中有非显示字符", "内容有不符合规则字符");
                }
            }

            @Override
            protected Boolean process() throws VinciException {
                if (!feedService.isFeedExist(feedComments.getFeedId())) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_FEED_NOT_EXIST_ERROR, "发布feed comment feed不存在" + feedComments.getFeedId(), "要评论的帖子不存在");
                }
                UserInfo replyUserInfo = null;
                if (feedComments.getReplyToUserId() > 0) {
                    replyUserInfo = userService.checkUserInfo(feedComments.getReplyToUserId());
                    if (replyUserInfo == null) {
                        throw new VinciException(ErrorCode.FEED_COMMENTS_PUBLISH_REPLY_USER_NOT_EXIST, "回复的用户不存在:" + feedComments.getReplyToUserId(), "回复的用户不存在");
                    }
                }
                feedComments.setCommentId(LocalIdGenerator.INSTANCE.generateId());
                feedComments.setUserId(UserContext.getUserInfo().getId());
                feedCommentsMapper.publishComments(feedComments);
                feedCommentsMapper.countPlus(feedComments.getFeedId());
                if (replyUserInfo != null) {
                    userMessageService.insertCommentMeAtFeedUserMessage(UserContext.getUserInfo(), replyUserInfo, feedComments);
                }
                return true;
            }
        }.execute();
    }

    /**
     * 删除Feed评论
     */
    @Transactional
    public boolean deleteComments(final long feedId, final long commentId) {
        return new BizTemplate<Boolean>("feedComments.delete") {

            @Override
            protected void checkParams() throws VinciException {
                if (feedId <=0 || commentId <= 0) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_DELETE_NOT_EXIST_ERROR, "传入commentsId为空或不合法", "没有选择评论或不合法");
                }

            }

            @Override
            protected Boolean process() throws VinciException {
                FeedComments comment = feedCommentsMapper.getCommentByFeedCommentId(feedId, commentId);
                if (comment == null) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_DELETE_NOT_EXIST_ERROR, "删除Feed评论不存在", "该评论不存在");
                }
                if (comment.getIsDeleted() == 1) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_HAS_DELETED_ERROR, "删除Feed评论不存在,已删除", "该评论已被删除");
                }
                if (UserContext.getUserInfo().getId() != comment.getUserId()) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_DELETE_NOT_OWN_ERROR, "删除非本人FEED评论", "删除非本人FEED评论");
                }
                if (feedCommentsMapper.deleteComments(feedId, commentId) == 0) {
                    throw new VinciException(ErrorCode.FEED_COMMENT_DELETE_NOT_EXIST_ERROR, "删除Feed评论不存在", "该评论不存在");
                }
                feedCommentsMapper.countMinus(comment.getFeedId());
                return true;
            }
        }.execute();

    }

    /**
     * 用于提供给其他service或controller批量拿到feed评论的接口
     */
    public Map<Long,ListFeedCommentsResponse> getFeedCommentByIds(final Collection<Long> feedIds , final Collection<Long> commentIds) {
        return new BizTemplate<Map<Long,ListFeedCommentsResponse>>("FeedCommentsService.getFeedCommentByIds") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Map<Long,ListFeedCommentsResponse> process() throws Exception {
                if (CollectionUtils.isEmpty(commentIds)) {
                    return Collections.emptyMap();
                }
                Map<Long,FeedComments> commentsMap = feedCommentsMapper.getCommentByCommentIds(commentIds);

                Map<Long,ListFeedCommentsResponse> result = Maps.newHashMapWithExpectedSize(commentsMap.size());

                Set<Long> userIds = Sets.newHashSetWithExpectedSize(commentsMap.size());
                commentsMap.values().stream().forEach(feed -> userIds.add(feed.getUserId()));

                Map<Long,UserInfo> userInfoMap  = userService.getUserInfoMap(userIds);
                if (UserContext.getUserInfo() != null) {
                    userAttentionService.checkIsAttention(UserContext.getUserInfo().getId(),userInfoMap.values());
                }

                commentsMap.values().stream().forEach(feedCommentsTemp -> {
                    ListFeedCommentsResponse listFeedCommentsResponseTemp = new ListFeedCommentsResponse();

                    listFeedCommentsResponseTemp.setCommentId(feedCommentsTemp.getCommentId());
                    listFeedCommentsResponseTemp.setFeedId(feedCommentsTemp.getFeedId());
                    listFeedCommentsResponseTemp.setCommentText(feedCommentsTemp.getCommentText());
                    listFeedCommentsResponseTemp.setDeleted(feedCommentsTemp.getIsDeleted()==1);
                    listFeedCommentsResponseTemp.setDtCreate(feedCommentsTemp.getDtCreate());
                    listFeedCommentsResponseTemp.setDtUpdate(feedCommentsTemp.getDtUpdate());
                    listFeedCommentsResponseTemp.setUserInfo(userInfoMap.get(feedCommentsTemp.getUserId()));

                    result.put(feedCommentsTemp.getCommentId(),listFeedCommentsResponseTemp);
                });
                return result;
            }
        }.execute();
    }

    /**
     * Feed评论列表及对应用户信息表
     */
    public ResponsePageVo<ListFeedCommentsResponse> listCommentsAndUserInfo(final long feedId, final long lastCommentId, final int pageSize) {
        return new BizTemplate<ResponsePageVo<ListFeedCommentsResponse>>("feedComments.listCommentsAndUserInfo") {

            @Override
            protected void checkParams() throws VinciException {
                if (feedId == 0) {
                    throw new VinciException(ErrorCode.FEED_DELETE_NO_FEED_ID_ERROR, "传入feed为空或不合法", "没有选择帖子");
                }
                if (pageSize <= 0 || pageSize > 100) {
                    throw new VinciException(ErrorCode.FEED_COMMENTS_LIST_PARAMETER_ERROR, "page或pageSize不合法", "参数不合法");
                }
            }

            @Override
            protected ResponsePageVo<ListFeedCommentsResponse> process() throws VinciException {
                ResponsePageVo<ListFeedCommentsResponse> result = new ResponsePageVo<>();

                List<FeedComments> commentsList;
                if (lastCommentId > 0) {
                    commentsList = feedCommentsMapper.listComments(feedId, lastCommentId, pageSize+1);
                } else {
                    commentsList = feedCommentsMapper.listFirstComments(feedId, pageSize+1);
                }
                int totalCount = feedCommentsMapper.getTotalCount(feedId);

                List<ListFeedCommentsResponse> responseComments = Lists.newArrayListWithExpectedSize(commentsList.size());

                //获取用户信息,判断所有用户的关注关系
                List<Long> uidList = new ArrayList<>();
                for (FeedComments feedCommentsTemp : commentsList) {
                    uidList.add(feedCommentsTemp.getUserId());
                }
                Map<Long, UserInfo> userInfoMap = userService.getUserInfoMap(uidList);
                userAttentionService.checkIsAttention(UserContext.getUserInfo().getId(), userInfoMap.values());

                for (int i=0; i<pageSize && i < commentsList.size(); i++) {
                    FeedComments feedCommentsTemp = commentsList.get(i);

                    ListFeedCommentsResponse listFeedCommentsResponseTemp = new ListFeedCommentsResponse();

                    listFeedCommentsResponseTemp.setCommentId(feedCommentsTemp.getCommentId());
                    listFeedCommentsResponseTemp.setFeedId(feedCommentsTemp.getFeedId());
                    listFeedCommentsResponseTemp.setCommentText(feedCommentsTemp.getCommentText());
                    listFeedCommentsResponseTemp.setDeleted(feedCommentsTemp.getIsDeleted()==1);
                    listFeedCommentsResponseTemp.setDtCreate(feedCommentsTemp.getDtCreate());
                    listFeedCommentsResponseTemp.setDtUpdate(feedCommentsTemp.getDtUpdate());
                    listFeedCommentsResponseTemp.setUserInfo(userInfoMap.get(feedCommentsTemp.getUserId()));

                    responseComments.add(listFeedCommentsResponseTemp);
                }

                result.setData(responseComments);
                result.setPageSize(pageSize);
                result.setTotalCount(totalCount);
                result.setHasMore(commentsList.size()>pageSize);
                return result;
            }
        }.execute();

    }

}
