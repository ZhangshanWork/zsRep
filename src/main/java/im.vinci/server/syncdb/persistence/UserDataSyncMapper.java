package im.vinci.server.syncdb.persistence;

import im.vinci.server.syncdb.domain.ClientUserData;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * client_user_data
 * client_user_data_version_seq
 * 两张表的数据库操作,主要作用是用户多客户端数据同步
 * Created by tim@vinci on 16/7/25.
 */
@Repository
public interface UserDataSyncMapper {

    @Insert({"<script>",
            "insert into client_user_data ",
            "(real_user_id, table_name, data_pk, data, is_delete, update_version, dt_create, dt_update) values ",
            "<foreach item='item' index='index' collection='list' separator=','>",
                    "(#{item.realUserId},#{item.tableName}, #{item.dataPk}, #{item.data}, #{item.isDelete}, #{item.updateVersion}, now(), now())",
            "</foreach>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyColumn = "id")
    int insertUserData(@Param("list") Collection<ClientUserData> list);


    @Update("update client_user_data set data=#{data}, update_version=#{updateVersion}, dt_update=now()" +
            " where real_user_id=#{realUserId} and table_name=#{tableName} and data_pk=#{dataPk}")
    int updateUserData(ClientUserData data);


    /**批量更新,但是必须要有id**/
    @Insert({"<script>",
            "replace into client_user_data ",
            "(id,real_user_id, table_name, data_pk, data, is_delete, update_version, dt_update) values ",
            "<foreach item='item' index='index' collection='list' separator=','>",
            "(#{item.id},#{item.realUserId},#{item.tableName}, #{item.dataPk}, #{item.data}, #{item.isDelete}, #{version}, now())",
            "</foreach>",
            "</script>"
    })
    int updateUserDatas(@Param("version") long version, @Param("list") Collection<ClientUserData> datas);


    @Update("update client_user_data set is_delete=1, update_version=#{updateVersion}, dt_update=now() " +
            "where real_user_id=#{realUserId} and table_name=#{tableName} and data_pk=#{dataPk}")
    int setUserDataDeleted(ClientUserData data);


    /**
     * 用户获取单条数据
     */
    @Select("select * from client_user_data where real_user_id=#{real_user_id} and table_name=#{table_name} and data_pk=#{pk}")
    ClientUserData getUserData(@Param("real_user_id") long realUserId, @Param("table_name") String tableName, @Param("pk") String dataPk);

    /**
     * 获取用户多条数据,map key 为 data_pk
     * @return
     */
    @Select({"<script>",
            "select * from client_user_data where real_user_id=#{real_user_id} and table_name=#{table_name} and data_pk in ",
            "<foreach item='item' index='index' collection='pks' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"})
    @MapKey("dataPk")
    Map<String, ClientUserData> getUserDatas(@Param("real_user_id") long realUserId, @Param("table_name") String tableName, @Param("pks") Collection<String> dataPk);
    /**
     * 获取所有没被删除的数据
     */
    @Select({"<script>",
            "select * from client_user_data force index (idx_get_list) ",
            " where real_user_id=#{real_user_id} and table_name=#{table_name} and is_delete=0",
            " and id>#{least_id} order by id ",
            "<if test='size>0'> limit #{size}</if>",
            "</script>"
    })
    List<ClientUserData> getAllUserData(@Param("real_user_id") long realUserId, @Param("table_name") String tableName,
                                        @Param("least_id") long leastId, @Param("size") int size);

    /**
     * 获取某次同步的所有数据
     *
     * @param realUserId
     * @param tableName
     * @param updateVersion
     * @return
     */
    @Select("select * from client_user_data where real_user_id=#{real_user_id} and table_name=#{table_name} and update_version=#{update_version}")
    List<ClientUserData> getMultiUserDatas(@Param("real_user_id") long realUserId, @Param("table_name") String tableName, @Param("update_version") long updateVersion);

    /**
     * 获取最近被更新过的数据 (update_version, max_update_version]
     * exclude updateVersion
     * include maxUpdateVersion
     */
    @Select({"<script>",
            "select * from client_user_data where real_user_id=#{real_user_id} and table_name=#{table_name} ",
            " and update_version > #{update_version} ",
            "<if test='max_update_version > 0'> and update_version &lt;= #{max_update_version}</if>",
            "</script>"
    })
    List<ClientUserData> getUpdatedUserData(@Param("real_user_id") long realUserId, @Param("table_name") String tableName,
                                            @Param("update_version") long updateVersion, @Param("max_update_version") long maxUpdateVersion);


    /**
     * 获取特定version的数据有多少条
     */
    @Select("select count(*) from client_user_data where real_user_id=#{real_user_id} and table_name=#{table_name} and update_version=#{update_version}")
    int checkUpdateUserData(@Param("real_user_id") long realUserId, @Param("table_name") String tableName,
                            @Param("update_version") long updateVersion);
    //------ 以下为version_seq表的操作 -----------------

    @Select("select update_version from client_user_data_version_seq where real_user_id=#{real_user_id} and table_name=#{table_name}")
    Long getUserDataVersion(@Param("real_user_id") long realUserId, @Param("table_name") String tableName);

    @Insert("insert into client_user_data_version_seq (real_user_id,table_name,update_version,dt_create,dt_update) values (#{real_user_id},#{table_name},1,now(),now()) "+
            " on duplicate key update update_version=update_version+1, dt_update=now()"
    )
    int addOneOnUserDataVersion(@Param("real_user_id") long realUserId, @Param("table_name") String tableName);

    @Update("update client_user_data_version_seq set update_version=update_version+1, dt_update=now() where real_user_id=#{real_user_id} and table_name=#{table_name}")
    int updateAddOneOnUserDataVersion(@Param("real_user_id") long realUserId, @Param("table_name") String tableName);


}
