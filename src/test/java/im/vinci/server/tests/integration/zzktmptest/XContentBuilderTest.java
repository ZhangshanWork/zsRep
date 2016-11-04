package im.vinci.server.tests.integration.zzktmptest;

import com.fasterxml.jackson.databind.JsonNode;
import im.vinci.server.device.domain.DeviceUserLog;
import im.vinci.server.utils.JsonUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by zhongzhengkai on 15/12/15.
 */
public class XContentBuilderTest {


    public static void main(String[] args){
        String msgBody="{\"name\":\"next\",\"createtime\":1450070939680,\"info\":{\"mac\":\"38:fd:fe:60:03:49\",\"imei\":\"864765020140169\",\"rom_version\":\"V1.2.0\",\"sn\":\"102151204B0270                                              10\"},\"data\":{\"duration\":224902,\"from\":\"search\",\"is_user_download\":true,\"mid\":\"87614_xiami\",\"mlength\":225}}";
        final DeviceUserLog log = JsonUtils.decode(msgBody, DeviceUserLog.class);
        try {
            String logName = log.getName();
            XContentBuilder builder = _transformLog2(log);
            String newBodyStr = builder.string();


            System.out.println(newBodyStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static XContentBuilder _transformLog2(DeviceUserLog log) throws IOException {
        DeviceUserLog.DeviceInfo info = log.getInfo();
        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        Iterator iterator = log.getData().fields();
        builder.field("name", log.getName());
        builder.field("create_time", log.getCreatetime());
        builder.field("sn", info.getSn());
        builder.field("imei", info.getImei());
        builder.field("mac", info.getMac());
        builder.field("rom_version", info.getRom_version());

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            JsonNode node = (JsonNode) entry.getValue();
            String key = (String) entry.getKey();
            key = _reviseKeyName2(key);
            if (node.isNumber()) {
                builder.field(key, node.asLong());
            } else if (node.isTextual()) {
                String text = node.asText();
                //前端发的search型userlog的
                if (log.getName() == "search" && key.equals("search_result")) {
                    String[] arr = text.split(",");
                    List list = Arrays.asList(arr);
                    builder.field(key, list);
                } else {
                    builder.field(key, node.asText());
                }
            } else if (node.isBoolean()) {
                builder.field(key, node.asBoolean());
            } else if (!node.isArray()) {
                builder.field(key, node.toString());
            } else {
                ArrayList list = new ArrayList();
                int size = node.size();
                for (int i = 0; i < size; ++i) {
                    JsonNode subNode = node.get(i);
                    if (subNode.isTextual()) {
                        list.add(subNode.asText());
                    } else {
                        list.add(Long.valueOf(subNode.asLong()));
                    }
                }
                builder.field(key, list);
            }
        }
        builder.endObject();
        return builder;
    }

    private static String _reviseKeyName2(String fieldName) {
        Map<String,String> fieldNameMap = new HashMap(){
            {
                put("createtime","create_time");
                put("searchresult","search_result");
                put("searchtype","search_type");
            }
        };
        if (fieldNameMap.containsKey(fieldName)) {
            return fieldNameMap.get(fieldName);
        } else {
            return fieldName;
        }
    }

}


