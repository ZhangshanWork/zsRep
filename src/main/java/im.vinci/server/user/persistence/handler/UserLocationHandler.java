package im.vinci.server.user.persistence.handler;

import im.vinci.server.user.domain.UserLocation;
import im.vinci.server.utils.JsonUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserLocation
 * Created by tim@vinci on 16/7/19.
 */
public class UserLocationHandler implements TypeHandler<UserLocation> {

    @Override
    public void setParameter(PreparedStatement ps, int i, UserLocation parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null) {
            ps.setString(i,"{}");
        } else {
            ps.setString(i, JsonUtils.encode(parameter));
        }
    }

    @Override
    public UserLocation getResult(ResultSet rs, String columnName) throws SQLException {
        String s = rs.getString(columnName);
        UserLocation location = JsonUtils.decode(s,UserLocation.class);
        if (location == null) {
            return new UserLocation();
        }
        return location;
    }

    @Override
    public UserLocation getResult(ResultSet rs, int columnIndex) throws SQLException {
        String s = rs.getString(columnIndex);
        UserLocation location = JsonUtils.decode(s,UserLocation.class);
        if (location == null) {
            return new UserLocation();
        }
        return location;
    }

    @Override
    public UserLocation getResult(CallableStatement cs, int columnIndex) throws SQLException {
        String s = cs.getString(columnIndex);
        UserLocation location = JsonUtils.decode(s,UserLocation.class);
        if (location == null) {
            return new UserLocation();
        }
        return location;
    }
}
