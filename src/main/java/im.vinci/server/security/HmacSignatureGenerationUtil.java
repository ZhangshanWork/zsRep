package im.vinci.server.security;

import im.vinci.server.utils.DateUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


/**
 *
 */
public class HmacSignatureGenerationUtil {

//    private final static String ALGORITHM = "HmacSHA256";
    private final static String ALGORITHM = "HmacMD5";
    private final static String CHARSET_NAME = "UTF-8";

    private static Logger logger = LoggerFactory.getLogger(HmacSignatureGenerationUtil.class);
    
    public static String generateSignature(String strToSign, String accessKey)  {

        try {
            return DatatypeConverter.printHexBinary(hmacDigest(strToSign, accessKey));
        } catch (Exception e) {
            logger.warn("hmac sign got error : ",e);
            return StringUtils.EMPTY;
        }
    }

    public static String genToken(String mac , String imei , int hour) {

        String sign_1 = generateSignature(mac+"7GdXc0MF6IiUH97E5Aav"+imei,"LprZjQcBNjbJ7jSVpQ0K");
        String time = DateUtils.nextHourYMDHmsStr(hour).substring(0,13);
        try {
            return Base64.encodeBase64String(hmacDigest("WLDscfemBPePXpPO+mtE" + time + sign_1,"LprZjQcBNjbJ7jSVpQ0K"));
        } catch (Exception e) {
            logger.warn("hmac sign got error : ",e);
            return StringUtils.EMPTY;
        }
    }

    private static byte[] hmacDigest(String strToSign,
                                     String accessKey) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {

        SecretKey key = new SecretKeySpec(accessKey.getBytes(CHARSET_NAME), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);

        return mac.doFinal(strToSign.getBytes(CHARSET_NAME));
    }

    private static String generateBase64EncodedRandomProperty(int length)
            throws
            NoSuchAlgorithmException {

        String algorithm = "HmacSHA1";
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(length);
        byte[] property = keyGen.generateKey().getEncoded();
        return Base64.encodeBase64String(property);
    }
}
