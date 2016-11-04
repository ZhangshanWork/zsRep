package im.vinci.server.statistic.domain;

/**
 * Created by zhongzhengkai on 15/12/22.
 */
public class UserAgeRangeStat {

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    private int count;
    private String ageRange;


}
