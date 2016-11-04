package im.vinci.server.naturelang.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by tim@vinci on 16/5/6.
 */
public class ChatSemantic implements Serializable{
    private String chatText;
    //一组对话的标识
    private String sessionId;

    public ChatSemantic() {
    }

    public ChatSemantic(String text) {
        this.chatText = text;
    }
    public ChatSemantic(String text, String sessionId) {
        this.chatText = text;
        this.sessionId = sessionId;
    }

    public String getChatText() {
        return chatText;
    }

    public ChatSemantic setChatText(String chatText) {
        this.chatText = chatText;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ChatSemantic setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("chatText", chatText)
                .add("sessionId", sessionId)
                .toString();
    }
}
