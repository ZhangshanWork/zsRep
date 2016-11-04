package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserMessage;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Created by mayuchen on 16/8/10.
 */
@Repository
public interface UserMessageMapper {

    @Select("select * from v_message_box where message_uid=#{messageUid}")
    UserMessage getUserMessageByUid(@Param("messageUid") long messageUid);

    //查询某用户未读消息数量
    @Select("select count(*) from v_message_box where id=#{id} and is_read=0")
    UserMessage getUserMessageCountById(@Param("id") long id);

    //查询某用户消息箱的最新n条消息
    @Select("select * from v_message_box where user_to_id=#{user_to_id} and message_type=#{message_type} order by message_uid desc limit #{pageSize}")
    List<UserMessage> getUserMessageList(@Param("user_to_id") long userToId, @Param("message_type") String message_type, @Param("pageSize") long pageSize);

    //查询某用户消息箱比last_message新的n条消息
    @Select("select * from v_message_box where user_to_id=#{user_to_id} and message_type=#{message_type} and message_uid<#{id} order by message_uid desc limit #{pageSize}")
    List<UserMessage> getLaterMessageListById(@Param("user_to_id") long userToId, @Param("message_type") String messageType, @Param("id") long id, @Param("pageSize") long pageSize);

    @Select("select count(*) from v_message_box where user_to_id=#{user_to_id} and message_type=#{message_type}")
    int getUserMessageCount(@Param("user_to_id") long userToId, @Param("message_type") String messageType);

//    //查询消息箱中评论的具体信息
//    @Select({"<script>",
//            "select * from v_comments where id in ",
//            "<foreach item='item' index='index' collection='commentsIdList' open='(' separator=',' close=')'>",
//            "#{item}",
//            "</foreach>",
//            "</script>"})
//    @MapKey("id")
//    HashMap<String, FeedComments> getUserMessageCommentList(@Param("commentsIdList")Collection<Long> commentsIdList);
//
//    //查询消息箱中帖子的具体信息
//    @Select({"<script>",
//            "select * from v_feed where feed_id in ",
//            "<foreach item='item' index='index' collection='feedIdList' open='(' separator=',' close=')'>",
//            "#{item}",
//            "</foreach>",
//            "</script>"})
//    @MapKey("feedId")
//    HashMap<Long, Feed> getUserFeedList(@Param("feedIdList")Collection<Long> feedIdList);

    //向消息箱表中插入消息(默认未读)
    @Insert("insert into v_message_box " +
            "(message_uid, user_from_id, user_to_id, message_type, message_body, content, is_read, dt_create, dt_update) "+
            "values " +
            "(#{messageUid}, #{userFromId}, #{userToId}, #{messageType}, #{messageBody}, #{content}, 0, now(),now())")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertUserMessage(UserMessage userMessage);


    //更新消息箱表中消息(未读->已读)
    @Update({"<script>",
            "update v_message_box set is_read=1, dt_update=now() where user_to_id=#{user_to_id} and message_uid in ",
            "<foreach item='item' index='index' collection='messageList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    int setMessageRead(@Param("messageList") List<Long> messageList, @Param("user_to_id") long toUserId);


    //删除消息箱表中消息
    @Update("delete from v_message_box where message_uid=#{messageUid} and user_from_id=#{userFromId}")
    int deleteUserMessage(UserMessage userMessage);

}







