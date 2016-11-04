package im.vinci.server.statistic.domain;

/**
 * Created by zhongzhengkai on 15/12/22.
 */
public class UserGenderStat {

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private int count;
    private String gender;

}
