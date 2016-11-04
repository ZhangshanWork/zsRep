package im.vinci.server.other.persistence.modify;

import im.vinci.server.other.domain.preset.UserPresetMusic;
import im.vinci.server.other.persistence.modify.providers.UserPresetDeleteSqlProvider;
import im.vinci.server.other.persistence.modify.providers.UserPresetModifySqlProvider;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zhongzhengkai on 15/12/25.
 */
@Repository
public interface UserPresetModifyMapper {


    @InsertProvider(type = UserPresetModifySqlProvider.class, method = "saveUserPresetMusicBatch")
    boolean saveUserPresetMusicBatch(@Param("list") List<UserPresetMusic> list);

    @DeleteProvider(type = UserPresetDeleteSqlProvider.class, method = "deleteBatchByIds")
    boolean deleteBatchByIds(@Param("ids") List<Integer> ids);
}
