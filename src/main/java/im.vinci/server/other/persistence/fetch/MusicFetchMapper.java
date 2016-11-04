package im.vinci.server.other.persistence.fetch;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by henryhome on 10/17/15.
 */
@Repository
public interface MusicFetchMapper {

    @Select("select song_id from music m, music_tag mt, tag t where m.id = mt.music_id and mt.tag_id = t.id " +
            "and t.name = #{currentMood}")
    public List<Integer> getMusicIdListByCurrentMood(String currentMood);

    @Select("select song_id from music m, music_scene ms, scene s where m.id = ms.music_id and ms.scene_id = s.id " +
        "and s.name = '轻松休闲' or s.name = '新歌' or s.name = '热门' or s.name= '流行'")
    public List<Integer> getDefaultMusicIdList();
}
