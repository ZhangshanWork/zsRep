package im.vinci.server.syncdb.controller;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
import com.google.common.collect.ImmutableMap;
import im.vinci.server.common.exceptions.ServerErrorException;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.push.PushService;
import im.vinci.server.security.ApiSecurityLabel;
import im.vinci.server.syncdb.domain.ClientUserData;
import im.vinci.server.syncdb.domain.wrapper.DownloadUserDataResponse;
import im.vinci.server.syncdb.domain.wrapper.UploadUserDataRequest;
import im.vinci.server.syncdb.service.UserDataSyncService;
import im.vinci.server.syncdb.service.UserFavoriteMusicOperateService;
import im.vinci.server.utils.JsonUtils;
import im.vinci.server.utils.UserContext;
import im.vinci.server.utils.apiresp.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by tim@vinci on 16/7/27.
 */
@RestController
@RequestMapping(value = "/vinci/user/syncdb", produces = "application/json;charset=UTF-8")
public class UserSyncDBController {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Environment env;

    @Autowired
    private UserDataSyncService userDataSyncService;

    @Autowired
    private UserFavoriteMusicOperateService userFavoriteMusicOperateService;

    @Autowired
    private PushService pushService;

    private TransactionProducer producer;

    private Consumer consumer;

    private Consumer favoriteMusicConsumer;

    @Value("${ons.user_client_data_sync.topic}")
    private String userSyncDataOnsTopic;

    @Value("${ons.user_client_data_sync.sync_push_msg.consumeId}")
    private String userSyncDataPushMsgConsumerId;

    @PostConstruct
    public void init() {
        Properties properties = new Properties();

        properties.put(PropertyKeyConst.ProducerId, env.getRequiredProperty("ons.produceId"));
        properties.put(PropertyKeyConst.AccessKey, env.getRequiredProperty("ons.accessKey"));
        properties.put(PropertyKeyConst.SecretKey, env.getRequiredProperty("ons.secretKey"));
        producer = ONSFactory.createTransactionProducer(properties,
                /**
                 * 直接检查数据中有没有当前update_version的数据,有就算成功,没有就不成功.
                 * 另外,如果是实际成功了,但是马上又被修改了导致update version没了,其实也没有影响,因为新的update version会发出来并按照新数据进行处理
                 */
                msg -> {
                    try {
                        long uid = Long.parseLong(msg.getUserProperties("uid"));
                        long cv = Long.parseLong(msg.getUserProperties("update_version"));
                        String table = msg.getUserProperties("table_name");
                        if (userDataSyncService.checkUploadUserData(uid, table, cv)) {
                            return TransactionStatus.CommitTransaction;
                        }
                        return TransactionStatus.RollbackTransaction;
                    }catch (NumberFormatException e) {
                        return TransactionStatus.RollbackTransaction;
                    }catch (VinciException e) {
                        return TransactionStatus.Unknow;
                    }catch (Exception e) {
                        return TransactionStatus.RollbackTransaction;
                    }
                });
        producer.start();
        properties.remove(PropertyKeyConst.ProducerId);
        properties.put(PropertyKeyConst.ConsumerId, userSyncDataPushMsgConsumerId);
        consumer = ONSFactory.createConsumer(properties);
        _runUserDataSyncReceiveConsumer(consumer);

        favoriteMusicConsumer = ONSFactory.createConsumer(properties);
        runUserFavoriteMusicConsumer(favoriteMusicConsumer);
    }

    @PreDestroy
    public void destroy() {
        if (producer != null && producer.isStarted()) {
            producer.shutdown();
        }
        producer = null;
        if (consumer != null && consumer.isStarted()) {
            consumer.shutdown();
        }
        consumer = null;

        if (favoriteMusicConsumer != null && favoriteMusicConsumer.isStarted()) {
            favoriteMusicConsumer.shutdown();
        }
        favoriteMusicConsumer = null;
    }

    /**
     * 收取所有数据库更新消息,收到后发送给前端设备push message
     */
    private void _runUserDataSyncReceiveConsumer(Consumer consumer) {
        consumer.subscribe(userSyncDataOnsTopic, "*", (msg, context) -> {
            logger.info("receive user data sync msg {}, to push msg to device client:{}",msg.getMsgID(), msg.getKey());
            try {
                long uid = Long.parseLong(msg.getUserProperties("uid"));
                long cv = Long.parseLong(msg.getUserProperties("update_version"));
                String table = msg.getUserProperties("table_name");
                boolean result = pushService.pushMessageByUidAsync(
                        uid,
                        ImmutableMap.of("action", "userdata.sync"),
                        JsonUtils.encode(ImmutableMap.of(
                                "uid", uid,
                                "table", table,
                                "max_update_version", cv
                        ))
                );

                if (result) {
                    return Action.CommitMessage;
                }
                return Action.ReconsumeLater;
            }catch (NumberFormatException e) {
                return Action.CommitMessage;
            }catch (Exception e) {
                logger.error("occurred unexpected error:",e);
                return Action.ReconsumeLater;
            }
        });
        consumer.start();

    }

    private void runUserFavoriteMusicConsumer(Consumer consumer) {
        consumer.subscribe(userSyncDataOnsTopic, "*", (msg, context) -> {
            logger.info("receive user data sync msg {}, to update favorite musics:{}",msg.getMsgID(), msg.getKey());
            try {
                long userid = Long.parseLong(msg.getUserProperties("uid"));
                long version = Long.parseLong(msg.getUserProperties("update_version"));
                String tableName = msg.getUserProperties("table_name");

                List<ClientUserData> datas = userDataSyncService.getUserData(userid, tableName, version);

                if(datas == null || datas.size() <= 0)
                {
                    return Action.CommitMessage;
                }

                logger.info("update favorite music data userid: {}, tableName: {}, version: {}", userid, tableName, version);

                boolean result = userFavoriteMusicOperateService.updateData(datas);

                logger.info("update favorite music data OK");

                if (result) {
                    return Action.CommitMessage;
                }
                return Action.ReconsumeLater;
            }catch (NumberFormatException e) {
                return Action.CommitMessage;
            }catch (Exception e) {
                logger.error("occurred unexpected error:",e);
                return Action.ReconsumeLater;
            }
        });
        consumer.start();

    }

    @RequestMapping(value = "/download",method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<DownloadUserDataResponse> downloadUserData(@RequestParam("table") String table, @RequestParam("max_update_version") long version) {
        return new ResultObject<>(userDataSyncService.downloadUserData(UserContext.getUserInfo().getId(), table, version));
    }

    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    @ApiSecurityLabel(isCheckLogin = true)
    public ResultObject<Map<String,Long>> uploadUserData(final @RequestBody UploadUserDataRequest request) {
        long cv = userDataSyncService.plusUserDataUpdateVersion(UserContext.getUserInfo().getId(),request.getTable());
        Message msg = new Message(userSyncDataOnsTopic, request.getTable(),
                UserContext.getUserInfo().getId()+"_"+cv+"_"+request.getTable(),
                request.getTable().getBytes()
        );
        msg.putUserProperties("uid",Long.toString(UserContext.getUserInfo().getId()));
        msg.putUserProperties("update_version",Long.toString(cv));
        msg.putUserProperties("table_name",request.getTable());
        final AtomicReference<VinciException> exceptionReference = new AtomicReference<>();
        //ons 发送事务消息 https://help.aliyun.com/document_detail/29548.html?spm=5176.doc29537.6.133.wZRKyD
        try {
            SendResult sendResult = producer.send(msg, new LocalTransactionExecuter() {
                @Override
                public TransactionStatus execute(Message msg, Object arg) {
                    // 消息ID(有可能消息体一样，但消息ID不一样, 当前消息ID在控制台无法查询)
//                String msgId = msg.getMsgID();
                    // 消息体内容进行crc32, 也可以使用其它的如MD5
//                long crc32Id = HashUtil.crc32Code(msg.getBody());
                    // 消息ID和crc32id主要是用来防止消息重复
                    // 如果业务本身是幂等的, 可以忽略, 否则需要利用msgId或crc32Id来做幂等
                    // 如果要求消息绝对不重复, 推荐做法是对消息体body使用crc32或md5来防止重复消息
                    TransactionStatus transactionStatus = TransactionStatus.Unknow;
                    try {
                        boolean isCommit =
                                userDataSyncService.uploadUserData(UserContext.getUserInfo().getId(), request.getTable(), cv, request.getRecords());
                        if (isCommit) {
                            // 本地事务成功、提交消息
                            transactionStatus = TransactionStatus.CommitTransaction;
                        } else {
                            // 本地事务失败、回滚消息
                            transactionStatus = TransactionStatus.RollbackTransaction;
                        }
                    } catch (VinciException e) {
                        exceptionReference.set(e);
                        transactionStatus = TransactionStatus.RollbackTransaction;
                    }
                    logger.info("upload user data Message :{},{},{} transactionStatus:{}", UserContext.getUserInfo().getId(), request.getTable(), cv, transactionStatus.name());
                    return transactionStatus;
                }
            }, null);
        }catch (Exception e) {
            VinciException exception = exceptionReference.get();
            if (exception != null) {
                throw exception;
            } else {
                logger.error("occur unexpected exception when send ons msg:",e);
                throw new ServerErrorException();
            }
        }
        return new ResultObject<>(ImmutableMap.of("update_version",cv));
    }

}
