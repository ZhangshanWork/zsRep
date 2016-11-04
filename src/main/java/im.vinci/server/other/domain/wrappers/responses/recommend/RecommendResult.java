package im.vinci.server.other.domain.wrappers.responses.recommend;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by henryhome on 5/10/16.
 */
public class RecommendResult {

    private List<String> musicList;
    private List<List<String>> tagList;

    public RecommendResult() {
        this.musicList = new ArrayList<>();
        this.tagList = new ArrayList<>();
    }

    public List<String> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<String> musicList) {
        this.musicList = musicList;
    }

    public List<List<String>> getTagList() {
        return tagList;
    }

    public void setTagList(List<List<String>> tagList) {
        this.tagList = tagList;
    }
}
