package im.vinci.server.user.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.Feed;
import im.vinci.server.feed.domain.wrapper.ListFeedCommentsResponse;
import im.vinci.server.feed.service.FeedCommentsService;
import im.vinci.server.feed.service.FeedService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.user.domain.UserMessage;
import im.vinci.server.user.domain.UserMessageType;
import im.vinci.server.user.domain.wrappers.UserMessageComments;
import im.vinci.server.user.service.UserLoginAndBindDeviceService;
import im.vinci.server.user.service.UserMessageService;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import im.vinci.server.utils.apiresp.ResultObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户消息相关的接口
 * Created by mayuchen on 16/8/11.
 */
@RestController
@RequestMapping(value = "/vinci/user", produces = "application/json;charset=UTF-8")
public class UserMessageController {

    @Autowired
    private UserMessageService userMessageService;      //feedMessage.setUserFromId(111);

    @Autowired
    private FeedService feedService;

    @Autowired
    private FeedCommentsService feedCommentsService;

    @Autowired
    private UserLoginAndBindDeviceService UserLoginAndBindDeviceService;

    /**
     * 添加某用户消息, 暂时不允许自助添加消息
     * Param:   (String) user_to_uid, (String) comments_id, (String) message_type;
     */
//    @RequestMapping(value = "/message/add")
//    @ApiSecurityLabel(isCheckLogin = true)
//    public ResultObject<UserMessage> MessageAdd(@RequestParam("user_to_uid") long userToUid,
//                                                @RequestParam("comments_id") long commentsId,
//                                                @RequestParam("message_type") String messageType) {
//        UserInfo toUserInfo = UserLoginAndBindDeviceService.checkUserInfo(userToUid);
//        if (toUserInfo == null) {
//            throw new VinciException(ErrorCode.API_USER_NOT_EXISTS, "要发送消息to user(" + userToUid + ") 不存在", "要发送的用户不存在");
//        }
//        UserMessage userMessage = new UserMessage();
//        userMessage.setUserToId(toUserInfo.getId());
//        userMessage.setUserFromId(UserContext.getUserInfo().getId());
//        userMessage.setCommentsId(commentsId);
//        userMessage.setMessageType(messageType);
//        return new ResultObject<>(userMessageService.insertMessage(userMessage));
//    }


    /**
     * 显示用户最新未读消息
     * Param: (String) message_type, (String) offset, (String) page_size;
     */
    @RequestMapping(value = "/message/list", method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<ResponsePageVo<UserMessageComments>> userMessageList(@RequestParam("message_type") String messageType,
                                                                             @RequestParam(value = "last_message_uid", defaultValue = "0") long last_message_uid,
                                                                             @RequestParam(value = "page_size", defaultValue = "10") int pageSize) {
        UserMessageType userMessageType = UserMessageType.value(messageType);
        if (userMessageType == null) {
            throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有这个消息类型:" + messageType, "没有这个消息类型");
        }
        switch (userMessageType) {
            case CommentInFeed:
                break;
            default:
                throw new VinciException(ErrorCode.ARGUMENT_ERROR, "暂时不支持这个消息类型:" + messageType, "暂时不支持这个消息类型");
        }
        ResponsePageVo<UserMessage> userMessageList = userMessageService.selectMessageList(UserContext.getUserInfo().getId(),
                userMessageType, last_message_uid, pageSize);

        ResponsePageVo<UserMessageComments> result = new ResponsePageVo<>();
        result.setHasMore(userMessageList.getHasMore());
        result.setPageSize(userMessageList.getPageSize());

        if (CollectionUtils.isEmpty(userMessageList.getData())) {
            return new ResultObject<>(result.setData(Collections.emptyList()));
        }
        switch (userMessageType) {
            case CommentInFeed:
                result.setData(getFeedAndComments(userMessageList.getData()));
                break;
            default:
                throw new VinciException(ErrorCode.ARGUMENT_ERROR, "暂时不支持这个消息类型:" + messageType, "暂时不支持这个消息类型");
        }
        return new ResultObject<>(result);
    }

    private List<UserMessageComments> getFeedAndComments(List<UserMessage> data) {
        if (CollectionUtils.isEmpty(data)) {
            return Collections.emptyList();
        }
        Set<Long> feedIdSet = Sets.newHashSetWithExpectedSize(data.size());
        Set<Long> commentIdSet = Sets.newHashSetWithExpectedSize(data.size());
        for (UserMessage userMessage : data) {
            if (userMessage == null) {
                continue;
            }
            JsonNode feedIdNode = userMessage.getMessageBody().get("feed_id");
            JsonNode commentIdNode = userMessage.getMessageBody().get("comment_id");
            if (feedIdNode == null || !feedIdNode.isNumber() || commentIdNode == null || !commentIdNode.isNumber()) {
                continue;
            }
            long feedId = feedIdNode.asLong();
            long commentId = commentIdNode.asLong();
            feedIdSet.add(feedId);
            commentIdSet.add(commentId);
        }
        Map<Long,Feed> feedMap = feedService.getFeedByIds(feedIdSet);
        Map<Long,ListFeedCommentsResponse> commentMap = feedCommentsService.getFeedCommentByIds(feedIdSet,commentIdSet);

        List<UserMessageComments> result = Lists.newArrayListWithExpectedSize(data.size());
        for (UserMessage userMessage : data) {
            if (userMessage == null) {
                continue;
            }
            JsonNode feedIdNode = userMessage.getMessageBody().get("feed_id");
            JsonNode commentIdNode = userMessage.getMessageBody().get("comment_id");
            if (feedIdNode == null || !feedIdNode.isNumber() || commentIdNode == null || !commentIdNode.isNumber()) {
                continue;
            }
            long feedId = feedIdNode.asLong();
            long commentId = commentIdNode.asLong();

            UserMessageComments userMessageComment = new UserMessageComments();
            Feed feed = feedMap.get(feedId);
            if (feed == null) {
                feed = Feed.getDeleteFeedInstance(feedId);
            }
            ListFeedCommentsResponse comment = commentMap.get(commentId);
            if (comment == null) {
                comment = ListFeedCommentsResponse.getDeleteCommentInstance(feedId, commentId);
            }
            userMessageComment.setFeed(feed).setFeedComments(comment).setFeedMessage(userMessage);
            result.add(userMessageComment);
        }
        return result;
    }
}