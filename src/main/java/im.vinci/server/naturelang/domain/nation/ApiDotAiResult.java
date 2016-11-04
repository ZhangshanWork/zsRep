package im.vinci.server.naturelang.domain.nation;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by tim@vinci on 16/5/24.
 * Api.ai的结果解析
 */
public class ApiDotAiResult implements Serializable{
    private String id;
    //Date and time of the request in UTC timezone using ISO-8601 format.
    private String date;
    //Contains the results of the natual language processing.
    private Result result;
    //Contains data on how the request succeeded or failed.
    private Status status;

    public String getId() {
        return id;
    }

    public ApiDotAiResult setId(String id) {
        this.id = id;
        return this;
    }

    public String getDate() {
        return date;
    }

    public ApiDotAiResult setDate(String date) {
        this.date = date;
        return this;
    }

    public Result getResult() {
        return result;
    }

    public ApiDotAiResult setResult(Result result) {
        this.result = result;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public ApiDotAiResult setStatus(Status status) {
        this.status = status;
        return this;
    }

    public static class Result {
        //Source of the answer. Could be "agent" if the response was retrieved from the agent. Or "domains"
        private String source;
        //	The query that was used to produce this result.
        private String resolvedQuery;
        //An action to take. Example: turn on
        private String action;
        //Parameters to be used by the action. Example: device : computer
        private Map<String,String> parameters;
        // Array of context objects with the fields "name", "parameters", "lifespan".
        private List<Context> contexts;
        //Data about fulfillment, speech, structured fulfillment data, etc.
        private FullFillment fulfillment;

        public String getSource() {
            return source;
        }

        public Result setSource(String source) {
            this.source = source;
            return this;
        }

        public String getResolvedQuery() {
            return resolvedQuery;
        }

        public Result setResolvedQuery(String resolvedQuery) {
            this.resolvedQuery = resolvedQuery;
            return this;
        }

        public String getAction() {
            return action;
        }

        public Result setAction(String action) {
            this.action = action;
            return this;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public Result setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        public List<Context> getContexts() {
            return contexts;
        }

        public Result setContexts(List<Context> contexts) {
            this.contexts = contexts;
            return this;
        }

        public FullFillment getFulfillment() {
            return fulfillment;
        }

        public Result setFulfillment(FullFillment fulfillment) {
            this.fulfillment = fulfillment;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("source", source)
                    .add("resolvedQuery", resolvedQuery)
                    .add("action", action)
                    .add("parameters", parameters)
                    .add("contexts", contexts)
                    .add("fulfillment", fulfillment)
                    .toString();
        }
    }


    public static class FullFillment {
        //Text to be pronounced to the user / shown on the screen
        private String speech;
        //Source of the fulfillment / data, e.g. "Wikipedia". Applies only when Domains are enabled for the agent
        private String source;

        public String getSpeech() {
            return speech;
        }

        public FullFillment setSpeech(String speech) {
            this.speech = speech;
            return this;
        }

        public String getSource() {
            return source;
        }

        public FullFillment setSource(String source) {
            this.source = source;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("speech", speech)
                    .add("source", source)
                    .toString();
        }
    }

    public static class Context {
        private String name;
        private int lifespan;
        private Map<String,String> parameters;

        public String getName() {
            return name;
        }

        public Context setName(String name) {
            this.name = name;
            return this;
        }

        public int getLifespan() {
            return lifespan;
        }

        public Context setLifespan(int lifespan) {
            this.lifespan = lifespan;
            return this;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public Context setParameters(Map<String, String> parameters) {
            this.parameters = parameters;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("name", name)
                    .add("lifespan", lifespan)
                    .add("parameters", parameters)
                    .toString();
        }
    }
    public static class Status {
        private int code;
        private String errorType;

        public int getCode() {
            return code;
        }

        public Status setCode(int code) {
            this.code = code;
            return this;
        }

        public String getErrorType() {
            return errorType;
        }

        public Status setErrorType(String errorType) {
            this.errorType = errorType;
            return this;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("code", code)
                    .add("errorType", errorType)
                    .toString();
        }
    }
}
