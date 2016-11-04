package im.vinci.server.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import im.vinci.server.utils.json.DisablingMaskFieldIntrospector;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Json工具类
 * Created by tim@vinci on 15-1-27.
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance(DATE_PATTERN);
    private static ObjectMapper objectMapper;

    private static ObjectMapper objectMapperWithMask;

    private static ObjectMapper httpObjectMapper = new ObjectMapper();

    private static ObjectMapper newObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        SimpleModule module = new SimpleModule("DateTimeModule", Version.unknownVersion());
        module.addSerializer(Date.class, new DateJsonSerializer());
        module.addDeserializer(Date.class, new DateJsonDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    static {
        objectMapper = newObjectMapper();
        objectMapper.setAnnotationIntrospector(new DisablingMaskFieldIntrospector());

        objectMapperWithMask = newObjectMapper();

        // Ask the json converter to ignore properties with null values when convert
        httpObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        httpObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // Ask the json converter to ignore extra properties here
        httpObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        httpObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    }

    public static String encodeWithMask(Object obj) {
        try {
            return objectMapperWithMask.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("encode(Object)", e); //$NON-NLS-1$
        }
        return null;
    }

    public static String encode(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("encode(Object)", e); //$NON-NLS-1$
        }
        return null;
    }
    public static byte[] encode2bytes(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (IOException e) {
            logger.error("encode(Object)", e); //$NON-NLS-1$
        }
        return null;
    }
    public static JsonNode decode(String json) {
        try {
            return objectMapper.readValue(json, JsonNode.class);

        } catch (IOException e) {
            logger.error("decode(String, Class<T>)", e); //$NON-NLS-1$
        }
        return null;
    }
    public static <T> T decode(String json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            logger.error("decode(String, Class<T>)", e); //$NON-NLS-1$
        }
        return null;
    }
    public static <T> T decode(byte[] json, Class<T> valueType) {
        try {
            return objectMapper.readValue(json, valueType);
        } catch (IOException e) {
            logger.error("decode(String, Class<T>)", e); //$NON-NLS-1$
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public static <T> T decode(String json, TypeReference<T> reference) {
        try {
            return (T) objectMapper.readValue(json, reference);
        } catch (IOException e) {
            logger.error("decode(String, Class<T>)", e);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    public static <T> T decode(byte[] json, TypeReference<T> reference) {
        try {
            return (T) objectMapper.readValue(json, reference);
        } catch (IOException e) {
            logger.error("decode(String, Class<T>)", e);
        }
        return null;
    }
    public static ObjectMapper getObjectMapperInstance() {
        return objectMapper;
    }

    public static ObjectMapper getHttpObjectMapperInstance() {
        return httpObjectMapper;
    }

    public static String encodeWithHttpModel(Object obj, boolean isPretty) {
        try {
            if (isPretty) {
                return httpObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            }
            return httpObjectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            logger.error("encode(Object)", e); //$NON-NLS-1$
        }
        return null;
    }

    private static class DateJsonDeserializer extends JsonDeserializer<Date> {
        @Override
        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            Object value = jsonParser.getCurrentValue();
            if (value instanceof Number) {
                return new Date(jsonParser.getLongValue());
            } else if (value instanceof String){
                String date = jsonParser.getText();
                if (date != null && !date.isEmpty()) {
                    try {
                        return DATE_FORMAT.parse(date);
                    } catch (ParseException e) {
                        throw new JsonParseException(jsonParser,"cannot parse date string:", e);
                    }
                }
            }
            return null;
        }
    }

    private static class DateJsonSerializer extends JsonSerializer<Date> {
        @Override
        public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                throws IOException {
            if (date != null) {
                jsonGenerator.writeString(DATE_FORMAT.format(date));
            }
        }
    }


    public static List<HttpMessageConverter<?>> generateMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        // Ask the json converter to ignore properties with null values when convert
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Ask the json converter to ignore extra properties here
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        converter.setObjectMapper(objectMapper);
        converters.add(converter);

        return converters;
    }
}
