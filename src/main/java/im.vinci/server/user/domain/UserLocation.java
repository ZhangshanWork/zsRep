package im.vinci.server.user.domain;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * 用户位置字段
 * Created by tim@vinci on 16/7/19.
 */
public class UserLocation implements Serializable {
    private String country;

    private String province;

    private String city;

    public UserLocation() {
    }

    public UserLocation(String country, String province, String city) {
        this.country = country;
        this.province = province;
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public UserLocation setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getProvince() {
        return province;
    }

    public UserLocation setProvince(String province) {
        this.province = province;
        return this;
    }

    public String getCity() {
        return city;
    }

    public UserLocation setCity(String city) {
        this.city = city;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserLocation that = (UserLocation) o;
        return Objects.equal(getCountry(), that.getCountry()) &&
                Objects.equal(getProvince(), that.getProvince()) &&
                Objects.equal(getCity(), that.getCity());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getCountry(), getProvince(), getCity());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("country", country)
                .add("province", province)
                .add("city", city)
                .toString();
    }
}
