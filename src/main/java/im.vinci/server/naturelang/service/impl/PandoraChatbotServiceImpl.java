package im.vinci.server.naturelang.service.impl;

import im.vinci.server.naturelang.domain.ChatSemantic;
import im.vinci.server.naturelang.service.ChatbotService;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tim@vinci on 16/5/6.
 */
//@Service("PandoraChatbotServiceImpl")
public class PandoraChatbotServiceImpl implements ChatbotService{

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static String userKey = "c95ff9a6e0931d60bf6aa5e787dee638";

    /** The Application ID assigned by Pandorabots. */
    public static String appId = "1409612611691";

    /** server name of pandorabots API */
    public static String host = "aiaas.pandorabots.com";

    @Override
    public ChatSemantic chat(String text, String clientId, String sessionId) throws IOException, JSONException, URISyntaxException {
        return talk("testbot",sessionId, text);
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new PandoraChatbotServiceImpl().chat("how are you",null,null));
    }


    /**
     * Simplest method to talk to a bot.
     *
     * @param botName
     *            name of bot
     * @param input
     *            text for conversation
     * @return text of bot's response
     * @see #talk(String, String, String)
     * @since 0.0.1
     */
    public ChatSemantic talk(String botName, String input)
            throws ClientProtocolException, IOException, JSONException,
            URISyntaxException {
        return talk(botName, null, input);
    }


    /**
     * Simplest method to talk to a bot.
     *
     * @param botName
     *            name of bot
     * @param input
     *            text for conversation
     * @return text of bot's response
     * @see #talk(String, String, String)
     * @since 0.0.1
     */
    public ChatSemantic talk(String botName, String sessionId, String input)
            throws ClientProtocolException, IOException, JSONException,
            URISyntaxException {
        return talk(botName, null, sessionId, input);
    }

    /**
     * Talk to a bot as a specific client.
     *
     * @param botName
     *            name of bot
     * @param clientName
     *            name of client
     * @param input
     *            text for conversation
     * @return text of bot's response
     * @see #debugBot(String, String, String , String, boolean, boolean, boolean,
     *      boolean, boolean)
     * @since 0.0.1
     */
    public ChatSemantic talk(String botName, String clientName, String sessionId , String input)
            throws ClientProtocolException, IOException, JSONException,
            URISyntaxException {
        return debugBot(botName, clientName, sessionId, input, false, false, false, false,
                false);
    }

    /**
     * Most general version of talk method that returns detailed debugging
     * information.
     *
     * @param botName
     *            name of bot
     * @param clientName
     *            name of client (optional)
     * @param input
     *            text for conversation
     * @param extra
     *            adds extra information into response
     * @param reset
     *            reset status of bot
     * @param trace
     *            adds trace data into response
     * @param reload
     *            force system to realod bot
     * @param recent
     *            use recent pod even if it is older than files
     * @return text of bot's response
     * @throws IOException
     * @throws JSONException
     * @throws ClientProtocolException
     * @throws URISyntaxException
     * @since 0.0.1
     */
    public ChatSemantic debugBot(String botName, String clientName, String sessionId, String input,
                           boolean extra, boolean reset, boolean trace, boolean reload,
                           boolean recent) throws ClientProtocolException, IOException,
            JSONException, URISyntaxException {
        URI uri = talkUri(botName);
        Log("Talk botName=" + botName + " input=\"" + input + "\"" + " uri="
                + uri);
        List<NameValuePair> params = baseParams();
        params.add(new BasicNameValuePair("input", input));
        if (clientName != null)
            params.add(new BasicNameValuePair("client_name", clientName));
        if (sessionId != null)
            params.add(new BasicNameValuePair("sessionid", sessionId));
        if (extra)
            params.add(new BasicNameValuePair("extra", "true"));
        if (reset)
            params.add(new BasicNameValuePair("reset", "true"));
        if (trace)
            params.add(new BasicNameValuePair("trace", "true"));
        if (reload)
            params.add(new BasicNameValuePair("reload", "true"));
        if (recent)
            params.add(new BasicNameValuePair("recent", "true"));
        Log("Talk params=" + URLEncodedUtils.format(params, "UTF-8"));
        String response = Request.Post(uri).bodyForm(params).execute()
                .returnContent().asString();
        JSONObject jObj = new JSONObject(response);
        ChatSemantic chat = new ChatSemantic();
        sessionId = jObj.getString("sessionid");
        if (StringUtils.hasText(sessionId)) {
            chat.setSessionId(sessionId);
        }
        JSONArray jArray = jObj.getJSONArray("responses");
        chat.setChatText(jArray.getString(0).trim());
        return chat;
    }

    /**
     * helper for composing URI.
     *
     * @param path
     *            string to be added
     * @return string of partial URI path
     * @since 0.0.9
     */
    private String sep(String path) {
        return path == null ? "" : "/" + path;
    }

    /**
     * composing path part of URI.
     *
     * @param mode
     *            bot or talk
     * @param botName
     *            name of bot
     * @param kind
     *            file kind
     * @param fileName
     *            file name
     * @return string of path part of URI
     * @throws URISyntaxException
     * @since 0.0.9
     */
    private String composeUri(String mode, String botName, String kind,
                              String fileName) throws URISyntaxException {
        String uri = "https://" + host;
        uri += sep(mode);
        uri += sep(appId);
        uri += sep(botName);
        uri += sep(kind);
        uri += sep(fileName);
        return uri;
    }

    /**
     * base parameter for HTTP request.
     *
     * @return list of name-value pair represents URI parameters
     * @since 0.0.9
     */
    private List<NameValuePair> baseParams() {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user_key", userKey));
        return params;
    }

    /**
     * composing parameter part of URI.
     *
     * @return string of parameter part of URI
     * @since 0.0.9
     */
    private String composeParams(List<NameValuePair> params) {
        List<NameValuePair> baseParams = baseParams();
        if (params != null)
            baseParams.addAll(params);
        return "?" + URLEncodedUtils.format(baseParams, "UTF-8");
    }

    /**
     * composing URI for talking to bot.
     *
     * @param botName
     * @return URI for request
     * @throws URISyntaxException
     * @since 0.0.9
     */
    private URI talkUri(String botName) throws URISyntaxException {
        return new URI(composeUri("talk", botName, null, null));
    }

    /**
     * Read response from Pandorabots server.
     *
     * @param httpResp
     *            HTTP response
     * @return HTTP response
     * @since 0.0.1
     */
    public String readResponse(HttpResponse httpResp) {
        String response = "";
        try {
            int code = httpResp.getStatusLine().getStatusCode();
            Log("Response code=" + code);
            InputStream is = httpResp.getEntity().getContent();
            BufferedReader inb = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder("");
            String line;
            String NL = System.getProperty("line.separator");
            while ((line = inb.readLine()) != null) {
                sb.append(line).append(NL);
                Log("Read " + line);
            }
            inb.close();
            response = sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    private void Log(String message) {
        if (logger.isDebugEnabled())
            logger.debug("PandorabotsAPI:{} " ,message);
    }
}
