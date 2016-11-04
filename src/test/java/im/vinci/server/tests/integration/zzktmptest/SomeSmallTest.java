package im.vinci.server.tests.integration.zzktmptest;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhongzhengkai on 15/12/1.
 */
public class SomeSmallTest {

    public static void main(String[] args){
        String str="this is a small piece string";
        byte[] bts=str.getBytes();
        try {
            System.out.println(new String(bts,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        try {
            System.out.println(InetAddress.getLocalHost().getHostAddress().indexOf("e"));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

//        new Paranel().doLogic();

        Set<String> set = new HashSet<String>(){{
            add("1");
        }};

        System.out.println(set);
        String[] array = set.toArray(new String[]{});
        System.out.println(array.toString());


    }

}


abstract class Base{

    abstract void process() throws Exception;

    int execute() throws Exception{
        try {
            process();
            return 11;
        } catch (Exception e) {
            throw new Exception("this is an exception from execute()");
        }finally {
            System.out.println("do finally block");
        }
    }
}

class Paranel{

    int doLogic() throws Exception{
        try {
//            new Base(){
//                @Override
//                protected void process() throws Exception{
//                    System.out.println("do process action");
//                    throw new Exception("this is an exception");
//                }
//            }.execute();
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            System.out.println("sss");
        }
        return 1;
    }

}