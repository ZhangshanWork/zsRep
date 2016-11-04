package im.vinci.server.discovery.persistence;

import im.vinci.server.discovery.domain.Category;
import im.vinci.server.discovery.domain.Channel;
import im.vinci.server.discovery.domain.MusicAlbum;
import im.vinci.server.discovery.domain.MusicSong;
import im.vinci.server.discovery.domain.wrappers.AlbumSongResponce;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/**
 * Created by ASUS on 2016/8/24.
 */
@Repository
public interface DiscoveryHomePageMapper {

    //插入虾米专辑
    @Insert("insert into d_music_album(album_id,album_name,album_logo,artist_id,artist_name,artist_logo," +
            "description,song_count,company,play_counts)" +
            " values(#{album_id},#{album_name},#{album_logo},#{artist_id},#{artist_name},#{artist_logo},"+
            "#{desc},#{song_count},#{company},#{play_counts})")
    int insertAlbum(im.vinci.server.search.domain.music.MusicAlbum musicAlbum);
    //插入虾米音乐
    @Insert("insert into d_music_song(song_id,song_type,song_name,artist," +
            "artist_id,artist_logo,album_name,album_id,play_seconds,album_logo,listen_file," +
            "lyric_file,singers,play_counts)" +
            "values(#{song_id},#{song_type},#{song_name},#{artist},"+
            "#{artist_id},#{artist_logo},#{album_name},#{album_id},#{play_seconds},#{album_logo},#{listen_file}," +
            "#{lyric_file},#{singers},#{play_counts})")
    int insertSong(im.vinci.server.search.domain.music.MusicSong musicSong);

    //获得频道列表
    @Select("select * from d_channel")
    List<Channel> getChannelList();

    //获取专辑id列表
    @Select("select album_id from d_channel_album where channel_id = #{channelId}")
    List<Long> getAlbumIdList(@Param("channelId") long channelId);

    //获得分类列表
    @Select("select * from d_category")
    List<Category> getCategoryList();

    //获得分类数据
    @Select("select * from d_album_song_list where category_id = #{categoryId}")
    List<AlbumSongResponce> getAlbumSongListList(@Param("categoryId") long categoryId);

    //获得专辑或歌曲id列表
    @Select("select album_song_id from d_album_song where album_song_list_id = #{albumSongListId}")
    List<Long> getAlbumSongList(@Param("albumSongListId") long albumSongListId);

    //根据专辑id列表获得专辑列表
    @Select({"<script>",
            "select * from d_music_album where album_id in ",
            "<foreach item='item' index='index' collection='albumIdList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<MusicAlbum> getMusicAlbumList(@Param("albumIdList") Collection<Long> albumIdList);

    //根据歌曲id列表获得歌曲列表
    @Select({"<script>",
            "select * from d_music_song where song_id in ",
            "<foreach item='item' index='index' collection='songIdList' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    List<MusicSong> getMusicSongList(@Param("songIdList") Collection<Long> songIdList);

    //根据专辑Id获得歌曲列表
    @Select("select * from d_music_song where album_id = #{albumId}")
    List<MusicSong> getSongList(@Param("albumId") long albumId);

    //根据频道id获得专辑id列表
    @Select("select album_id from d_channel_album where channel_id = #{channelId}")
    List<Long> getChannelAlbumList(@Param("channelId") long channelId);

    //根据album_song_list_id获得album_song_id列表
    @Select("select album_song_id from d_album_song where album_song_list_id = #{albumSongListId}")
    List<Long> getCategoryAlbumList(@Param("albumSongListId") long albumSongListId);

    //获得第一次最新上传请求歌曲列表
    @Select("select * from d_music_song order by id desc limit #{pageSize}")
    List<MusicSong> getFirstRecent(@Param("pageSize") int pageSize);

    //根据歌曲songId得到歌曲id
    @Select("select id from d_music_song where song_id = #{songId}")
    long getIdBySongId(@Param("songId") long songId);

    //获得第一次以后最新上传请求歌曲列表
    @Select("select * from d_music_song where id < #{id} order by id desc limit #{pageSize}")
    List<MusicSong> getRecent(@Param("id") long id, @Param("pageSize") int pageSize);

}
