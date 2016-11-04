package im.vinci.server.user.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.google.common.collect.Lists;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.feed.domain.FeedComments;
import im.vinci.server.user.domain.UserInfo;
import im.vinci.server.user.domain.UserMessage;
import im.vinci.server.user.domain.UserMessageType;
import im.vinci.server.user.persistence.UserMessageMapper;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.LocalIdGenerator;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResponsePageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Created by mayuchen on 16/8/10.
 */
@Service
public class UserMessageService {

    @Autowired
    private UserLoginAndBindDeviceService userService;

    @Autowired
    private UserCountService userCountService;

    @Autowired
    private UserMessageMapper userMessageMapper;


    /**
     * 添加用户消息
     *
     * @return 返回添加用户消息Uid
     */
    @Transactional
    public UserMessage insertCommentMeAtFeedUserMessage(final UserInfo userInfo, final UserInfo replyUserInfo, final FeedComments comments) {
        return new BizTemplate<UserMessage>("insertMessage") {

            @Override
            protected void checkParams() throws VinciException {
                if (userInfo == null || replyUserInfo == null || comments == null) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "feed或者comment不存在", "feed或者comment不存在");
                }
            }


            @Override
            protected UserMessage process() throws Exception {
                UserMessage userMessage = new UserMessage();
                //生成 message_uid
                userMessage.setMessageUid(LocalIdGenerator.INSTANCE.generateId());
                userMessage.setUserFromId(userInfo.getId());
                userMessage.setUserToId(replyUserInfo.getId());
                userMessage.setMessageType(UserMessageType.CommentInFeed);
                userMessage.setMessageBody(JsonNodeFactory.instance.objectNode()
                        .put("feed_id", comments.getFeedId()).put("comment_id", comments.getCommentId()));
                userMessage.setContent(comments.getCommentText());
                if (userMessageMapper.insertUserMessage(userMessage) > 0) {
                    userCountService.adjustMessageUnreadCount(replyUserInfo.getId(), 1);
                }
                return userMessage;
            }
        }.execute();
    }


    /**
     * 查询用户消息箱中某种类型的评论列表(按时间倒序)
     *
     * @return 返回更新用户讯息是否成功
     */
    @Transactional
    public ResponsePageVo<UserMessage> selectMessageList(final long uid, final UserMessageType messageType, final long lastMessageUid, final int pageSize) {
        return new BizTemplate<ResponsePageVo<UserMessage>>("updateMessageIsRead") {


            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(messageType)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入参数错误", "传入参数错误不全");
                }
                if (pageSize > 50 || pageSize < 0) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的页面大小过大或者页面参数错误", "传入的页面大小过大或者页面参数错误");
                }
                if (StringUtils.isEmpty(lastMessageUid)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的页面偏移量错误", "传入的页面偏移量错误");
                }
            }

            @Override
            protected ResponsePageVo<UserMessage> process() throws Exception {
                List<UserMessage> userMessageList;
                UserInfo userInfo;
                if (UserContext.getUserInfo() != null && UserContext.getUserInfo().getId() == uid) {
                    userInfo = UserContext.getUserInfo();
                } else {
                    userInfo = userService.checkUserInfo(uid);
                }
                if (userInfo == null) {
                    throw new VinciException(ErrorCode.API_USER_NOT_EXISTS, "用户不存在:" + uid, "用户不存在");
                }
                //1.得到userMessage表
                if (lastMessageUid == 0) {
                    //若lastMessageUid为0,则取最新的message数据
                    userMessageList = userMessageMapper.getUserMessageList(uid, messageType.name(), pageSize+1);
                } else {
                    userMessageList = userMessageMapper.getLaterMessageListById(uid, messageType.name(), lastMessageUid, pageSize+1);
                }
//                int totalCount = userMessageMapper.getUserMessageCount(uid, messageType);
                if (CollectionUtils.isEmpty(userMessageList)) {
                    return new ResponsePageVo<UserMessage>().setHasMore(false).setPageSize(pageSize).setData(Collections.emptyList());
                }
                List<UserMessage> userMessageResult = Lists.newArrayListWithCapacity(userMessageList.size());
                List<Long> userMessageUnreadIds = Lists.newArrayListWithCapacity(userMessageList.size());
                for (int i=0; i<pageSize && i<userMessageList.size(); i++) {
                    UserMessage message = userMessageList.get(i);
                    userMessageResult.add(message);
                    if (!message.isRead()) {
                        userMessageUnreadIds.add(message.getMessageUid());
                    }
                }
                if (userMessageUnreadIds.size() > 0) {
                    int changeNum = userMessageMapper.setMessageRead(userMessageUnreadIds, uid);
                    userCountService.adjustMessageUnreadCount(userInfo.getId(), -changeNum);
                }

                return new ResponsePageVo<UserMessage>()
                        .setHasMore(userMessageList.size()>userMessageResult.size())
                        .setPageSize(pageSize).setData(userMessageResult);
                //2.得到 Message-Comments 映射关系
//                    HashMap<String, FeedComments> messageCommentsMap;
//                    List<Long> commentsList = new ArrayList<>();
//                    for(UserMessage userMessage : userMessageList){
//                        commentsList.add(userMessage.getCommentsId());
//                    }
//                    messageCommentsMap = userMessageMapper.getUserMessageCommentList(commentsList);
//                    if(commentsList.isEmpty()){
//                        return new ArrayList<UserMessage>();
//                    }
//                    logger.error("得到 Message-Comments 映射关系");
//                    //3.得到 Message-Feed 映射关系
//                    HashMap<Long, Feed> messageFeedMap;
//                    List<Long> feedList = new ArrayList<>();
//                    FeedComments feedComments;
//                    Iterator<FeedComments> iter = messageCommentsMap.values().iterator();
//                    while (iter.hasNext()) {
//                        feedComments = iter.next();
//                        feedList.add(feedComments.getFeedId());
//                    }
//                    messageFeedMap = userMessageMapper.getUserFeedList(feedList);
//                    if(commentsList.isEmpty()){
//                        return new ArrayList<UserMessage>();
//                    }
//                    logger.error("得到 Message-Feed 映射关系");
//                    //4.得到 Message-UserInfo 映射关系
//                    Map<Long, UserInfo> messageUserInfoMap;
//                    List<Long> commentsUserList = new ArrayList<>();
//                    for(UserMessage userMessage : userMessageList){
//                        commentsUserList.add(userMessage.getUserFromId());
//                    }
//                    messageUserInfoMap  =  realUserAndLoginMapper.getUserInfoListById(commentsUserList);
//
//                    if(messageUserInfoMap.isEmpty()){
//                        return new ArrayList<UserMessage>();
//                    }
//                    logger.error("得到 Message-UserInfo 映射关系");
//
//                    //5.将userMessage、feedComments组合
//                    for(UserMessage userMessage : userMessageList){
//                        userMessage.setFeedComments(messageCommentsMap.get(userMessage.getCommentsId()));
//                        userMessage.setFeed(messageFeedMap.get(userMessage.getFeedComments().getFeedId()));
//                        userMessage.setUserInfo(messageUserInfoMap.get(userMessage.getUserFromId()));
//                    }
//                    logger.error("得到 Message-UserInfo 映射关系");
//                    //6.将userMessage、feedComments组合
//                    List<Long> messageIdList = new ArrayList<Long>();
//                    for(UserMessage messageRead : userMessageList) {
//                        messageIdList.add(messageRead.getId());
//                    }
//                    if(userMessageMapper.setMessageRead(messageIdList) <= 0){
//                        throw new VinciException(ErrorCode.USER_MESSAGE_DATABASE_ERROR, "updateMessageIsRead_更新未读数据项错误", "更新未读数据项错误,请重试");
//                    }
            }
        }.execute();
    }
}


