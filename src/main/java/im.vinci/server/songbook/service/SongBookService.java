package im.vinci.server.songbook.service;

import com.taobao.api.ApiException;
import im.vinci.server.songbook.model.SongBook;

import java.util.List;

/**
 * Created by mlc on 2016/6/28.
 */
public interface SongBookService {
    public List<SongBook> getSongBook(SongBook songBook);

    public void doSave(List<SongBook> list);

    public void doDel(SongBook songBook);

    public List  doRefreshSongRepos(String deviceId, int size) throws ApiException;

    public List doRefreshToday(String deviceId, int size) throws ApiException;

    public List doLoadSongRepos(int size);
}
