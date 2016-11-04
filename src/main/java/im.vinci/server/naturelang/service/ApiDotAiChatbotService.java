package im.vinci.server.naturelang.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.naturelang.domain.nation.ApiDotAiResult;
import im.vinci.server.utils.BizTemplate;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.WebUtils;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * Created by tim@vinci on 16/5/24.
 */
@Service
public class ApiDotAiChatbotService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public ApiDotAiResult nlu(final String text, final String sessionId) {
        return new BizTemplate<ApiDotAiResult>("ApiDotAiChatbotService.NLU") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected boolean onError(Throwable e) {
                return false;
            }

            @Override
            protected ApiDotAiResult process() throws Exception {
                if (StringUtils.isEmpty(text)) {
                    return null;
                }
                ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                        .put("query", text).put("lang", "en").put("v", "20150910");
                if (StringUtils.hasText(sessionId)) {
                    builder.put("sessionId", sessionId);
                }
                Response response = Request.Get("https://api.api.ai/v1/query?" + WebUtils.buildQuery(builder.build(), WebUtils.CHARSET_UTF8))
                        .socketTimeout(3000).connectTimeout(3000)
                        .addHeader("Authorization", "Bearer 2324f0aa7fe14c849d70ee6c2de06d38")
                        .addHeader("Content-Type", "application/json; charset=utf-8")
                        .execute();
                JsonNode json = JsonUtils.decode(response.returnContent().asString(), JsonNode.class);
                if (json == null) {
                    logger.warn("ApiDotAi occurred error:{}", response.returnResponse().getStatusLine());
                    return null;
                }
                if (logger.isDebugEnabled()) {
                    logger.debug(json.toString());
                }
                JsonNode status = json.findPath("status").findPath("code");
                if (!status.isIntegralNumber() || status.asInt() != 200) {
                    logger.warn("ApiDotAi occurred error:{}", json.findPath("status").findPath("errorType").toString());
                    return null;
                }

                return JsonUtils.getObjectMapperInstance().readerFor(ApiDotAiResult.class).readValue(json);
            }
        }.execute();

    }

    public static void main(String[] args) throws IOException {
        System.out.println(new ApiDotAiChatbotService().nlu("i wanna listen to songs of backstreet boy", null));
    }
}
