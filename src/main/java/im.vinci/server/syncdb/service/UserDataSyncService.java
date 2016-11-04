package im.vinci.server.syncdb.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import im.vinci.server.common.exceptions.VinciException;
import im.vinci.server.common.exceptions.error.ErrorCode;
import im.vinci.server.syncdb.domain.ClientUserData;
import im.vinci.server.syncdb.domain.wrapper.DownloadUserDataResponse;
import im.vinci.server.syncdb.persistence.UserDataSyncMapper;
import im.vinci.server.utils.BizTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户数据同步service
 * Created by tim@vinci on 16/7/26.
 */
@Service
public class UserDataSyncService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private final static int MAX_LINE_OF_USER_DOWNLOAD_DATA = 100;

    @Autowired
    private UserDataSyncMapper userDataSyncMapper;

    public DownloadUserDataResponse downloadUserData(final long realUserId, final String tableName, final long version) {
        return new BizTemplate<DownloadUserDataResponse>(getClass().getSimpleName() + ".downloadUserData") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(tableName)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "tableName为空", "没有传tableName");
                }
                if (version < 0L) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "version小于0", "version不能小于0");
                }
            }

            @Override
            protected DownloadUserDataResponse process() throws Exception {
                Long maxVersion = userDataSyncMapper.getUserDataVersion(realUserId, tableName);
                if (maxVersion == null || maxVersion <= version) {
                    return new DownloadUserDataResponse().setCurrentUpdateVersion(0L).setHasMore(false).setRecords(Collections.emptyList());
                }
                boolean hasMore = false;
                if (maxVersion - version > MAX_LINE_OF_USER_DOWNLOAD_DATA) {
                    maxVersion = version + MAX_LINE_OF_USER_DOWNLOAD_DATA;
                    hasMore = true;
                }
                List<ClientUserData> userDataList = userDataSyncMapper.getUpdatedUserData(realUserId, tableName, version, maxVersion);
                return new DownloadUserDataResponse().setCurrentUpdateVersion(maxVersion).setHasMore(hasMore).setRecords(userDataList);
            }
        }.execute();
    }

    @Transactional
    public long plusUserDataUpdateVersion(final long realUserId, final String tableName) {
        return new BizTemplate<Long>("UserDataSyncService.plusUserDataUpdateVersion") {

            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(tableName)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "tableName为空", "没有传tableName");
                }
            }

            @Override
            protected Long process() throws Exception {
                Long version = userDataSyncMapper.getUserDataVersion(realUserId, tableName);
                if (version == null) {
                    userDataSyncMapper.addOneOnUserDataVersion(realUserId, tableName);
                } else {
                    userDataSyncMapper.updateAddOneOnUserDataVersion(realUserId, tableName);
                }
                Long currentVersion = userDataSyncMapper.getUserDataVersion(realUserId, tableName);
                if (currentVersion == null) {
                    throw new VinciException(ErrorCode.INTERNAL_SERVER_ERROR, "数据库错误,无法插入syncdb_client_user_data_version_seq", "内部错误请重试");
                }
                return currentVersion;
            }
        }.execute();
    }

    @Transactional
    public boolean uploadUserData(final long realUserId, final String tableName, final long cv, final List<ClientUserData> datas) {
        return new BizTemplate<Boolean>(getClass().getSimpleName() + ".uploadUserData") {
            @Override
            protected void checkParams() throws VinciException {
                if (StringUtils.isEmpty(tableName)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "tableName为空", "没有传tableName");
                }
                if (CollectionUtils.isEmpty(datas)) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入数据", "没有传入数据");
                }
                if (CollectionUtils.size(datas) > 100) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "传入的数据条数大于100", "传入的数据条数过大");
                }
            }

            @Override
            protected Boolean process() throws Exception {
                Map<String, ClientUserData> userDataMap = Maps.newHashMapWithExpectedSize(datas.size());
                for (ClientUserData data : datas) {
                    if (data == null || StringUtils.isEmpty(data.getDataPk())) {
                        continue;
                    }
                    data.setRealUserId(realUserId);
                    data.setTableName(tableName);
                    data.setUpdateVersion(cv);
                    userDataMap.put(data.getDataPk(), data);
                }
                if (userDataMap.isEmpty()) {
                    throw new VinciException(ErrorCode.ARGUMENT_ERROR, "没有传入数据", "没有传入数据");
                }

                Map<String, ClientUserData> oldUserDataMap = userDataSyncMapper.getUserDatas(realUserId, tableName, userDataMap.keySet());

                List<ClientUserData> insertedDataList = Lists.newArrayListWithCapacity(
                        (userDataMap.size() - oldUserDataMap.size()) <= 0 ? 10 : (userDataMap.size() - oldUserDataMap.size()));
                List<ClientUserData> updatedDataList = Lists.newArrayListWithCapacity(oldUserDataMap.size());

                for (ClientUserData data : userDataMap.values()) {
                    if (oldUserDataMap.containsKey(data.getDataPk())) {
                        data.setId(oldUserDataMap.get(data.getDataPk()).getId());
                        updatedDataList.add(data);
                    } else {
                        insertedDataList.add(data);
                    }
                }
                if (insertedDataList.size() > 0) {
                    userDataSyncMapper.insertUserData(insertedDataList);
                }
                if (updatedDataList.size() > 0) {
                    userDataSyncMapper.updateUserDatas(cv, updatedDataList);
                }
                return true;
            }
        }.execute();
    }

    public boolean checkUploadUserData(long realUserId, String tableName, long version) {
        return new BizTemplate<Boolean>(getClass().getSimpleName() + ".checkUploadUserData") {

            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected Boolean process() throws Exception {
                if (realUserId <=0 || StringUtils.isEmpty(tableName) || version<=0) {
                    return false;
                }
                int len = userDataSyncMapper.checkUpdateUserData(realUserId, tableName, version);
                return len>0;
            }
        }.execute();
    }

    public List<ClientUserData> getUserData(long realUserId, String tableName, long version) {
        return new BizTemplate<List<ClientUserData>>(getClass().getSimpleName() + ".getUserData") {
            @Override
            protected void checkParams() throws VinciException {

            }

            @Override
            protected List<ClientUserData> process() throws Exception {
                if (realUserId <=0 || StringUtils.isEmpty(tableName) || version<=0) {
                    return null;
                }

                List<ClientUserData> list = userDataSyncMapper.getMultiUserDatas(realUserId, tableName, version);
                return list;
            }
        }.execute();
    }
}
