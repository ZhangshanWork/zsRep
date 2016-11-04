package im.vinci.server.naturelang.musicpick.pick;

import com.hankcs.hanlp.HanLP;
import im.vinci.server.naturelang.musicpick.util.LCSdistance;
import im.vinci.server.naturelang.musicpick.util.SortMapByValue;
import im.vinci.server.naturelang.service.impl.NatureLangServiceImpl;
import im.vinci.server.search.domain.music.MusicSong;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Service
public class PickElementService{
	 private  final ArrayList<String> firstPersonNoun = loadFile("nlp/pick/prefix");
	 private  final ArrayList<String> postfix = loadFile("nlp/pick/postfix");
	 private  final ArrayList<String> jingxuanji_keyword = loadFile(
			"nlp/pick/jingxuanjiwords_mixXiamiFeeling");
	 private  final Map<String, String> xiamiFeeling = loadFileToMap("nlp/pick/XiaMiFeelingExtent");
	 @Autowired
	 private NatureLangServiceImpl natureLangService;
	 
	public static Map<String, String> loadFileToDict(String loc) {
		Map<String, String> dict = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource(loc).getInputStream(), "utf8"));
			String line = br.readLine();
			while (line != null) {
				String[] id_loc = line.split(" ");
				dict.put(id_loc[0], id_loc[1]);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dict;
	}

	public  Map<String, String> loadFileToMap(String loc) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource(loc).getInputStream(), "utf8"));

			String line = br.readLine();
			String core = "";

			while (line != null) {
				if (line.contains("\t")) {
					map.put(line.trim(), core);
				} else {
					core = line;
				}

				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static ArrayList<String> loadFile(String loc) {
		ArrayList<String> list = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new ClassPathResource(loc).getInputStream(), "utf8"));

			String line = br.readLine();
			while (line != null) {
				list.add(line);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public  String cutPostfix(String result) {
		for (String p : postfix) {
			if (result.endsWith(p)) {
				return result.replace(p, "");
			}
		}
		return result;
	}

	public  String cutPrefix(String command) {
		// command = command.replace("音乐", "");
		for (String fpn : firstPersonNoun) {
			if (command.startsWith(fpn)) {
				return command.replaceFirst(fpn, "");
			}

		}
		return command;
	}

	public  String commandInterpret(String command) {
		command = cutPrefix(command);
		String finalResult = "";

		Pair max = new Pair(jingxuanji_keyword.get(0), 0.0);

		for (String s : jingxuanji_keyword) {
			// process jingxuanji_keyword first
			String processed_s = HanLP.convertToSimplifiedChinese(s);
			if (s.equals("休假"))
				processed_s = "休假";
			processed_s = processed_s.toLowerCase();
			String processed_command = command.toLowerCase();
			Double curScore = LCSdistance.lcs_distance2(processed_command, processed_s);
			if (curScore > max.getV()) {
				max = new Pair(processed_s, curScore);
			}
		}
		finalResult = max.getK();
		if (xiamiFeeling.containsKey(max.getK())) {
			finalResult = xiamiFeeling.get(max.getK());
			// System.out.println("xiamiFeeling Match:"+finalResult);
		}
		finalResult = cutPostfix(finalResult);
		// System.out.println("after clear postfix:"+finalResult);
		return finalResult;
	}

	// retrieve top 100 songs, and print
	public  List<String> retriSongsFromCollections_local_cid_table(JSONArray collections) throws Exception {

		if (collections.size() == 0) {
			collections = natureLangService.getCollections("好听");
		}
		Map<String, Double> songs_count = new HashMap<String, Double>();
		ArrayList<JSONArray> songsFromCollections = natureLangService.getSongsFromNCollections_local(collections,
				null);
		for (JSONArray songs : songsFromCollections) {
			for (Object song : songs) {
				JSONObject songJSON = JSONObject.fromObject(song);

				if (songs_count.containsKey(songJSON.toString())) {
					songs_count.put(songJSON.toString(), songs_count.get(songJSON.toString()) + 1.0);
				} else {
					songs_count.put(songJSON.toString(), 1.0);
				}
			}
		}
		List<String> songs = new ArrayList<String>();
		List<Entry<String, Double>> sortedMap = SortMapByValue.sort(songs_count);

		for (int i = sortedMap.size() - 1; i >= sortedMap.size() - 100; i--) {
			if (i < 0)
				break;
			songs.add(sortedMap.get(i).getKey());
		}
		return songs;
	}



	/*
	 * 获取语素匹配结果
	 * @param lang
	 * */
	public  List<MusicSong> getPickResult(String lang) throws Exception{
		List<MusicSong> list = new ArrayList<>();
		String finalResult = commandInterpret(lang);
		System.out.println("input:" + lang + "   output:" + finalResult);
		JSONArray collections = natureLangService.getCollections(finalResult);
		List<String> songs = retriSongsFromCollections_local_cid_table(collections);
		for(String str:songs) {
			list.add(transJsonToMusicSongs(JSONObject.fromObject(str)));
		}
		return list;
	}
	
	/*
	 * 将json文件转换为MusicSong
	 * */
	public  MusicSong transJsonToMusicSongs(JSONObject object) {
		MusicSong musicSong =  new MusicSong("xiami");;
		if(!ObjectUtils.isEmpty(object.get("song_id"))) {
			musicSong.setSong_id(object.getLong("song_id"));
		}
		if(!ObjectUtils.isEmpty(object.get("album_id"))) {
			musicSong.setAlbum_id(object.getLong("album_id"));
		}
		/*if(!ObjectUtils.isEmpty(object.get("play_seconds"))) {
			musicSong.setPlay_counts(object.getLong("play_seconds"));
		}*/
		if(!ObjectUtils.isEmpty(object.get("album_logo"))) {
			musicSong.setAlbum_logo(object.getString("album_logo"));
		}
		if(!ObjectUtils.isEmpty(object.get("singers"))){
			musicSong.setAlbum_name(object.getString("singers"));
		}
		if(!ObjectUtils.isEmpty(object.get("song_name"))) {
			musicSong.setSong_name(object.getString("song_name"));
		}
		if(!ObjectUtils.isEmpty(object.get("artist_name"))) {
			musicSong.setArtist(object.getString("artist_name"));
		}
		if(!ObjectUtils.isEmpty(object.get("album_name"))) {
			musicSong.setAlbum_name(object.getString("album_name"));
		}
		if(!ObjectUtils.isEmpty(object.get("play_counts"))) {
			musicSong.setPlay_counts(object.getLong("play_counts"));
		}
		return musicSong;
	}
}
