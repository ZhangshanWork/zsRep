package im.vinci.server.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户消息
 * Created by mayuchen on 16/8/10.
 */
public class UserMessage implements Serializable {

    @JsonIgnore
    private long id;

    private long messageUid;

    @JsonProperty("userFromUid")
    private long userFromId;

    @JsonProperty("userToUid")
    private long userToId;

    //message type, 是@ 还是 评论还是什么其他的
    private UserMessageType messageType;

    //message的规范内容,用json表示
    private JsonNode messageBody;

    //自定义内容
    private String content;

    private boolean isRead = false;

    private Date dtCreate;

    @JsonIgnore
    private Date dtUpdate;

    public long getId() {
        return id;
    }

    public UserMessage setId(long id) {
        this.id = id;
        return this;
    }

    public long getMessageUid() {
        return messageUid;
    }

    public UserMessage setMessageUid(long messageUid) {
        this.messageUid = messageUid;
        return this;
    }

    public long getUserFromId() {
        return userFromId;
    }

    public UserMessage setUserFromId(long userFromId) {
        this.userFromId = userFromId;
        return this;
    }

    public long getUserToId() {
        return userToId;
    }

    public UserMessage setUserToId(long userToId) {
        this.userToId = userToId;
        return this;
    }

    public JsonNode getMessageBody() {
        return messageBody;
    }

    public UserMessage setMessageBody(JsonNode messageBody) {
        this.messageBody = messageBody;
        return this;
    }

    public String getContent() {
        return content;
    }

    public UserMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public UserMessageType getMessageType() {
        return messageType;
    }

    public UserMessage setMessageType(UserMessageType messageType) {
        this.messageType = messageType;
        return this;
    }

    public boolean isRead() {
        return isRead;
    }

    public UserMessage setRead(boolean read) {
        isRead = read;
        return this;
    }

    public Date getDtCreate() {
        return dtCreate;
    }

    public UserMessage setDtCreate(Date dtCreate) {
        this.dtCreate = dtCreate;
        return this;
    }

    public Date getDtUpdate() {
        return dtUpdate;
    }

    public UserMessage setDtUpdate(Date dtUpdate) {
        this.dtUpdate = dtUpdate;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("messageUid", messageUid)
                .add("userFromId", userFromId)
                .add("userToId", userToId)
                .add("messageType", messageType)
                .add("messageBody", messageBody)
                .add("content", content)
                .add("isRead", isRead)
                .add("dtCreate", dtCreate)
                .add("dtUpdate", dtUpdate)
                .toString();
    }
}
