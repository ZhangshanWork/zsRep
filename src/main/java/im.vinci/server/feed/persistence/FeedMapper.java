package im.vinci.server.feed.persistence;

import im.vinci.server.feed.domain.Feed;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;

/**
 * Feed操作
 * Created by frank on 16-8-5.
 */
@Repository
public interface FeedMapper {

    @Insert("INSERT INTO v_feed(user_id,feed_id,content,topic,page_type,page_content)" +
            "VALUES(#{userId},#{feedId},#{content},#{topic},#{pageType},#{pageContent})")
    int publishFeed(Feed feed);

    @Insert("INSERT INTO v_feed_counts(feed_id) value(#{feedId})")
    int insertCount(@Param("feedId") long feedId);

    @Update("update v_feed set is_deleted=1 where feed_id = #{feedId}")
    int deleteFeed(@Param("feedId") long feedId);

    @Select({"<script>",
            "select * from v_feed where is_deleted=0 and feed_id in ",
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    @MapKey("feedId")
    Map<Long,Feed> getFeedByIds(@Param("ids") Collection<Long> ids);

    @Select("select user_id from v_feed where feed_id=#{id} and is_deleted=0")
    Long getFeedUserById(@Param("id") long feedId);
}
