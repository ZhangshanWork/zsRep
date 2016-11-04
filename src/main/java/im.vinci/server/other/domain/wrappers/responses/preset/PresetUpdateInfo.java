package im.vinci.server.other.domain.wrappers.responses.preset;

import java.util.List;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public class PresetUpdateInfo {
    private boolean needUpdate;
    private Presets presets;

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        this.needUpdate = needUpdate;
    }

    public Presets getPresets() {
        return presets;
    }

    public void setPresets(Presets presets) {
        this.presets = presets;
    }

    public String toString() {
        String toReturn = "";
        toReturn += "{need_update:" + needUpdate;
        if (presets != null) {
            toReturn += ",presets:" + presets.toString();
        }
        toReturn += "}";
        return toReturn;
    }


    public class Category {
        public List<String> getMusics() {
            return musics;
        }

        public void setMusics(List<String> musics) {
            this.musics = musics;
        }

        public String getPlaylistName() {
            return playlistName;
        }

        public void setPlaylistName(String playlistName) {
            this.playlistName = playlistName;
        }

        public List<String> getTags() {
            return tags;
        }

        public void setTags(List<String> tags) {
            this.tags = tags;
        }

        private String playlistName;
        private List<String> tags;
        private List<String> musics;

        public String toString() {
            String toReturn = "";
            toReturn += "{playlist_name:" + playlistName;
            if (tags != null) {
                toReturn += ",tags:" + tags.toString();
            }
            if (musics != null) {
                toReturn += ",musics:" + musics.toString();
            }
            toReturn += "}";
            return toReturn;
        }
    }

    public class Presets {
        public List<Category> getCategories() {
            return categories;
        }

        public void setCategories(List<Category> categories) {
            this.categories = categories;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        private String version;
        private List<Category> categories;

        public String toString(){
            String toReturn = "";
            toReturn += "{version:" + version;
            if (categories != null) {
                toReturn += ",categories:"+categories.toString();
            }
            toReturn += "}";
            return toReturn;
        }
    }
}

