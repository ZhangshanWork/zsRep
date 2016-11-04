package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserCounts;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * 操作user counts表的各种操作
 * Created by tim@vinci on 16/8/29.
 */
@Repository
public interface UserCountMapper {

    @Insert("insert into v_user_counts(user_id) value(#{userId})")
    int insertUserCount(@Param("userId") long userId);

    @Select("select * from v_user_counts where user_id = #{userId}")
    UserCounts getUserCount(@Param("userId") long userId);


    @Select({"<script>",
            "select * from v_user_counts where user_id in ",
            "<foreach item='item' index='index' collection='userIdList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    @MapKey("userId")
    Map<Long,UserCounts> getUserCountsMap(@Param("userIdList") Collection<Long> uidList);

    @Update("update v_user_counts set attentioner_count=attentioner_count+${delta},dt_update = now() " +
            "where user_id=#{userId} and attentioner_count <= ${max}")
    int addAttentionerCount(@Param("userId") long userId, @Param("delta") int delta, @Param("max") int maxAttentionCount);

    @Update("update v_user_counts set follower_count=follower_count+${delta},dt_update = now() where user_id=#{attention_user_id}")
    int addFollowerCount(@Param("attention_user_id") long attention_user_id, @Param("delta") int delta);

    @Update("update v_user_counts set feed_count=feed_count+${delta},dt_update = now() where user_id=#{userId}")
    int addFeedCount(@Param("userId") long userId, @Param("delta") int delta);


    @Update("update v_user_counts set message_unread_count=message_unread_count+${delta},dt_update = now() where user_id=#{userId}")
    int addMessageUnreadCount(@Param("userId") long userId, @Param("delta") int delta);

}
