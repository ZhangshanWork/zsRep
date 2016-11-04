package im.vinci.server.naturelang.service.impl.process;

import im.vinci.server.naturelang.domain.MusicSemantic;
import im.vinci.server.naturelang.domain.XMLYSemantic;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.utils.CommonUtils;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.naturelang.utils.SimilarityUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class ElasticProcess {
	private Logger logger = LoggerFactory.getLogger(getClass());
	List<MusicSemantic> musics = Collections
			.synchronizedList(new ArrayList<MusicSemantic>());
	List<XMLYSemantic> xmlys = Collections
			.synchronizedList(new ArrayList<XMLYSemantic>());
	private final String IndexName = "music_repository";
	private final boolean useSmart = true;
	// 对接收到的语音信息进行处理
	public List<MusicSemantic> natureLangProcess(Client client, String lang)
			throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		String prefix_dic_name = "prefix.dic";
		String suffix_dic_name = "suffix.dic";
		String template_name = "nlp.template";

		float[] score_array = new float[4];
		lang = Context.filterLang(lang);
		tokens = new ElasticUtils().preProcess(lang, score_array,
				prefix_dic_name, suffix_dic_name, useSmart);
		logger.info("Process Results:");
		for (int i = 0; i < tokens.size(); i++)
			logger.info(tokens.get(i) + "/");
		SearchResponse response = new ElasticUtils().search(client, IndexName,
				tokens, score_array, template_name);
		//针对英文点歌，进行二次判定过滤
		String temp = "";
		for (String str : tokens) {
			temp += str;
			if(str.matches("[a-zA-Z' ]+")){
				temp += "_";
			}
		}
		if(temp.matches("[a-zA-Z' _]+")&&!Context.IfSinger(temp)){
			SearchResponse responseEn = new ElasticUtils().searchForSongEn(client, IndexName, tokens, template_name,"origin");
			SearchResponse responseSmart = new ElasticUtils().searchForSongEn(client, IndexName, tokens, template_name,"smart");
			response = distinctSearchResponse(tokens,response,responseEn,responseSmart);
		}


		SearchHits hits = response.getHits();
		logger.info("the number of hits is :" + hits.getTotalHits());
		if (hits.getTotalHits() == 0) {
			return musics;
		}

		return filterMusicResult(hits, tokens,lang);
	}

	/**
	 * 国际版语义解析
	 * @param client
	 * @param lang
	 * @return
	 * @throws Exception
     */
	public List<MusicSemantic> nationLangProcess(Client client, String lang) throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		String prefix_dic_name = "prefix.dic";
		String suffix_dic_name = "suffix.dic";
		String template_name = "nlp.template";
		float[] score_array = new float[4];
		tokens = new ElasticUtils().preProcess(lang, score_array, prefix_dic_name, suffix_dic_name, useSmart);
		logger.info("Process Nation Results:");
		for (int i = 0; i < tokens.size(); i++) {
			logger.info(tokens.get(i) + "/");
		}
		SearchResponse response = new ElasticUtils().search(client, IndexName,
				tokens, score_array, template_name);
		SearchHits hits = response.getHits();
		logger.info("the number of hits is :" + hits.getTotalHits());
		if (hits.getTotalHits() == 0) {
			return musics;
		}
		return filterMusicResult(hits, tokens,lang);
	}

	// 区分流派解析，若非，则按照常规音乐处理
	private List<MusicSemantic> filterMusicResult(SearchHits hits,
			ArrayList<String> tokens,String lang) throws IOException {
		SearchHit hit = hits.getHits()[0];
		if (ObjectUtils.isNotEmperty(hit.getSource().get("type"))) {
			MusicSemantic semantic = new MusicSemantic();
			String type = hit.getSource().get("type").toString();
			if (Context.IfSingerInLang(lang)){
			   Map map = musicNotGenre(hits, lang);
				return filterXMResult1(map, tokens);
			}
			//若type以曲库结尾，则表明语义识别的结果是曲库信息
			if(type.endsWith("曲库")){
				semantic.setGenre(type);
			}else{
				semantic.setRank(type);
			}
			logger.info(semantic.getRank());
			musics.add(semantic);
		} else {
			Map map = chooseMusic(hit,hits,lang);
			return filterXMResult1(map, tokens);
		}
		return musics;
	}

	/**
	 * 判定返回结果，过滤曲风流派对结果的影响
	 * */
	private Map musicNotGenre(SearchHits hits, String lang) throws IOException {
		Map map = new HashMap<>();
		for(SearchHit temp:hits){
			if (!ObjectUtils.objToString(temp.getSource().get("type")).endsWith("曲库")){
				if (lang.contains(ObjectUtils.objToString(temp.getSource().get("singer_name")))
						&&!ObjectUtils.isNotEmperty(map.get("singer_name"))){
					map.put("singer_name", ObjectUtils.objToString(temp.getSource().get("singer_name")));
				}
				if (lang.contains(ObjectUtils.objToString(temp.getSource().get("song_name")))
						&&!ObjectUtils.isNotEmperty(map.get("song_name"))){
					map.put("song_name", ObjectUtils.objToString(temp.getSource().get("song_name")));
				}
				if ((lang.contains("专辑")||lang.contains("album"))&&lang.contains(ObjectUtils.objToString(temp.getSource().get("album_name")))
						&&!ObjectUtils.isNotEmperty(map.get("album_name"))){
					map.put("album_name", ObjectUtils.objToString(temp.getSource().get("album_name")));
				}
			}
		}
		return map;
	}

	/**
	 * 预先抽取结构中的数据，降低时间成本
	 **/
	private List<MusicSemantic> searchHitToList(SearchHits hits) {
		List<MusicSemantic> songList = new ArrayList<>();
		for(SearchHit temp:hits){
			MusicSemantic musicSemantic = new MusicSemantic();
			String artist = ObjectUtils.objToString(temp.getSource().get("singer_name"));
			if (StringUtils.isNotBlank(artist)){
				musicSemantic.setArtist(artist);
			}
			String song = ObjectUtils.objToString(temp.getSource().get("song_name"));
			if (StringUtils.isNotBlank(song)){
				musicSemantic.setSong(song);
			}
			String album = ObjectUtils.objToString(temp.getSource().get("album_name"));
			if (StringUtils.isNotBlank(album)){
				musicSemantic.setAlbum(album);
			}
			songList.add(musicSemantic);
		}
		return songList;
	}

	/**
	* 对返回的结果集进行过滤
	* */
	private Map chooseMusic(SearchHit hit, SearchHits hits, String lang) throws IOException {
		Map<String ,String> map = new HashMap<>();
		lang = CommonUtils.filter(lang);
		List<MusicSemantic> musicSemanticList = searchHitToList(hits);
		if (StringUtils.isNotBlank(Context.getSingerInlang(lang))) {
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getArtist())&&lang.contains(temp.getArtist())
						&&!StringUtils.isNotBlank(map.get("singer_name"))){
					map.put("singer_name", temp.getArtist());
					lang = lang.replaceFirst(temp.getArtist(), "");
				}
			}
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getSong())&&lang.contains(temp.getSong())
						&&!StringUtils.isNotBlank(map.get("song_name"))){
					map.put("song_name", temp.getSong());
					lang = lang.replaceFirst(temp.getSong(), "");
				}
				if (StringUtils.isNotBlank(temp.getAlbum())&&
						(lang.contains("专辑")||lang.contains("album"))&&lang.contains(temp.getAlbum())
						&&!StringUtils.isNotBlank(map.get("album_name"))){
					map.put("album_name", temp.getAlbum());
					lang = lang.replaceFirst(temp.getAlbum(), "");
				}
			}
		}else if(lang.contains("专辑")||lang.contains("album")) {
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getAlbum()) &&	(lang.contains("专辑") || lang.contains("album"))
						&& lang.contains(temp.getAlbum()) && !StringUtils.isNotBlank(map.get("album_name"))) {
					map.put("album_name", temp.getAlbum());
					lang = lang.replaceFirst(temp.getAlbum(), "");
				}
			}
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getSong()) &&
						lang.contains(temp.getSong())&& !StringUtils.isNotBlank(map.get("song_name"))) {
					map.put("song_name",temp.getSong());
					lang = lang.replaceFirst(temp.getSong(), "");
				}
				if (StringUtils.isNotBlank(temp.getArtist()) &&
						lang.contains(temp.getArtist())
						&& !StringUtils.isNotBlank(map.get("singer_name"))) {
					map.put("singer_name", temp.getArtist());
					lang = lang.replaceFirst(temp.getArtist(), "");
				}
			}
		}else{
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getSong()) &&lang.contains(temp.getSong())
						&& !StringUtils.isNotBlank(map.get("song_name"))) {
					map.put("song_name", temp.getSong());
					lang = lang.replaceFirst(temp.getSong(), "");
				}
			}
			for(MusicSemantic temp:musicSemanticList){
				if (StringUtils.isEmpty(lang)) {
					break;
				}
				if (StringUtils.isNotBlank(temp.getArtist()) &&lang.contains(temp.getArtist())
						&& !StringUtils.isNotBlank(map.get("singer_name"))) {
					map.put("singer_name", temp.getArtist());
					lang = lang.replaceFirst(temp.getArtist(), "");
				}
			}
		}
		return map;
	}

	// 喜马拉雅处理
	public List<XMLYSemantic> xmlyProcess(Client client, String lang)
			throws Exception {
		ArrayList<String> tokens = new ArrayList<String>();
		String prefix_dic_name = "prefix.dic";
		String suffix_dic_name = "suffix.dic";
		float[] score_array = new float[3];
		String template_name = "xmly.template";
		String type = "xmly_track";
		tokens = new ElasticUtils().preProcess(lang, score_array,
				prefix_dic_name, suffix_dic_name, useSmart);
		logger.info("Process Results:");
		for (int i = 0; i < tokens.size(); i++)
			logger.info(tokens.get(i) + "/");
		SearchResponse response = new ElasticUtils().search_xmly(client,
				IndexName, tokens, type, score_array, template_name);
		SearchHits hits = response.getHits();
		logger.info("the number of hits is :" + hits.getTotalHits());
		if (hits.getTotalHits() == 0) {
			return xmlys;
		}
		return filterXMLYResult(hits.getHits()[0], tokens);
	}

	private List<XMLYSemantic> filterXMLYResult(SearchHit hit,
			ArrayList<String> tokens) {
		String lang = filterXmly(convertTokens(tokens));
		XMLYSemantic xmly = new XMLYSemantic();
		/*String catagory = ObjectUtils.objToString(hit.getSource().get(
				"category_title"));
		if (StringUtils.isNotBlank(catagory) && judgeXmly(catagory, tokens)) {
			catagory = filterXmly(catagory);
			String cata = LCS(lang, catagory);
			if (StringUtils.isNotBlank(cata)) {
				xmly.setCatalog(catagory);
				lang = lang.replaceFirst(cata, "");
			}
		}
		String sub_catagory = ObjectUtils.objToString(hit.getSource().get(
				"sub_category"));
		if (StringUtils.isNotBlank(sub_catagory)) {
			sub_catagory = filterXmly(sub_catagory);
			String sub_cata = LCS(lang, sub_catagory);
			if (StringUtils.isNotBlank(sub_cata)) {
				xmly.setSubCatalog(sub_catagory);
				lang = lang.replaceFirst(sub_cata, "");
			}
		}
		String album = ObjectUtils.objToString(hit.getSource().get(
				"album_title"));
		if (StringUtils.isNotBlank(album) && judgeXmly(album, tokens)) {
			album = filterXmly(album);
			String album1 = LCS(lang, album);
			if (StringUtils.isNotBlank(album1)) {
				xmly.setAlbum(album);
				lang = lang.replaceFirst(album1, "");
			}
		}
		String track = ObjectUtils.objToString(hit.getSource().get(
				"track_title"));
		if (StringUtils.isNotBlank(track) || judgeXmly(track, tokens)) {
			track = filterXmly(track);
			String track1 = LCS(lang, track);
			if (StringUtils.isNotBlank(track1)) {
				xmly.setName(track);
			}
		}
		xmlys.add(xmly);*/
		return xmlys;
	}

	private List<MusicSemantic> filterXMResult1(Map<String,String> map, ArrayList tokens) {
		MusicSemantic semantic = new MusicSemantic();
		if (StringUtils.isNotBlank(map.get("singer_name"))) {
			semantic.setArtist(map.get("singer_name"));
		}
		if (StringUtils.isNotBlank(map.get("album_name"))) {
			semantic.setAlbum(map.get("album_name"));
		}
		if (StringUtils.isNotBlank(map.get("song_name"))) {
			semantic.setSong(map.get("song_name"));
		}
		musics.add(semantic);
		return musics;
	}

	//过滤喜马拉雅的结果返回值
	private String filterXmly(String string) {
		char[] array = {'\\', '、', '!', '！', '?', '？', ' ', ',', '，',
				'-', '_'};
		for (int i = 0; i < array.length; i++)
			string = string.replace(array[i], ' ');
		string = string.replaceAll("\\(.+?\\)","");
		string = string.replaceAll("\\（.+?\\）","");
		string = string.toLowerCase();
		return string;
	}
	
	private String filter(String str) {
		char[] array = {'\\', '、', '!', '！', '?', '？',  '（', '）', ',', '，',
				'-', '_'};
		for (int i = 0; i < array.length; i++)
			str = str.replace(array[i], ' ');
//		str = str.replace(" ", "");
		str = str.replaceAll("\\(...*\\)", "");
		str = str.replace("(", "");
		str = str.replace(")", "");
		str = str.toLowerCase();
		return str;
	}
	private String convertTokens(ArrayList<String> tokens) {
		String query = "";
		for (int i = 0; i < tokens.size(); i++){
			if(tokens.get(i).matches("[a-zA-Z0-9' ]+")){
				query +=  " " + tokens.get(i);
			}else{
				query += tokens.get(i);
			}
		}

		return query;
	}


	//区分英文语境下歌曲点播的准确率的问题
	private SearchResponse distinctSearchResponse(List<String> tokens,SearchResponse response,SearchResponse responseEn,SearchResponse smart) {
		String lang = "";
		for (String str : tokens) {
			lang = lang + str + " ";
		}

		String result = getSongFromHit(responseEn);
		String result1 = getSongFromHit(smart);
		if (SimilarityUtil.levenshtein(lang, result) > 0.9) {
			return responseEn;
		} else if (SimilarityUtil.levenshtein(lang,result1.toLowerCase()) > 0.9) {
			return smart;
		}else{
			return response;
		}
	}


	private String getSongFromHit(SearchResponse response) {
		SearchHits hits = response.getHits();
		String result = "";
		if(hits.getHits().length > 0){
			SearchHit hit = hits.getHits()[0];
			result = ObjectUtils.objToString(hit.getSource().get(
					"song_name"));
		}
		return result;

	}




}
