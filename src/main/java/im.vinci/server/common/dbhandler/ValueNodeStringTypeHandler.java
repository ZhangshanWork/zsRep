package im.vinci.server.common.dbhandler;

import com.fasterxml.jackson.databind.JsonNode;
import im.vinci.server.utils.JsonUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.*;

public class ValueNodeStringTypeHandler implements TypeHandler<JsonNode> {
    @Override
    public void setParameter(PreparedStatement preparedStatement, int i, JsonNode node, JdbcType jdbcType)
            throws SQLException {
        if (node == null) {
            preparedStatement.setNull(i, Types.VARCHAR);
        } else {
            preparedStatement.setString(i, JsonUtils.encode(node));
        }
    }

    @Override
    public JsonNode getResult(ResultSet resultSet, String s) throws SQLException {
        String str = resultSet.getString(s);
        return JsonUtils.decode(str);
    }

    @Override
    public JsonNode getResult(ResultSet resultSet, int i) throws SQLException {
        String str = resultSet.getString(i);
        return JsonUtils.decode(str);
    }

    @Override
    public JsonNode getResult(CallableStatement callableStatement, int i) throws SQLException {
        String str = callableStatement.getString(i);
        return JsonUtils.decode(str);
    }
}
