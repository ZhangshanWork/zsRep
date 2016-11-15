package im.vinci.server.naturelang.service.back;



import com.iflytek.cloud.speech.*;
import im.vinci.server.naturelang.domain.Parameter;
import im.vinci.server.naturelang.domain.PMResponse;
import im.vinci.server.naturelang.utils.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PmBack {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	public PmBack(){
		init();
	}
	private final String APPID = "5682479d";
	private void init(){
		SpeechUtility.createUtility("appid=" + APPID);
	}

	public PMResponse getPM(Parameter parameter, String ip){
		log.info("weather get service");
		JSONObject temp_result = new JSONObject();
		PMResponse response = new PMResponse();
		String result = "";
		try {
			if(parameter==null){
				response.setRc(4);
				response.setRtext("输入是空");
				response.setText("");
				return response;
			}
			String query = parameter.getQuery();
			response.setText(query);
			//re_result.put("text", query);
			String city = get_city(query);
			if(city.equals("")){
				String lat_lon = parameter.getLocation().getLongitude()+","+parameter.getLocation().getLatitude();
				//System.out.println(lat_lon);
				city = get_city_lat(lat_lon);
				query = city + query;
				//System.out.println(city);
			}
			if (city.equals("") && StringUtils.isNoneBlank(ip)) {
				city = CommonUtils.GetAddressByIp(ip);
				query = city + query;
			}
			if(city.equals("")){
				response.setRc(4);
				response.setRtext("没有地理位置信息");
				return response;
			}
			result =get_result(query);
			//System.out.println("result is:"+result);
			JSONObject jsonobject = new JSONObject(result);
			temp_result = jsonobject;

			if(jsonobject.getInt("rc")!=0){
				log.info("failed to get weather you want");
				response.setRc(jsonobject.getInt("rc"));
				response.setRtext("没有您要查询的天气");
				//re_result.put("rtext","没有您要查询的天气");
				//System.out.println("没有您要查询的天气");
			}
			else{
				//System.out.println(temp_result.toString());
				response.setRc(temp_result.getInt("rc"));
				response.setService(temp_result.getString("service"));
				response.setOperation(temp_result.getString("operation"));
				response.getSemantic().getLocation().setCityAddr(temp_result.getJSONObject("semantic").getJSONObject("location").getString("cityAddr"));
				response.getSemantic().getLocation().setCity(temp_result.getJSONObject("semantic").getJSONObject("location").getString("city"));
				response.getSemantic().getLocation().setType(temp_result.getJSONObject("semantic").getJSONObject("location").getString("type"));
				if(!temp_result.getJSONObject("semantic").getJSONObject("location").isNull("areaAddr")){
					response.getSemantic().getLocation().setAreaAddr(temp_result.getJSONObject("semantic").getJSONObject("location").getString("areaAddr"));
				}
				if(!temp_result.getJSONObject("semantic").getJSONObject("location").isNull("area")){
					response.getSemantic().getLocation().setArea(temp_result.getJSONObject("semantic").getJSONObject("location").getString("area"));
				}
				if(!temp_result.getJSONObject("semantic").getJSONObject("location").isNull("provinceAddr")){
					response.getSemantic().getLocation().setProvinceAddr(temp_result.getJSONObject("semantic").getJSONObject("location").getString("provinceAddr"));
				}
				if(!temp_result.getJSONObject("semantic").getJSONObject("location").isNull("province")){
					response.getSemantic().getLocation().setProvince(temp_result.getJSONObject("semantic").getJSONObject("location").getString("province"));
				}
				response.getSemantic().getSlots().setAqi(temp_result.getJSONObject("semantic").getJSONObject("slots").getInt("aqi"));
				response.getSemantic().getSlots().setArea(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("area"));
				response.getSemantic().getSlots().setPm25(temp_result.getJSONObject("semantic").getJSONObject("slots").getInt("pm25"));
				response.getSemantic().getSlots().setPositionName(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("positionName"));
				response.getSemantic().getSlots().setPublishDateTime(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("publishDateTime"));
				response.getSemantic().getSlots().setPublishDateTimeLong(temp_result.getJSONObject("semantic").getJSONObject("slots").getLong("publishDateTimeLong"));
				response.getSemantic().getSlots().setQuality(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("quality"));
				response.getSemantic().getSlots().setSourceName(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("sourceName"));
				response.getSemantic().getSlots().setSubArea(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("subArea"));
			}
			response.setText(parameter.getQuery());
			//re_result.put("text", json.getString("query"));
		}
		catch(Exception e){
			//e.printStackTrace();
			log.info(e.getMessage());
		}
		//log.info("result is:"+re_result.toString());
		return response;
		//return re_result.toString();
	}

	private String get_result(String msg) throws InterruptedException{
		final JSONObject re_result = new JSONObject();

		try {
			re_result.put("text", msg);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			log.info(e1.getMessage());
			//e1.printStackTrace();
		}
		//创建文本语义理解对象
		TextUnderstander mTextUnderstander = TextUnderstander.createTextUnderstander();
		//final Response response = new Response();
		//final JSONObject re_result = new JSONObject();
		final CountDownLatch cd = new CountDownLatch(1);
		//final String[] retry = {""};
		//log.info("Xunfei service get");
		//初始化监听器
		TextUnderstanderListener searchListener = new TextUnderstanderListener(){
			//语义结果回调
			public void onResult(UnderstanderResult result){
				log.info("Xunfei result is:"+result.toString());
				try {
					JSONObject json = new JSONObject(result.getResultString());
					if(json.get("rc").toString().equals("0") && "QUERY".equals(json.getString("operation")) && "pm25".equals(json.getString("service"))){
						re_result.put("rc", 0);
						re_result.put("rtext", "");
						re_result.put("service", json.getString("service"));
						re_result.put("operation", json.getString("operation"));
						JSONObject semantic_t = new JSONObject();
						semantic_t.put("location", json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("location"));
						//semantic_t.put("datetime", json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime"));
						JSONArray jsonarray = json.getJSONObject("data").getJSONArray("result");
						semantic_t.put("slots", jsonarray.getJSONObject(0));
						String area=jsonarray.getJSONObject(0).getString("area");
						if(jsonarray.length()>1){
							for(int i=0;i<jsonarray.length();i++){
								if(!area.equals(jsonarray.getJSONObject(i).getString("area"))){
									re_result.put("rtext", "城市不一样");
									break;
								}
							}
							if(re_result.getString("rtext").equals("")){
								for(int i=0;i<jsonarray.length();i++){
									if(jsonarray.getJSONObject(i).getString("subArea").equals(jsonarray.getJSONObject(i).getString("area"))){
										semantic_t.put("slots", jsonarray.getJSONObject(i));
									}
								}
							}
						}
						re_result.put("semantic", semantic_t);
					}
					else if(json.get("rc").toString().equals("0") && !"weather".equals(json.getString("service"))){
						re_result.put("rc", 1);
						//JSONObject j = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime");
						re_result.put("datetime", "今天");//j.getString("date").equals("CURRENT_DAY")?"今天":j.getString("dateOrig"));
					}
					else if(json.get("rc").toString().equals("4")){
						re_result.put("rc", 2);
						//JSONObject j = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime");
						//re_result.put("date", j.getString("date").equals("CURRENT_DAY")?"今天":j.getString("dateOrig"));
						re_result.put("datetime","今天");
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			//语义错误回调
			public void onError(SpeechError error) {
				log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
				cd.countDown();
			}
		};

		mTextUnderstander.understandText(msg.replace(" ", ""), searchListener);
		cd.await(1, TimeUnit.SECONDS);
		log.info("result is:"+re_result.toString());
		return re_result.toString();

	}

	private String get_city(String msg){
		String city="";
		try {
			msg = URLEncoder.encode(msg,"UTF-8");
		}
		catch(UnsupportedEncodingException e1){
			//e1.printStackTrace();
			log.info(e1.getMessage());
		}
		try{
			String url = "http://ltpapi.voicecloud.cn/analysis/?api_key=o1W4g5J1G3p7S895W8A9UqddqA5pGb9JugXPaWwJ&text="+msg+"&pattern=srl&format=json";
			URL realurl = new URL(url);
			URLConnection con = realurl.openConnection();
			con.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			String line;
			String result="";
			while((line = in.readLine()) != null){
				result += line;
			}
			//JSONObject json = new JSONObject(result);
			JSONArray jarray = new JSONArray(result);
			jarray = (JSONArray) jarray.get(0);
			jarray = (JSONArray) jarray.get(0);
			for(int i=0;i<jarray.length();i++){
				if(jarray.getJSONObject(i).getString("pos").equals("ns")){
					city = city + jarray.getJSONObject(i).getString("cont")+" ";
				}
				else if(jarray.getJSONObject(i).getString("pos").equals("nt")){
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			log.info(e.getMessage());
		}
		return city;
	}

	private String get_city_lat(String msg){
		String city = "";
		String url = "http://restapi.amap.com/v3/geocode/regeo?key=2fc9168c57e034f73dca05ed44091b2b&location="+msg+"&poitype=&radius=&extensions=all&batch=false&roadlevel=1&output=json";
		try{
			URL realurl = new URL(url);
			URLConnection con = realurl.openConnection();
			con.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			String line;
			String result="";
			while((line = in.readLine())!=null){
				result += line;
			}
			JSONObject json = new JSONObject(result);
			json = json.getJSONObject("regeocode").getJSONObject("addressComponent");
			if(!json.get("province").toString().contains("[")){
				if(!json.get("city").toString().contains("[")){
					city = json.get("province")+" "+json.get("city");
				}
				else if(!json.get("district").toString().contains("[")){
					city = json.get("province")+" "+json.get("district");
				}
				else{
					city = json.get("province").toString();
				}
			}
			//city = json.get("province")+" "+json.get("city");
		}
		catch(Exception e){
			//e.printStackTrace();
			log.info(e.getMessage());
		}
		return city;
	}

}
