package im.vinci.server.elasticsearch.index;

import java.util.List;

/**
 * Created by zhongzhengkai on 16/1/14.
 */
public interface XiamiMusic {

    String getMaxPlayCountMusicId();

    String getMaxPlayCountMusicIdByArtist(String artistName);

    List<String> getTopNMaxPlayCountMusicIds(int topN);

    List<String> getTopNMaxPlayCountMusicIdsByArtist(String artistName, int topN);

}
