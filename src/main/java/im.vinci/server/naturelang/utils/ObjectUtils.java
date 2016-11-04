package im.vinci.server.naturelang.utils;

import com.taobao.api.domain.StandardSong;
import im.vinci.server.search.domain.music.MusicSong;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectUtils {
	//判断字符串型对象是否为空
	public static boolean isNotEmperty(Object obj){
		if(obj!=null&&StringUtils.isNotBlank(obj+"")){
			return true;
		}
		return false;
	}
	
	public static String objToString(Object obj){
		if(obj==null){
			return "";
		}
		return obj.toString();
	}

    public static String listToString(List<String> list) {
        String result = "";
        for (String str : list) {
            result += str;
        }
        return result;
    }

    public static List<MusicSong> transferToMusicSongList(List<StandardSong> standardSongs) {
        List<MusicSong> list = new ArrayList<>();
        if(!CollectionUtils.isEmpty(standardSongs)){
            for(StandardSong song:standardSongs)
            list.add(toStandSong(song));
        }
        Collections.shuffle(list);
        return list;
    }

    private static MusicSong toStandSong(StandardSong standardSong) {
        MusicSong song = new MusicSong("xiami");
        if(StringUtils.isNotBlank(standardSong.getSongId()+"")){
            song.setSong_id(standardSong.getSongId());
        }
        if(StringUtils.isNoneBlank(standardSong.getSongName())){
            song.setSong_name(standardSong.getSongName());
        }
        if(StringUtils.isNoneBlank(standardSong.getArtistName())){
            song.setArtist(standardSong.getArtistName());
        }
        if(StringUtils.isNoneBlank(standardSong.getArtistId()+"")){
            song.setArtist_id(standardSong.getArtistId());
        }
        if(StringUtils.isNoneBlank(standardSong.getAlbumId()+"")){
            song.setAlbum_id(standardSong.getAlbumId());
        }
        if(StringUtils.isNoneBlank(standardSong.getAlbumName())){
            song.setAlbum_name(standardSong.getAlbumName());
        }
        if(StringUtils.isNoneBlank(standardSong.getListenFile())){
            song.setListen_file(standardSong.getListenFile());
        }
        if(StringUtils.isNoneBlank(standardSong.getSingers())){
            song.setSingers(standardSong.getSingers());
        }
        return song;
    }

    public static String iflyAsrFilter(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        StringBuilder b = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {

            char c = str.charAt(i);
            if (c == '　' || c == ' ') {
                if (b.length() != 0 && b.charAt(b.length() - 1) != ' ') {
                    b.append(' ');
                }
                continue;
            }
            int ci = (int) c;
            if (((ci >> 8) & 0xFF) == 0xFF) {
                ci = (ci & 0xFF) + 32;
                b.append((char) ci);
            } else if ((ci >= 48 && ci <= 57) || (ci >= 65 && ci <= 90) || (ci >= 97 && ci <= 122) || (ci >= 0x4E00 && ci <= 0x9FCF)) {
                //标准汉字范围
                b.append(c);
            } else {
                if (b.length() != 0 && b.charAt(b.length() - 1) != ' ') {
                    b.append(' ');
                }
            }

        } // end for.
        String result = b.toString();
        if (StringUtils.isEmpty(result) || StringUtils.isEmpty(result.replaceAll(" ", ""))) {
            return "";
        }
        return result;
    }

}
