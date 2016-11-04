package im.vinci.server.other.domain.profile;

import im.vinci.server.common.exceptions.ServerErrorException;
import im.vinci.server.utils.VinciUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by henryhome on 10/24/15.
 */
public class UserMusicProfileConstant {

    public static final String MUSIC_REGION = "音乐地区";
    public static final String MUSIC_CATEGORY = "音乐种类";
    public static final String MUSIC_STYLE = "音乐风格";
    public static final String FAVORITE_SINGER = "最喜爱歌手";
    public static final String MUSIC_TIMELINESS = "音乐时效性";
    public static final String ALL = "全选";

    private static Environment env;

    private static Logger logger = LoggerFactory.getLogger(UserMusicProfileConstant.class);

    public static Map<String, List<String>> getMusicProfileMap() throws ServerErrorException {
        Map<String, List<String>> musicProfileMap = new HashMap<>();

        String musicRegionStr = env.getProperty("user.music_region_keywords");
        String musicCategoryStr = env.getProperty("user.music_category_keywords");
        String musicStyleStr = env.getProperty("user.music_style_keywords");
        String musicTimelinessStr = env.getProperty("user.music_timeliness_keywords");
        String allKeywordStr = env.getProperty("user.all_keywords");

        try {
            musicRegionStr = VinciUtils.convertToUTF8Str(musicRegionStr);
            musicCategoryStr = VinciUtils.convertToUTF8Str(musicCategoryStr);
            musicStyleStr = VinciUtils.convertToUTF8Str(musicStyleStr);
            musicTimelinessStr = VinciUtils.convertToUTF8Str(musicTimelinessStr);
            allKeywordStr = VinciUtils.convertToUTF8Str(allKeywordStr);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            throw new ServerErrorException();
        }

        List<String> musicRegionKeywords = Arrays.asList(musicRegionStr.split("\\|"));
        List<String> musicCategoryKeywords = Arrays.asList(musicCategoryStr.split("\\|"));
        List<String> musicStyleKeywords = Arrays.asList(musicStyleStr.split("\\|"));
        List<String> musicTimelinessKeywords = Arrays.asList(musicTimelinessStr.split("\\|"));
        List<String> allKeywords = Arrays.asList(allKeywordStr.split("\\|"));

        musicProfileMap.put(MUSIC_REGION, musicRegionKeywords);
        musicProfileMap.put(MUSIC_CATEGORY, musicCategoryKeywords);
        musicProfileMap.put(MUSIC_STYLE, musicStyleKeywords);
        musicProfileMap.put(MUSIC_TIMELINESS, musicTimelinessKeywords);
        musicProfileMap.put(ALL, allKeywords);

        return musicProfileMap;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }
}



