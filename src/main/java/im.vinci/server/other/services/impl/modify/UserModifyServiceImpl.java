package im.vinci.server.other.services.impl.modify;

import com.google.common.base.Joiner;
import im.vinci.server.common.exceptions.ServerErrorException;
import im.vinci.server.other.domain.account.VinciUserDetails;
import im.vinci.server.other.domain.account.VinciUserDetailsBuilder;
import im.vinci.server.other.domain.profile.UserMusicProfile;
import im.vinci.server.other.domain.profile.UserMusicProfileConstant;
import im.vinci.server.other.domain.user.Gender;
import im.vinci.server.other.domain.user.User;
import im.vinci.server.other.domain.wrappers.requests.profile.UserMusicProfileGeneration;
import im.vinci.server.other.domain.wrappers.requests.user.UserInfoGeneration;
import im.vinci.server.other.persistence.fetch.AccountFetchMapper;
import im.vinci.server.other.persistence.fetch.UserFetchMapper;
import im.vinci.server.other.persistence.modify.AccountModifyMapper;
import im.vinci.server.other.persistence.modify.UserModifyMapper;
import im.vinci.server.other.services.modify.AccountModifyService;
import im.vinci.server.other.services.modify.UserModifyService;
import im.vinci.server.utils.VinciUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by henryhome on 3/26/15.
 */
@Service
public class UserModifyServiceImpl implements UserModifyService {

    @Autowired
    private AccountFetchMapper accountFetchMapper;

    @Autowired
    private AccountModifyMapper accountModifyMapper;

    @Autowired
    private UserFetchMapper userFetchMapper;

    @Autowired
    private UserModifyMapper userModifyMapper;

    @Autowired
    private AccountModifyService accountModifyService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private Environment env;

    public Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void addUserInfo(UserInfoGeneration userInfoGeneration) throws Exception {

        Integer userId = userInfoGeneration.getUserId();
        Integer age = userInfoGeneration.getAge();
        String gender = userInfoGeneration.getGender();

        User user = new User();
        user.setUserId(userId);
        user.setAge(age);

        if (Gender.MALE.name() == gender) {
            user.setGender(Gender.MALE.name());
        } else if (Gender.FEMALE.name() == gender) {
            user.setGender(Gender.FEMALE.name());
        }

        userModifyMapper.addUser(user);
    }

    @Override
    public void addUserMusicProfile(UserMusicProfileGeneration userMusicProfileGeneration) throws Exception {
        String deviceId = userMusicProfileGeneration.getDeviceId();
        String gender = userMusicProfileGeneration.getGender();
        String ageRange = userMusicProfileGeneration.getAge();
        String musicRegion = userMusicProfileGeneration.getMusicRegion();
        String musicCategory = userMusicProfileGeneration.getMusicCategory();
        String musicStyle = userMusicProfileGeneration.getMusicStyle();
        String favoriteSinger = userMusicProfileGeneration.getFavoriteSinger();
        String musicTimeliness = userMusicProfileGeneration.getMusicTimeliness();

        final String genderToAdd = processGender(gender);
        final String ageRangeToAdd = processAgeRange(ageRange);
        final String musicRegionToAdd = processMusicProfile(UserMusicProfileConstant.MUSIC_REGION, musicRegion);
        final String musicCategoryToAdd = processMusicProfile(UserMusicProfileConstant.MUSIC_CATEGORY, musicCategory);
        final String musicStyleToAdd = processMusicProfile(UserMusicProfileConstant.MUSIC_STYLE, musicStyle);
        final String musicTimelinessToAdd = processMusicProfile(UserMusicProfileConstant.MUSIC_TIMELINESS, musicTimeliness);

        logger.info("The gender is " + genderToAdd);
        logger.info("The age range is " + ageRangeToAdd);
        logger.info("The region is " + musicRegionToAdd);
        logger.info("The category is " + musicCategoryToAdd);
        logger.info("The style is " + musicStyleToAdd);
        logger.info("The favorite singer is " + favoriteSinger);
        logger.info("The music timeliness is " + musicTimelinessToAdd);

        VinciUserDetailsBuilder detailsFromDB = accountFetchMapper.getUserDetailsByDeviceId(deviceId);
        Integer userId;

        if (detailsFromDB == null) {
            VinciUserDetails details = accountModifyService.createUserDetails(deviceId);

            userId = transactionTemplate.execute(new TransactionCallback<Integer>() {
                public Integer doInTransaction(TransactionStatus status) {
                    accountModifyMapper.addVinciUserDetails(details);

                    User user = new User();
                    user.setUserId(details.getId());
                    user.setGender(genderToAdd);
                    user.setAgeRange(ageRangeToAdd);

                    userModifyMapper.addUser(user);

                    return details.getId();
                }
            });
        } else {
            userId = detailsFromDB.getId();

            User user = new User();
            user.setUserId(userId);
            user.setGender(genderToAdd);
            user.setAgeRange(ageRangeToAdd);

            userModifyMapper.updateUser(user);
        }

        UserMusicProfile profileFromDB = userFetchMapper.getUserMusicProfileByUserId(userId);

        if (profileFromDB == null) {
            UserMusicProfile profile = new UserMusicProfile();
            profile.setUserId(userId);
            profile.setMusicRegion(musicRegionToAdd);
            profile.setMusicCategory(musicCategoryToAdd);
            profile.setMusicStyle(musicStyleToAdd);
            profile.setFavoriteSinger(favoriteSinger);
            profile.setMusicTimeliness(musicTimelinessToAdd);

            userModifyMapper.addUserMusicProfile(profile);
        } else {
            UserMusicProfile profile = new UserMusicProfile();
            profile.setUserId(userId);
            profile.setMusicRegion(musicRegionToAdd);
            profile.setMusicCategory(musicCategoryToAdd);
            profile.setMusicStyle(musicStyleToAdd);
            profile.setFavoriteSinger(favoriteSinger);
            profile.setMusicTimeliness(musicTimelinessToAdd);

            userModifyMapper.updateUserMusicProfile(profile);
        }
    }

    private String processGender(String gender) {
        if (gender != null) {
            logger.info("The original gender sentence is " + gender);

            String maleKeywordStr = env.getProperty("user.male_keywords");
            String femaleKeywordStr = env.getProperty("user.female_keywords");
            String neutralKeywordStr = env.getProperty("user.neutral_keywords");

            try {
                maleKeywordStr = VinciUtils.convertToUTF8Str(maleKeywordStr);
                femaleKeywordStr = VinciUtils.convertToUTF8Str(femaleKeywordStr);
                neutralKeywordStr = VinciUtils.convertToUTF8Str(neutralKeywordStr);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
                throw new ServerErrorException();
            }

            List<String> maleKeywords = Arrays.asList(maleKeywordStr.split("\\|"));
            List<String> femaleKeywords = Arrays.asList(femaleKeywordStr.split("\\|"));
            List<String> neutralKeywords = Arrays.asList(neutralKeywordStr.split("\\|"));


            boolean found = false;
            for (String keyword : maleKeywords) {
                if (gender.contains(keyword)) {
                    gender = Gender.MALE.name();
                    found = true;
                    break;
                }
            }

            if (!found) {
                for (String keyword : femaleKeywords) {
                    if (gender.contains(keyword)) {
                        gender = Gender.FEMALE.name();
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                for (String keyword : neutralKeywords) {
                    if (gender.contains(keyword)) {
                        gender = Gender.NEUTRAL.name();
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return null;
            }
        }

        return gender;
    }

    private String processAgeRange(String ageRange) throws Exception {
        if (ageRange != null) {
            logger.info("The original age range sentence is " + ageRange);

            String ageRangeStr = env.getProperty("user.age_range_keywords");

            try {
                ageRangeStr = VinciUtils.convertToUTF8Str(ageRangeStr);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
                throw new ServerErrorException();
            }

            List<String> ageRangeKeywords = Arrays.asList(ageRangeStr.split("\\|"));

            boolean found = false;
            for (String keyword : ageRangeKeywords) {
                if (ageRange.contains(keyword)) {
                    ageRange = keyword;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return null;
            }
        }

        return ageRange;
    }

    private String processMusicProfile(String key, String musicInfo) throws Exception {
        List<String> musicInfoList = new ArrayList<>();

        if (musicInfo != null) {
            logger.info("The original music info sentence is " + musicInfo);

            Map<String, List<String>> musicProfileMap = UserMusicProfileConstant.getMusicProfileMap();

            List<String> keywords = musicProfileMap.get(key);

            boolean found = false;

            for (String keyword : keywords) {
                if (musicInfo.contains(keyword)) {
                    musicInfoList.add(keyword);
                    found = true;
                }
            }

            if (!found) {
                keywords = musicProfileMap.get(UserMusicProfileConstant.ALL);

                for (String keyword : keywords) {
                    if (musicInfo.contains(keyword)) {
                        if (key == UserMusicProfileConstant.MUSIC_REGION) {
                            musicInfoList.add("华语");
                            musicInfoList.add("欧美");
                            musicInfoList.add("日韩");
                        } else if (key == UserMusicProfileConstant.MUSIC_CATEGORY) {
                            musicInfoList.add("独立");
                            musicInfoList.add("流行");
                            musicInfoList.add("小众");
                            musicInfoList.add("大众");
                        } else if (key == UserMusicProfileConstant.MUSIC_STYLE) {
                            musicInfoList.add("民谣");
                            musicInfoList.add("摇滚");
                        } else if (key == UserMusicProfileConstant.MUSIC_TIMELINESS) {
                            musicInfoList.add("老歌");
                            musicInfoList.add("新歌");
                        }
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                return null;
            }
        }

        return Joiner.on(", ").join(musicInfoList);
    }
}



