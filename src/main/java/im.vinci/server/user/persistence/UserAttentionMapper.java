package im.vinci.server.user.persistence;

import im.vinci.server.user.domain.UserAttention;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by mayuchen on 16/8/2.
 */
@Repository
public interface UserAttentionMapper {

    @Select("select * from v_attention where id=#{id}")
    UserAttention getUserAttentionById(@Param("id") long id);

    @Select("select attention_user_id from v_attention where user_id=#{userId}")
    List<Long> getUserAttetionListOnly(@Param("userId") long userId);

    //用户关注查询(2次)
    //lastAttentionId=0,查询最新的
    @Select("select * from v_attention where user_id=#{userId} order by id desc limit 0, #{pageSize}")
    List<UserAttention> getUserAttentionList(@Param("userId") long userId, @Param("pageSize") long pageSize);
    //lastAttentionId!=0,查某id之后的
    @Select("select * from v_attention where user_id=#{userId} and id<#{lastAttentionId} order by id desc limit 0, #{pageSize}")
    List<UserAttention> getLaterAttentionId(@Param("userId") long userId, @Param("lastAttentionId") long lastAttentionId, @Param("pageSize") long pageSize);

    //用户粉丝查询(2次)
    //lastAttentionId=0,查询最新的
    @Select("select * from v_attention where attention_user_id=#{attentionUserId} order by id desc limit 0, #{pageSize}")
    List<UserAttention> getUserFollowList(@Param("attentionUserId") long attentionUserId, @Param("pageSize") long pageSize);
    //lastAttentionId!=0,查某id之后的
    @Select("select * from v_attention where attention_user_id=#{attentionUserId} and id<#{lastAttentionId} order by id desc limit 0, #{pageSize}")
    List<UserAttention> getLaterFollowList(@Param("attentionUserId") long attentionUserId, @Param("lastAttentionId") long lastAttentionId, @Param("pageSize") long pageSize);

    //我是否关注了我的粉丝
    @Select({"<script>",
            "select attention_user_id from v_attention ",
            "where user_id=#{userId} and attention_user_id in ",
            "<foreach item='item' index='index' collection='attentionList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    Set<Long> getIsAttentionList(@Param("userId") long userId, @Param("attentionList") Collection<Long> attentionList);


    @Select("select count(*) from v_attention where user_id=#{userId} and attention_user_id=#{attentionUserId}")
    int getUserAttentionBy_user_attention_id(@Param("userId") long userId, @Param("attentionUserId") long attentionUserId);


    @Insert("insert into v_attention " +
            "(user_id, attention_user_id, dt_create, dt_update) "+
            "values " +
            "(#{userId}, #{attentionUserId}, now(),now())")
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertUserAttention(UserAttention userAttention);



    @Delete("delete from v_attention where user_id=#{userId} and attention_user_id=#{attentionUserId}")
    int deleteUserAttention(@Param("userId") long userId, @Param("attentionUserId") long attentionUserId);
}
