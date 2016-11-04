package im.vinci.server.naturelang.service.impl.process;

import im.vinci.server.naturelang.listener.Context;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TemplateQueryBuilder;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class ElasticUtils {

	//用来预处理，包含分词，去除前缀，去除后缀等,返回结果为前缀后缀所代表的不同域的分值
	public ArrayList<String> preProcess(String query,float[] score_array,String prefix_dic_name,String suffix_dic_name,boolean useSmart) throws Exception
	{
		ArrayList<String> tokens = new ArrayList<String>();
		ElasticHandler handler = new ElasticHandler();
		tokens = handler.cut(query,useSmart);
		int field_num = 3;
		float[] score_t = new float[field_num];
		//handler.remove_prefix(tokens, prefix_dic_name,score_array);
		//handler.remove_suffix(tokens, suffix_dic_name,score_t);
		for(int i = 0; i < field_num; i ++)
			score_array[i] *= score_t[i];
		return tokens;
	}
	/*用来进行查询，并返回结果
	 * String Indices，查询的索引名
	 * ArrayList<String> tokens 分词返回的tokens
	 * float[] score_array 根据前后缀得到的不同检索域的分值信息
	 * String template_name 检索使用的模板名称
	 */
	public SearchResponse search(Client esClient,String IndexName,ArrayList<String> tokens,float[] score_array,String template_name)
	throws Exception
	{
		//设置默认值
		score_array[0]= 16.0f;
		score_array[1] = 8.0f;
		score_array[2] = 32.0f;
		score_array[3] = 4096.0f;
		//读取查询模板，然后设置参数查询
		BufferedReader bodyReader = null;
		try {
			bodyReader = new BufferedReader(new InputStreamReader(new ClassPathResource("nlp/"+template_name).getInputStream(), "utf8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new UnsupportedEncodingException();
		}
		String line = null;
		StringBuilder strBuffer = new StringBuilder();
		try {
			while ((line = bodyReader.readLine()) != null) {
				strBuffer.append(line);
				strBuffer.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new IOException();
		}

		String query = "";
		String temp = "";
		for(int i = 0; i < tokens.size(); i ++){
			if(tokens.get(i).matches("[a-zA-Z0-9 ]+")){
				temp += " " + tokens.get(i);
			}else{
				temp += tokens.get(i);
			}
		}

		if (StringUtils.isNotBlank(temp) && temp.matches("[a-zA-Z0-9 ]+")) {
			for(int i = 0; i < tokens.size(); i ++)
				query += tokens.get(i) + " ";
		}else{
			query = temp;
		}
		Map<String, Object> search_params = new HashMap<>();

        
        if(tokens.size() == 1 || Context.IfSinger(query))
        {
			if(Context.IfSinger(tokens.get(0))){
				score_array[0] =  1024.0f;
			}
        	search_params.put("field_type", "best_fields");
        }
        else
        {
			search_params.put("field_type", "cross_fields");
        	score_array[2] = 1024.0f;
        	score_array[0] = 128.0f;
        	score_array[1] = 64.0f;
			score_array[3] = 4096.0f;
        	if(tokens.contains("专辑"))
        	{
				query = query.replace("专辑","");
				score_array[0] = 128.0f;
            	score_array[2] = 128.0f;
            	score_array[1] = 4096.0f;
				score_array[3] = 1.0f;
        	}//判定是否出现歌手信息
			else if ((!query.matches("[a-zA-Z ]+"))&&StringUtils.isNotBlank(Context.getSingerInlang(query))) {
				//出现歌手信息，重新写入query
				String singer = Context.getSingerInlang(query);
				query = query.replaceFirst(singer,"").trim();
				if (query.startsWith("的")||query.startsWith("唱的")) {
					query = query.replaceFirst("唱的","").replaceFirst("的","");
				}
				query = singer + "  " + query;
				score_array[2] = 1024.0f;
				score_array[0] = 4096.0f;
				score_array[1] = 64.0f;
				score_array[3] = 1.0f;
			}else{
				score_array[2] = 4096.0f;
				score_array[3] = 4096.0f;
				search_params.put("field_type", "best_fields");
			}

        }
		query = query.replace("_", " ").replace("-", " ");
		if(query.matches("[a-zA-Z0-9 ]+[的]{0,1}[a-zA-Z0-9 ]+[的歌]{0,1}")){
			query = query.replace("的", " ");
		}

		search_params.put("query", query);
        /*search_params.put("singers_type", "singer_name.smart^"+score_array[0]);
        search_params.put("song_name_type", "song_name.smart^"+score_array[2]);
        search_params.put("album_name_type", "album_name.smart^"+score_array[1]);
        search_params.put("context_type", "keyword.smart^"+score_array[3]);*/
		search_params.put("singers_type", "singer_name.smart");
		search_params.put("song_name_type", "song_name.smart");
		search_params.put("album_name_type", "album_name.smart");
		search_params.put("context_type", "keyword.smart");
        TemplateQueryBuilder qb = QueryBuilders.templateQuery(strBuffer.toString(), search_params);
        
        SearchResponse response = esClient.prepareSearch(IndexName)
	               .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	               .setQuery(qb)
				   .setSize(100)
	               .execute().actionGet();
		return response;
	}


	public SearchResponse searchForSongEn(Client esClient, String IndexName, ArrayList<String> tokens, String template_name,String type) throws Exception {
		BufferedReader bodyReader = null;

		try {
			bodyReader = new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/" + template_name)).getInputStream(), "utf8"));
		} catch (UnsupportedEncodingException var15) {
			throw new UnsupportedEncodingException();
		}

		String line = null;
		StringBuilder strBuffer = new StringBuilder();
		while((line = bodyReader.readLine()) != null) {
			strBuffer.append(line);
			strBuffer.append("\n");
		}

		String query = "";
		String temp = "";

		int search_params;
		for(search_params = 0; search_params < tokens.size(); ++search_params) {
			temp = temp + (String)tokens.get(search_params);
		}

		if(StringUtils.isNotBlank(temp) && (temp.matches("[a-zA-Z ]+")||ifHighCase(tokens))) {
			for(search_params = 0; search_params < tokens.size(); ++search_params) {
				query = query + (String)tokens.get(search_params) + " ";
			}
		} else {
			query = temp;
		}

		HashMap var17 = new HashMap();
		query = query.trim();

		String enLang = "";
		if(query.matches("[a-zA-Z_ ]+")) {//如果是英文的，则按照英文处理
			enLang = query.replace(" ", "_").replace("\'", "_");
			enLang = Context.filterNationPref(enLang);
			query = enLang.replace("_", " ");
		}
		var17.put("query", query);
		var17.put("field_type", "best_fields");
		var17.put("singers_type", "singer_name.origin^1.0");
		var17.put("album_name_type", "album_name.origin^1.0");
		var17.put("song_name_type", "song_name."+type+"^1024.0");

		var17.put("context_type", "keyword.origin^1.0");
		TemplateQueryBuilder qb = QueryBuilders.templateQuery(strBuffer.toString(), var17);
		SearchResponse response = (SearchResponse)esClient.prepareSearch(new String[]{IndexName})
				//.setTypes("musicv1")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(qb)
				.execute()
				.actionGet();
		return response;
	}


	//喜马拉雅搜索

	public SearchResponse search_xmly(Client client,String IndexName,ArrayList<String> tokens,String type,float[] score_array,String template_name) throws Exception
	{
		//读取查询模板，然后设置参数查询
		BufferedReader bodyReader = null;
		bodyReader = new BufferedReader(new InputStreamReader(new ClassPathResource("nlp/"+template_name).getInputStream(), "utf8"));
		String line = null;
		StringBuilder strBuffer = new StringBuilder();
		try {
			while ((line = bodyReader.readLine()) != null) {
				strBuffer.append(line);
				strBuffer.append("\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		String query = "";
		for(int i = 0; i < tokens.size(); i ++)
			query+=tokens.get(i);			
				
		Map<String, Object> search_params = new HashMap<>();
        search_params.put("query", query);
        
        search_params.put("field_type", "most_fields");
        
        search_params.put("category_type", "category_title.smart^16");
        search_params.put("sub_category_type", "sub_category.smart^8");
        search_params.put("album_type", "album_title.smart^4");
        search_params.put("track_type", "track_title.smart^2");
        
        //System.out.println(search_params);
        
        TemplateQueryBuilder qb = QueryBuilders.templateQuery(strBuffer.toString(), search_params);
        
        SearchResponse response = client.prepareSearch(IndexName).setTypes(type)
	               .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	               .setQuery(qb).setSize(10)
	               .execute().actionGet();
		return response;
	}




	//判定句子中是否存在英文
	private boolean ifHighCase(List<String> list) {
		boolean flag = false;
		for (String temp : list) {
			if(temp.matches("[a-zA-Z ]+")){
				flag = true;
				continue;
			}
		}
		return flag;
	}
}
