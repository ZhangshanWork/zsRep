package im.vinci.server.other.domain.wrappers.requests.user;

/**
 * Created by henryhome on 9/11/15.
 */
public class UserInfoGeneration {

    private Integer userId;
    private Integer age;
    private String gender;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
