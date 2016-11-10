package im.vinci.server.naturelang.service.impl;

import com.taobao.api.DefaultTaobaoClient;
import com.taobao.api.request.AlibabaXiamiApiSearchCollectsGetRequest;
import com.taobao.api.response.AlibabaXiamiApiSearchCollectsGetResponse;
import im.vinci.server.naturelang.domain.MusicSemantic;
import im.vinci.server.naturelang.domain.XMLYSemantic;
import im.vinci.server.naturelang.listener.Context;
import im.vinci.server.naturelang.service.NatureLangService;
import im.vinci.server.naturelang.service.impl.process.ElasticHandler;
import im.vinci.server.naturelang.service.impl.process.ElasticProcess;
import im.vinci.server.search.domain.himalayas.GetHimalayaAlbumDetailResponse;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.elasticsearch.client.Client;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class NatureLangServiceImpl implements NatureLangService {

	@Resource(name = "esClient")
	private Client client;

	private static DefaultTaobaoClient tclient = new DefaultTaobaoClient("http://gw.api.taobao.com/router/rest"
			, "23064829", "29ed3de5990627239d0fdbddd3e94b51", "json", 500000, 500000);


	public List<MusicSemantic> getFinalResult(String str) throws Exception {
		List<MusicSemantic> musics = new ElasticProcess().natureLangProcess(client, str);
		return musics;
	}

	public List<MusicSemantic> getNationFinalResult(String str) throws Exception {
		List<MusicSemantic> musics = new ElasticProcess().nationLangProcess(client, str);
		return musics;
	}

	public List<XMLYSemantic> getXMLYFinalResult(String str) throws Exception {
		List<XMLYSemantic> xmlys = new ElasticProcess().xmlyProcess(client, str);
		return xmlys;
	}

	@Override
	public List<String> getSingersResult(String str) throws Exception {
		return new ElasticHandler().cut(str, true);
	}

	/**
	 * 获得语言文本中的歌手list,非歌手的名词被过滤掉
	 * @param lang
	 * @return
	 * @throws Exception
     */
	public List<String> getFilteredSingersResult(String lang) throws Exception {
		List<String> list = getSingersResult(lang);
		List<String> list1 = new ArrayList<String>();
		for (String e : list) {
			if (Context.IfSinger(e)) {
				list1.add(e);
			}
		}
		return list1;
	}

    @Override
    public GetHimalayaAlbumDetailResponse getAlbumById(String id) {
        GetHimalayaAlbumDetailResponse response = new GetHimalayaAlbumDetailResponse();
        response.setAlbum(Context.getOssHimalaya(id + ""));
        return response;
    }

	public ArrayList<Long> getCollectionsId(JSONArray collections){

		ArrayList<Long> collection_ids = new ArrayList<Long>();
		for(Object coll: collections){
			JSONObject jcoll = JSONObject.fromObject(coll);
			collection_ids.add(jcoll.getLong("list_id"));
		}
		return collection_ids;
	}

	public ArrayList<String> getCollectionsName(JSONArray collections){
		ArrayList<String> collection_name = new ArrayList<String>();
		for(Object coll: collections){
			JSONObject jcoll = JSONObject.fromObject(coll);
			collection_name.add(jcoll.getString("collect_name"));
		}
		return collection_name;
	}

	public JSONArray getCollections(String keyword){
		JSONArray collections=null;
		try{
			AlibabaXiamiApiSearchCollectsGetRequest req = new AlibabaXiamiApiSearchCollectsGetRequest();

			req.setPage(1L);
			req.setLimit(100L);
			req.setKey(keyword);
			
			AlibabaXiamiApiSearchCollectsGetResponse rsp = tclient.execute(req);
	
			//		System.out.println("collections by keyword:"+rsp.getBody());
			JSONObject jobj = JSONObject.fromObject(rsp.getBody());
	
			collections = jobj.getJSONObject("user_get_response").getJSONObject("data").getJSONArray("collects");
		}catch(Exception e){
			e.printStackTrace();
			getCollections(keyword);
		}
		return collections;
	}
}