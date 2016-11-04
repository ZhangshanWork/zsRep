package im.vinci.server.naturelang.persistence;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NatureLangDao {
    @Select("select * from xiami_collect_songs  where list_id = #{listid}")
    public List<Map> getCollectionsById(Long listid);
}
