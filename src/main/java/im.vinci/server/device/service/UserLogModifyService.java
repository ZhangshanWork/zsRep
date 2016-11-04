//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package im.vinci.server.device.service;

import com.aliyun.openservices.ons.api.*;
import com.fasterxml.jackson.databind.JsonNode;
import im.vinci.server.device.domain.DeviceUserLog;
import im.vinci.server.device.domain.DeviceUserLogForDB;
import im.vinci.server.device.persistence.UserLogModifyMapper;
import im.vinci.server.elasticsearch.domain.base.BasicDocModel;
import im.vinci.server.elasticsearch.impl.AccountExtraInfoAccessor;
import im.vinci.server.utils.JsonUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.Map.Entry;

@Service
public class UserLogModifyService  {
    private static Logger logger = LoggerFactory.getLogger(UserLogModifyService.class);
    private static Logger onsConsumeLogger = LoggerFactory.getLogger("onsConsume");
    private static Logger onsProduceLogger = LoggerFactory.getLogger("onsProduce");
    @Autowired
    Environment env;

    @Autowired(required = false)
    @Qualifier("onsDeviceUserLogProducer")
    private Producer producer;

    @Resource(name = "esClient")
    private Client esClient;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private UserLogModifyMapper userLogMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AccountExtraInfoAccessor accountExtraInfoAccessor;

//    @Autowired
    //这里不用spring框架注入的,自己new一个就好,要不然会出现一个post到推荐系统flask框架而request.form.get()无法取到数据的bug
    private RestTemplate restTemplate = new RestTemplate();

    private HashSet<String >sensitiveLogName = new HashSet<String>(){{
        add("pre");
        add("next");
        add("musicplay");
        add("doubleclick");
        add("share");
    }};

    public UserLogModifyService() {
    }

    public void sendLogToONS(DeviceUserLog userLog) {
        this.producer.send(this._createMsg(userLog));
    }

    //方便校正前端传过来的字段名用
    private Map<String,String> fieldNameMap = new HashMap<String,String>(){
        {
            put("createtime","create_time");
            put("searchresult","search_result");
            put("searchtype","search_type");
            put("currentversion","current_version");
            put("oldversion","old_version");
        }
    };

    @Bean(destroyMethod = "shutdown")
    @Profile({"qaci","prod"})
    public Consumer receiveLogFromONS() throws UnknownHostException {
        logger.info("--------------------- receiveLogFromONS");
        Properties properties = new Properties();
        properties.put("ConsumerId", this.env.getProperty("ons.deviceUserLog.consumeId"));
        properties.put("AccessKey", this.env.getProperty("ons.accessKey"));
        properties.put("SecretKey", this.env.getProperty("ons.secretKey"));

        //jdk 默认线程数是10,我们这里取和cpu核数相等的值
        properties.put(PropertyKeyConst.ConsumeThreadNums, Runtime.getRuntime().availableProcessors());

        Consumer consumer = ONSFactory.createConsumer(properties);
        String topic = this.env.getProperty("ons.deviceUserLog.topic");
        this._runConsumerByTopic(consumer, topic);
        logger.info("------>>>ONS Consumer Start to subscribe topic:" + topic);
        return consumer;
    }

    private void _runConsumerByTopic(Consumer consumer, String topic) {
        consumer.subscribe(topic, "*", new MessageListener() {
            public Action consume(final Message message, ConsumeContext context) {
                try {
                    byte[] msgBody = message.getBody();
                    final String bodyStr = new String(msgBody, "UTF-8");
                    onsConsumeLogger.info("msgID:" + message.getMsgID() + " received,body is:" + bodyStr);
                    final DeviceUserLog log = JsonUtils.decode(msgBody, DeviceUserLog.class);
                    if(log != null) {
                        String logName = log.getName();
                        if(logName != null) {
                            XContentBuilder builder = _transformLog(log);

                            IndexResponse response = esClient.prepareIndex("user_logs", logName, log.getEventid()).setSource(builder).execute().actionGet();
                            if (response.isCreated()) {
                                DeviceUserLog.DeviceInfo deviceInfo = log.getInfo();
                                //是upgrade型的log就去改写es里account_extra_info文档,方便统计版本分布数据
                                if (logName.equals("upgrade")) {
                                    accountExtraInfoAccessor.upsert(deviceInfo.getImei(), new BasicDocModel() {
                                        @Override
                                        public void initDocMap(Map<String, Object> map) {
                                            map.put("imei", deviceInfo.getImei());
                                            map.put("mac", deviceInfo.getMac());
                                            map.put("sn", deviceInfo.getSn());
                                            map.put("rom_version", deviceInfo.getRom_version());
                                            map.put("update_time", System.currentTimeMillis());
                                        }
                                    });
                                }


                                String newBodyStr = builder.string();
                                Long IdOfLogForDB = (Long) transactionTemplate.execute(new TransactionCallback() {
                                    public Long doInTransaction(TransactionStatus status) {
                                        DeviceUserLogForDB logForDB = new DeviceUserLogForDB(log, message.getKey(), newBodyStr);
                                        UserLogModifyService.this.userLogMapper.addUserLog(logForDB);
                                        return logForDB.getId();
                                    }
                                });
                                onsConsumeLogger.info("msgID:" + message.getMsgID() + ",eventID:" + log.getEventid() + " inserted successfully,db_id is:" + IdOfLogForDB);
                            } else {
                                onsConsumeLogger.info("msgID:" + message.getMsgID() + ",eventID:" + log.getEventid()+ " already exists!");
                            }
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.warn(e.getMessage());
                } catch (IOException e) {
                    logger.warn(e.getMessage());
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
                return Action.CommitMessage;
            }
        });
        consumer.start();
    }

    private XContentBuilder _transformLog(DeviceUserLog log) throws IOException {
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
        builder.field("agent", log.getAgent());

        while (iterator.hasNext()) {
            Entry entry = (Entry) iterator.next();
            JsonNode node = (JsonNode) entry.getValue();
            String key = (String) entry.getKey();
            key = _reviseKeyName(key);
            if (node.isNumber()) {
                builder.field(key, node.asLong());
            } else if (node.isTextual()) {
                String text = node.asText();
                //前端发的search型userlog的
                if (log.getName().equals("search") && key.equals("search_result")) {
                    if (text.length() == 0) {
                        builder.field(key, new ArrayList());
                    } else {
                        String[] arr = text.split(",");
                        List list = Arrays.asList(arr);
                        builder.field(key, list);
//                        builder.array(key, list);//对value是list对象的key,追加list里的值,如果直接builder.field(key, list),会覆盖掉原list对象
                    }
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

    private Message _createMsg(DeviceUserLog userLog) {
        String msgString = JsonUtils.encode(userLog);
        onsProduceLogger.info(msgString);
        Message msg = this.context.getBean("onsDeviceUserLogMessage", Message.class);
        if (!StringUtils.isEmpty(userLog.getEventid())) {
            msg.setKey(userLog.getEventid());
        }
        msg.setTag(userLog.getName());
        try {
            msg.setBody(msgString.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            logger.warn(e.getMessage());
        }
        return msg;
    }

    private String _reviseKeyName(String fieldName) {
        if (fieldNameMap.containsKey(fieldName)) {
            return fieldNameMap.get(fieldName);
        } else {
            return fieldName;
        }
    }

    public void printSth(){
        System.out.println(" i am vinci headable");
    }

}
