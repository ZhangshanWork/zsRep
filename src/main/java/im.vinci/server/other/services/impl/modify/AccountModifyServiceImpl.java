package im.vinci.server.other.services.impl.modify;

import im.vinci.server.common.exceptions.ServerErrorException;
import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import im.vinci.server.other.persistence.fetch.AccountFetchMapper;
import im.vinci.server.other.services.modify.AccountModifyService;
import org.apache.commons.codec.binary.Base64;
import org.apache.ibatis.exceptions.PersistenceException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;

/**
 * Created by henryhome on 2/27/15.
 */
@Service
public class AccountModifyServiceImpl implements AccountModifyService {

    @Autowired
    private Environment env;

    @Autowired
    private AccountFetchMapper accountFetchMapper;

    private Logger logger = LoggerFactory.getLogger(getClass());


    public VinciUserDetails createUserDetails(String deviceId) throws Exception {

        String accessId = generateUniqueAccessId();
        String accessKey = generateUniqueAccessKey();

        DateTime now = new DateTime();
        int accountExpiration = Integer.parseInt(env.getProperty("auth.account_expiration"));
        DateTime accountExpiredTime = now.plusMonths(accountExpiration);
        int credentialsExpiration = Integer.parseInt(env.getProperty("auth.credentials_expiration"));
        DateTime credentialsExpiredTime = now.plusMonths(credentialsExpiration);

        VinciUserDetails userDetails = new VinciUserDetails(accessId, null, deviceId, accessKey,
                accountExpiredTime.toDate(), credentialsExpiredTime.toDate(),
                true, true, true);

        return userDetails;
    }

    private String generateUniqueAccessId() throws Exception {

        Boolean isUnique = false;
        String accessId = null;
        VinciUserDetailsBuilder userDetails;

        while (!isUnique) {
            try {
                accessId = generateBase64EncodedRandomProperty(120);
            } catch (NoSuchAlgorithmException e) {
                logger.error("The algorithm is not found", e);
                throw new ServerErrorException();
            }

            userDetails = accountFetchMapper.getUserDetailsByAccessId(accessId);

            if (userDetails != null) {
                continue;
            }

            isUnique = true;
        }

        return accessId;
    }

    private String generateUniqueAccessKey() throws Exception {
        boolean isUnique = false;
        String accessKey = null;

        VinciUserDetailsBuilder userDetails;

        while (!isUnique) {
            try {
                accessKey = generateBase64EncodedRandomProperty(240);
            } catch (NoSuchAlgorithmException e) {
                throw new PersistenceException(e);
            }

            userDetails = accountFetchMapper.getUserDetailsByAccessKey(accessKey);

            if (userDetails != null) {
                continue;
            }

            isUnique = true;
        }

        return accessKey;
    }

    private String generateBase64EncodedRandomProperty(int length)
            throws
            NoSuchAlgorithmException {

        String algorithm = env.getProperty("auth.encoded_algorithm");
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithm);
        keyGen.init(length);
        byte[] property = keyGen.generateKey().getEncoded();
        return Base64.encodeBase64String(property);
    }
}




