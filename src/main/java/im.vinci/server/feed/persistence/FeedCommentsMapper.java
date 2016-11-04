package im.vinci.server.feed.persistence;

import im.vinci.server.feed.domain.FeedComments;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * FeedComments操作
 * Created by ASUS on 2016/8/10.
 */
@Repository
public interface FeedCommentsMapper {

    @Insert("INSERT INTO v_feed_comments(comment_id,feed_id,user_id,reply_to_user_id,comment_text)" +
            "VALUES(#{commentId},#{feedId},#{userId},#{replyToUserId},#{commentText})")
    int publishComments(FeedComments feedComments);

    @Update("update v_feed_comments set is_deleted=1, dt_update = now() where feed_id= #{feedId} and comment_id = #{commentId} and is_deleted = 0")
    int deleteComments(@Param("feedId") long feedId, @Param("commentId") long commentId);

    @Update("update v_feed_counts set comment_count= comment_count+1,dt_update = now() where feed_id = #{feedId}")
    int countPlus(@Param("feedId") long feedId);

    @Update("update v_feed_counts set comment_count= comment_count-1,dt_update = now() where feed_id = #{feedId}")
    int countMinus(@Param("feedId") long feedId);

    @Select("select * from v_feed_comments where feed_id=#{feedId} and comment_id=#{commentId}")
    FeedComments getCommentByFeedCommentId(@Param("feedId") long feedId, @Param("commentId") long commentId);

    @Select({"<script>",
            "select * from v_feed_comments where is_deleted = 0 and comment_id in ",
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    @MapKey("commentId")
    Map<Long,FeedComments> getCommentByCommentIds(@Param("ids") Collection<Long> ids);

    @Select("select * from v_feed_comments where feed_id = #{feedId} and is_deleted = 0 and comment_id < #{lastCommentId} order by comment_id desc limit #{pageSize}")
    List<FeedComments> listComments(@Param("feedId") long feedId, @Param("lastCommentId") long lastCommentId, @Param("pageSize") int pageSize);

    @Select("select * from v_feed_comments where feed_id = #{feedId} and is_deleted = 0 order by comment_id desc limit #{pageSize}")
    List<FeedComments> listFirstComments(@Param("feedId") long feedId, @Param("pageSize") int pageSize);

    @Select("select comment_count from v_feed_counts where feed_id = #{feedId}")
    int getTotalCount(@Param("feedId") long feedId);

}
