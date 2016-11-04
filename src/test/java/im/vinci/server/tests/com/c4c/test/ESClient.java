package im.vinci.server.tests.com.c4c.test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

//import org.elasticsearch.index.mapper.Mapper;
//import org.elasticsearch.index.mapper.object.ObjectMapper;

public class ESClient {

	private Client client;
	/**
	 * 
	 */
	@Before
	public void initESClient() {
		//Settings settings = Settings.settingsBuilder().put("cluster.name","elasticsearch").build();  
		Settings settings = Settings.settingsBuilder().put("cluster.name","vinci").build();
		try {
			//client = TransportClient.builder().settings(settings).build()
			//		.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.1"),9300));
					//.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("127.0.0.2"),9300));
			client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("101.200.159.42"),9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("111");
	}

	@After
	public void closeESClient() {
		client.close();
		System.out.println("222");
	}
	
	public static String ReadFile(String path){
	    File file = new File(path);
	    BufferedReader reader = null;
	    String laststr = "";
	    try {
	     //System.out.println("以行为单位读取文件内容，一次读一整行：");
	     reader = new BufferedReader(new FileReader(file));
	     String tempString = null;
	     //一次读入一行，直到读入null为文件结束
	     while ((tempString = reader.readLine()) != null) {
	      //显示行号
//	      System.out.println("line " line ": " tempString);
	      laststr = laststr + tempString;
	     }
	     reader.close();
	    } catch (IOException e) {
	     e.printStackTrace();
	    } finally {
	     if (reader != null) {
	      try {
	       reader.close();
	      } catch (IOException e1) {
	      }
	     }
	    }
	    return laststr;
	}

	public static void main(String args[]) throws JsonGenerationException, JsonMappingException, IOException, JSONException{
		ESClient esc = new ESClient();
		esc.initESClient();

//read json file
		
        String fullFileName = "/Users/qinghanxue/Desktop/vinci/oss-0813.json";
        
        File file = new File(fullFileName);
        Scanner scanner = null;
        StringBuilder buffer = new StringBuilder();
        try {
            scanner = new Scanner(file, "utf-8");
            while (scanner.hasNextLine()) {
                buffer.append(scanner.nextLine());
            }
 
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block  
 
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
        
        JSONArray jsonArray=null;
        jsonArray = new JSONArray(buffer.toString());  //jsonArray shows the array for the input data
        int leng = jsonArray.length();
        //System.out.println(jsonArray.get(0).toString());
        System.out.println(leng);

        int num=14081484;
        for (int i=0;i<jsonArray.length();i++){
        	
        	SongInput content = new SongInput();
			content.setsinger_name(jsonArray.getJSONObject(i).getString("artist"));
			if (jsonArray.getJSONObject(i).getString("artist_id").equals("null")){
				content.setsinger_id(0);
			}
			else{
				content.setsinger_id(Long.parseLong(jsonArray.getJSONObject(i).getString("artist_id")));
			}
			content.setsinger_eng_name(jsonArray.getJSONObject(i).getString("artist_sub_title"));
			content.setsong_name(jsonArray.getJSONObject(i).getString("song_name"));
			content.setsong_style("");
			content.setalbum_name(jsonArray.getJSONObject(i).getString("album_name"));
			//content.setsinger_id(Long.parseLong(jsonArray.getJSONObject(i).getString("artist_id")));
			content.setsong_id(Long.parseLong(jsonArray.getJSONObject(i).getString("song_id")));
			if (jsonArray.getJSONObject(i).getString("album_id").equals("null")){
				content.setalbum_id(0);
			}
			else{
				content.setalbum_id(Long.parseLong(jsonArray.getJSONObject(i).getString("album_id")));
			}
			if (jsonArray.getJSONObject(i).getString("play_counts").equals("null")){
				content.setplay_count(0);
			}
			else{
				content.setplay_count(Long.parseLong(jsonArray.getJSONObject(i).getString("play_counts")));
			}
			content.setoriginal_id(num);
			content.settype_id(0);
			//content.setplay_count(Long.parseLong(jsonArray.getJSONObject(i).getString("play_counts")));
			content.setlength(0);
			content.setsong_source(jsonArray.getJSONObject(i).getString("song_type"));
			content.setalbum_des("");

			esc.insert(content,num);
			num=num+1;
        	
        }
      
        
/*//insert data
		try {

            Settings settings = Settings.settingsBuilder()
                    .put("cluster.name", "elasticsearch").build();// cluster.name在elasticsearch.yml中配置

            Client client = TransportClient.builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(
                            InetAddress.getByName("127.0.0.1"), 9300));

            File article = new File("/Users/qinghanxue/Desktop/vinci/vinci-change-api/merge/supplement_database/bulk_songs.txt");
            //File article = new File("/Users/qinghanxue/Desktop/vinci/vinci-change-api/merge/oss_refine.txt");
            FileReader fr=new FileReader(article);
            BufferedReader bfr=new BufferedReader(fr);
            String line=null;
            BulkRequestBuilder bulkRequest=client.prepareBulk();
            int count=0;
            int id=0;
            while((line=bfr.readLine())!=null){
                bulkRequest.add(client.prepareIndex("all","musicv1",Integer.toString(id)).setSource(line));
                if (count%10000==0) {
                    bulkRequest.execute().actionGet();
                    bulkRequest=client.prepareBulk();
                }
                count++;
                id=id+1;
                //System.out.println(line);
            }
            bulkRequest.execute().actionGet();
            
            bfr.close();
            fr.close();
            System.out.println(count);
            System.out.println("插入完毕");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        System.out.println("插入完毕");
		
	}

	
		public void insert(SongInput content, int num) throws JsonGenerationException, JsonMappingException, IOException{
/*	        
			SongInput content = new SongInput();
			content.setsinger_name("a");
			content.setsong_name("b");
			content.setsong_style("c");
			content.setalbum_name("d");
			content.setsinger_id(1);
			content.setsong_id(2);
			content.setalbum_id(3);
			content.setoriginal_id(4);
			content.settype_id(5);
			content.setplay_count(6);
			content.setlength(7);
*/
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonValue = objectMapper.writeValueAsString(content);
			//System.out.println(jsonValue);
			IndexResponse response = client.prepareIndex("xiami_music_merge", "musicv1", Integer.toString(num))
			        .setSource(jsonValue)

			        .execute()
			        .actionGet();
		}
	
	/**
	 */
	private void createIndex() {
		// TODO Auto-generated method stub
		for(int i=0; i<10; i++){
			String id = "id"+i;
			String title = "this is title" + i;
			client.prepareIndex("blog", "post").setSource(getBuilderJson(id, title)).execute().actionGet();
			//System.out.println(i);
		}
		System.out.println("444");
	}
	
	private String getBuilderJson(String id,String title){
		String json = "";
		try {
			XContentBuilder contentBuilder = XContentFactory.jsonBuilder().startObject();
			contentBuilder.field("id",id);
			contentBuilder.field("title",title);
			json = contentBuilder.endObject().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	
	/**
	 */
	public void getIndex() {
		GetResponse res = client.prepareGet().setIndex("vinci")
				.setType("type1").setId("AVJjRJVqW-UsQoTouwCF").execute().actionGet();
				//.setType("xiami_data_new").setFields("song_name","海盗").execute().actionGet();
		System.out.println(res.getSource());
	}
	/**
	 */
	public void search(String singer, String song, String album){
		SearchRequestBuilder searchRequestBuilder = client.prepareSearch("vinci");
		searchRequestBuilder.setTypes("type1");
		//1.SearchType.DFS_QUERY_THEN_FETCH = ...
		// 2.SearchType.SCAN = ...
		// 3.SearchType.COUNT = ...
		searchRequestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//		searchRequestBuilder.setQuery(QueryBuilders.termQuery("song_name.origin", song));
		QueryStringQueryBuilder queryString = new QueryStringQueryBuilder(song);
		queryString.field("song_name.original");
//		FilterBuilder termsFilterBuilder = FilterBuilders.andFilter(FilterBuilders.termFilter("singer_name.origin",singer));
//		QueryStringQueryBuilder queryString2 = new QueryStringQueryBuilder(singer);
//		queryString1.field("singer_name.chinese");
//		org.elasticsearch.index.query.QueryBuilder queryString = QueryBuilders.queryString("song_name.origin", song);				.must(QueryBuilders.termQuery("singer_name.origin", singer));
		searchRequestBuilder.setQuery(queryString);
//		searchRequestBuilder.setFilter(termsFilterBuilder);
		searchRequestBuilder.setFrom(0);
		searchRequestBuilder.setSize(10);  //To make sure that all the related results will be returned
		searchRequestBuilder.setExplain(true);
		SearchResponse response = searchRequestBuilder.execute().actionGet();
//		System.out.println(response);
		
		SearchHits searchHits = response.getHits();
		SearchHit[] hits = searchHits.getHits();
		//System.out.println(hits.length);
		int count=0;
		for (int i = 0; i < hits.length; i++) {
			SearchHit hit = hits[i];
			String lat = hit.getSource().get("singer_id").toString();
			if(lat.equals("54017")){
				count = count + 1;
			}
			Map result = hit.getSource();
			System.out.println(result);
			//break;
		}
		System.out.println("555");
		System.out.println(hits.length);
		System.out.println(count);
	}
	
	private QueryStringQueryBuilder filteredQuery(
			QueryStringQueryBuilder field, QueryStringQueryBuilder field2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 */
	public void get(){
		GetResponse response = client.prepareGet("vinci", "type1", "海盗船")
				.execute().actionGet();
		//GetResponse response = client.prepareGet("blog", "post", "AVJjRJVqW-UsQoTouwCF")
		//		.setOperationThreaded(false).execute().actionGet();
		//Map headers = (Map) response.getHeaders();
		Set<String> headers = response.getHeaders();
		System.out.println(headers);
		boolean exists = response.isExists();
		System.out.println(exists);
		String sourceString = response.getSourceAsString();
		System.out.println(sourceString);
		String id = response.getId();
		System.out.println(id);
		boolean sourceEmpty = response.isSourceEmpty();
		System.out.println(sourceEmpty);
	}
	/**
	 */
	public void delete(){
		DeleteResponse response = client.prepareDelete("vinci", "type1", "AVJjRJVqW-UsQoTouwCF")
				.execute().actionGet();
		//GetResponse response = client.prepareDelete("blog", "post", "AVJjRJVqW-UsQoTouwCF")
		//		.setOperationThreaded(false).execute().actionGet();
		boolean isFound = response.isFound();
		System.out.println(isFound);
	}
}
