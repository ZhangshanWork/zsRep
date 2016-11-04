package im.vinci.server.other.services.fetch;

import im.vinci.server.other.domain.wrappers.requests.user.FirstMusicGet;

import java.util.List;

public interface UserFetchService {

    public String getFirstMusic(FirstMusicGet firstMusicGet) throws Exception;

    public List<String> ranMusicsInTopRankList(String artistName, int topN, int randomCount) throws Exception;
}
