package im.vinci.server.other.domain.preset;

/**
 * Created by zhongzhengkai on 15/12/24.
 */
public class SceneTag {
    private int id;
    private String createTime;
    private String tagName;
    private int sceneId;

    public SceneTag(){};

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSceneId() {
        return sceneId;
    }

    public void setSceneId(int sceneId) {
        this.sceneId = sceneId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
