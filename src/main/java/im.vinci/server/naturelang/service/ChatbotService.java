package im.vinci.server.naturelang.service;

import im.vinci.server.naturelang.domain.ChatSemantic;
import org.json.JSONException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * 机器人AI问答
 * Created by tim@vinci on 16/5/6.
 */
public interface ChatbotService {
    ChatSemantic chat(String text, String clientId, String sessionId) throws IOException, JSONException, URISyntaxException;
}
