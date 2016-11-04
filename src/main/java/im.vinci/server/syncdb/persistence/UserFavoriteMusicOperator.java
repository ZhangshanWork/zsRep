package im.vinci.server.syncdb.persistence;

import im.vinci.server.syncdb.domain.ClientUserData;
import im.vinci.server.syncdb.domain.UserFavoriteMusic;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by mingjie on 16/10/18.
 */
@Repository
public interface UserFavoriteMusicOperator {

    @Insert({"<script>",
            "insert ignore into user_favorite_music",
            "(user_id, artist, artist_id, artist_subtitle, album_id, album_logo, album_name, album_subtitle, music_id, play_seconds, song_name, song_subtitle, last_favorite_time) values ",
            "<foreach item='item' index='index' collection='list' separator=','>",
            "(#{item.userId}, #{item.artist}, #{item.artistId}, #{item.artistSubTitle}, #{item.albumId}, #{item.albumLogo}, #{item.albumName}, #{item.albumSubTitle}, #{item.musicId}, #{item.playSeconds}, #{item.songName}, #{item.songSubTitle}, #{item.lastFavoriteTime})",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertData(@Param("list") Collection<UserFavoriteMusic> list);

    @Delete({"<script>",
            "delete from user_favorite_music where",
            "<foreach item='item' index='index' collection='list' separator=','>",
            "user_id=#{item.userId} and artist_id=#{item.artistId} and album_id=#{item.albumId} and music_id=#{item.musicId}",
            "</foreach>",
            "</script>"
            })
    int deleteData(@Param("list") Collection<UserFavoriteMusic> list);

    @Select({"<script>",
            "select * from user_favorite_music where user_id=#{user_id} ",
            "<choose>",
            "<when test='last_favorite_music_id > 0'>",
            "and id &lt;= #{last_favorite_music_id} ",
            "order by id desc limit 1, #{page_size}",
            "</when>",
            "<otherwise>",
            "order by id desc limit #{page_size}",
            "</otherwise>",
            "</choose>",
            "</script>"
    })
    List<UserFavoriteMusic> getUserData(@Param("user_id") long userId, @Param("last_favorite_music_id") long lastFavoriteMusicId, @Param("page_size") int pageSize);

}
