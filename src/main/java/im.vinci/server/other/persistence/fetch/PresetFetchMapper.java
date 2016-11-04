package im.vinci.server.other.persistence.fetch;

import im.vinci.server.other.domain.preset.Playlistname;
import im.vinci.server.other.domain.preset.PresetMusic;
import im.vinci.server.other.persistence.fetch.providers.PresetFetchSqlProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
@Repository
public interface PresetFetchMapper {

    @Select("select * from preset_music limit 1")
    @Options(useCache =false, flushCache = Options.FlushCachePolicy.TRUE, timeout =10000)
    PresetMusic getOnePersetMusicData();

    @Select("select * from preset_music where version=#{version}")
    List<PresetMusic> listPresetMusicByVersion(Integer version);

    @SelectProvider(type = PresetFetchSqlProvider.class, method = "listTagNameByPlaylistNameIds")
    List<Playlistname> listTagNameByPlaylistNameIds(@Param("playlistNameIds") String[] playlistNameIds);

}
