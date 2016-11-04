package im.vinci.server.utils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Author: haolin
 * Email: haolin.h0@gmail.com
 * Date: 29/9/15
 */
public class Networks {

    /**
     * 获取主机名
     * @return 主机名
     */
    public static String getHostName() {

        String name = null;
        try {
            Enumeration<NetworkInterface> infs = NetworkInterface.getNetworkInterfaces();
            while (infs.hasMoreElements() && (name == null)) {
                NetworkInterface net = infs.nextElement();
                if (net.isLoopback()) {
                    continue;
                }
                Enumeration<InetAddress> addr = net.getInetAddresses();
                while (addr.hasMoreElements()) {

                    InetAddress inet = addr.nextElement();

                    if (inet.isSiteLocalAddress()) {
                        name = inet.getHostAddress();
                    }

                    if (!inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress())) {
                        name = inet.getCanonicalHostName();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            name = "localhost";
        }
        return name;
    }

    /**
     * 获取内网IP
     * @return 内网IP
     */
    public static String getSiteIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                Enumeration<InetAddress> addresses = network.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.isSiteLocalAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * 把IP按点号分4段，每段一整型就一个字节来表示，通过左移位来实现。
     * 第一段放到最高的8位，需要左移24位，依此类推即可
     *
     * @param ipStr ip地址
     * @return 整形
     */
    public static Integer ip2Num(String ipStr) {
        if (ipStr == null || "".equals(ipStr)) {
            return -1;
        }

        if (ipStr.contains(":")) {
            //ipv6的地址，不解析，返回127.0.0.1
            ipStr = "127.0.0.1";
        }

        String[] ips = ipStr.split("\\.");

        return (Integer.parseInt(ips[0]) << 24) + (Integer.parseInt(ips[1]) << 16) + (Integer.parseInt(ips[2]) << 8) + Integer.parseInt(ips[3]);
    }

    /**
     * 把整数分为4个字节，通过右移位得到IP地址中4个点分段的值
     *
     * @param ipNum ip int value
     * @return ip str
     */
    public static String num2Ip(int ipNum) {
        return ((ipNum >> 24) & 0xFF) + "." + ((ipNum >> 16) & 0xFF) + "." + ((ipNum >> 8) & 0xFF) + "." + (ipNum & 0xFF);
    }

    private static final String[] HEADERS_TO_TRY = {
            "x-real-ip",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR" };

    /**
     * 通过HttpServletRequest返回IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String rip = request.getRemoteAddr();
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                rip = ip;
                break;
            }
        }
        int pos = rip.indexOf(',');
        if (pos >= 0) {
            rip = rip.substring(0, pos);
        }
        return rip;
    }
}
