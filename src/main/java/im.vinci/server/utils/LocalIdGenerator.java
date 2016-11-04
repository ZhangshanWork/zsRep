package im.vinci.server.utils;

import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ip(int)%10000 + 到20160810天数(4位) + 当天的秒数(5位) + 毫秒最后一位 + 4位随机数(算是)
 */
public enum LocalIdGenerator {

    INSTANCE();

    private final static long BASE_TIME = 1470758400L; // 2016-08-10 00:00:00 in seconds

    private static AtomicInteger idGenerator = new AtomicInteger(0);

    private final String IP_NUM;

    private final short[] HIGH_DATA = new short[100];

    private final short[] LOW_DATA = new short[100];

    public static void main(String[] args) {
        System.out.println(INSTANCE.generateId());
    }
    LocalIdGenerator() {
        IP_NUM = String.valueOf(Networks.ip2Num(Networks.getSiteIp()) % 10000);

        List<Short> data = Lists.newArrayListWithExpectedSize(100);
        for (short i = 0; i < 100; i = (short) (i + 1)) {
            data.add(i);
        }
        Collections.shuffle(data);
        toArray(data, HIGH_DATA);
        Collections.shuffle(data);
        toArray(data, LOW_DATA);
    }

    private static short[] toArray(List<Short> shortList, short[] data) {
        int len = shortList.size();
        for (int i = 0; i < len; i++) {
            data[i] = shortList.get(i);
        }
        return data;
    }

    public long generateId() {
        StringBuilder builder = new StringBuilder(IP_NUM);
        long ts = System.currentTimeMillis();
        long l = ts/1000 - BASE_TIME;
        int s = (int) (ts%10);
//        System.out.println(builder);
        builder.append(getDataStr(l/86400,4));
//        System.out.println(builder);
        builder.append(getDataStr(l%86400,5));
//        System.out.println(builder);
        builder.append(s);
//        System.out.println(builder);
        return Long.valueOf(builder.append(getNextSixOffset(s)).toString());
    }

    private int getHighDataIndex(int id, int delta) {
        return HIGH_DATA[((id / 100 + delta) % 100)];
    }

    private int getLowDataIndex(int id, int delta) {
        return LOW_DATA[((id % 100 + delta) % 100)];
    }

    private String getDataStr(long data, int len) {
        String d = String.valueOf(data);
        StringBuilder builder = new StringBuilder();
        for (int i=0,j = len - d.length(); i<j ; i++) {
            builder.append("0");
        }
        return builder.append(d).toString();
    }

    private String getNextSixOffset(int delta) {
        StringBuilder builder = new StringBuilder();
        idGenerator.compareAndSet(9999, 0);
        int id = idGenerator.getAndIncrement();
        builder.append(getDataStr(getHighDataIndex(id, delta),2));
        builder.append(getDataStr(getLowDataIndex(id, delta),2));
        return builder.toString();
    }

}