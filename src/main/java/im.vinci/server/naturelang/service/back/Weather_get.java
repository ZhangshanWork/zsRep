package im.vinci.server.naturelang.service.back;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


import im.vinci.server.naturelang.domain.Parameter;
import org.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.TextUnderstander;
import com.iflytek.cloud.speech.TextUnderstanderListener;
import com.iflytek.cloud.speech.UnderstanderResult;

import im.vinci.server.naturelang.domain.Response_weather;

public class Weather_get {
	//private Logger log = LoggerFactory.getLogger(this.getClass());
	public Weather_get(){
		init();
	}
	private final String APPID = "5682479d";
	private void init(){
		SpeechUtility.createUtility("appid=" + APPID);
	}

	public Response_weather get_weather(Parameter parameter){
		//log.info("weather get service");
		JSONObject temp_result = new JSONObject();
		Response_weather response = new Response_weather();
		String result = "";
		try {
			if(parameter == null){
				response.setRc(4);
				response.setRtext("输入是空");
				response.setText("");
				return response;
    			/*re_result.put("rc", 4);
    			re_result.put("text", "");
    			re_result.put("rtext", "输入是空");
				//log.info("message is null");
				return re_result.toString();*/
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
			if(city.equals("")){
				response.setRc(4);
				response.setRtext("没有地理位置信息");
				return response;
	    		/*re_result.put("rc", 4);
	    		re_result.put("text", query);
	    		re_result.put("rtext", "没有位置信息");
	    		return re_result.toString();*/
			}
			result =get_result(query);
			JSONObject jsonobject = new JSONObject(result);
			temp_result = jsonobject;
			while(jsonobject.getInt("rc")>0){
				if(jsonobject.getInt("rc")==1){
					result = get_result(city+jsonobject.getString("datetime")+"天气");
					jsonobject = new JSONObject(result);
					//re_result = jsonobject;
				}
				else if(jsonobject.getInt("rc")>1){
					String[] citys = city.split(" ");
					//for(int i=0;i<citys.length;i++){
					result = get_result(citys[citys.length-1]+jsonobject.getString("datetime")+"天气");
					jsonobject = new JSONObject(result);
					if(jsonobject.getInt("rc")==0){
						temp_result = jsonobject;
						break;
					}
					else if(jsonobject.getInt("rc")>0 && (citys.length-2>-1)){
						result = get_result(citys[citys.length-2]+jsonobject.getString("datetime")+"天气");
						jsonobject = new JSONObject(result);
						if(jsonobject.getInt("rc")==0){
							temp_result = jsonobject;
							break;
						}
						else if(jsonobject.getInt("rc")>0 && (citys.length-3>-1)){
							result = get_result(citys[citys.length-3]+jsonobject.getString("datetime")+"天气");
							jsonobject = new JSONObject(result);
							if(jsonobject.getInt("rc")==0){
								temp_result = jsonobject;
								break;
							}
							else{
								break;
							}
						}
						else{
							break;
						}
					}
					else{
						break;
					}
					//}
				}
				else {
					break;
				}
			}
			if(jsonobject.getInt("rc")!=0){
				//log.info("failed to get weather you want");
				response.setRtext("没有您要查询的天气");
				response.setRc(jsonobject.getInt("rc"));
				//re_result.put("rtext","没有您要查询的天气");
				//System.out.println("没有您要查询的天气");
			}
			else{
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
				response.getSemantic().getDatetime().setDate(temp_result.getJSONObject("semantic").getJSONObject("datetime").getString("date"));
				response.getSemantic().getDatetime().setType(temp_result.getJSONObject("semantic").getJSONObject("datetime").getString("type"));
				if(!temp_result.getJSONObject("semantic").getJSONObject("datetime").isNull("dateOrig")){
					response.getSemantic().getDatetime().setDateOrig(temp_result.getJSONObject("semantic").getJSONObject("datetime").getString("dateOrig"));
				}
				if(!temp_result.getJSONObject("semantic").getJSONObject("slots").isNull("airQuality")){
					response.getSemantic().getSlots().setAirQuality(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("airQuality"));
				}
				response.getSemantic().getSlots().setCity(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("city"));
				response.getSemantic().getSlots().setDate(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("date"));
				response.getSemantic().getSlots().setDateLong(temp_result.getJSONObject("semantic").getJSONObject("slots").getLong("dateLong"));
				response.getSemantic().getSlots().setLastUpdateTime(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("lastUpdateTime"));
				//response.getSemantic().getSlots().setProvince(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("province"));
				response.getSemantic().getSlots().setSourceName(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("sourceName"));
				response.getSemantic().getSlots().setTempRange(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("tempRange"));
				response.getSemantic().getSlots().setWeather(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("weather"));
				response.getSemantic().getSlots().setWind(temp_result.getJSONObject("semantic").getJSONObject("slots").getString("wind"));
				response.getSemantic().getSlots().setWindLevel(temp_result.getJSONObject("semantic").getJSONObject("slots").getInt("windLevel"));
			}
			response.setText(parameter.getQuery());
			//re_result.put("text", json.getString("query"));
			//System.out.println("the last result is:"+new JSONObject(response).toString());
			//System.out.println(response.getSemantic().getLocation().getAreaAddr());
		}
		catch(Exception e){
			e.printStackTrace();
			//log.info(e.getMessage());
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
			//log.info(e1.getMessage());
			e1.printStackTrace();
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
				System.out.println(result.getResultString());
				//log.info("Xunfei result is:"+result.toString());
				try {
					JSONObject json = new JSONObject(result.getResultString());
					if(json.get("rc").toString().equals("0") && "QUERY".equals(json.getString("operation")) && "weather".equals(json.getString("service")))//只有返回结果为openQAQ时处理
					{
						JSONObject semantic_t = new JSONObject();
						re_result.put("rc", 0);
						re_result.put("rtext", "");
						//re_result.put("text", msg);
						re_result.put("service", json.getString("service"));
						re_result.put("operation", json.getString("operation"));
						semantic_t.put("location", json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("location"));
						semantic_t.put("datetime", json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime"));
						String queryDate = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime").getString("date");
						if(queryDate.equals("CURRENT_DAY")){
							SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							Date d = new Date();
							queryDate = df.format(d);
						}
						JSONArray jsonarray = json.getJSONObject("data").getJSONArray("result");
						JSONObject jsonobject = jsonarray.getJSONObject(0);
						//String province = jsonobject.getString("province");
						String city = jsonobject.getString("city");
						for(int i=0;i<jsonarray.length();i++){
							if(queryDate.equals(jsonarray.getJSONObject(i).getString("date"))){
								jsonobject = jsonarray.getJSONObject(i);
								break;
							}
								/*if(province.equals(jsonarray.getJSONObject(i).getString("province"))){
									;
								}
								else{
									System.out.println("省份不一样");
									re_result.put("rtext", "省份不一样");
									break;
								}*/
							if(city.equals(jsonarray.getJSONObject(i).getString("city"))){
								;
							}
							else{
								System.out.println("城市不一样");
								re_result.put("rtext", "城市不一样");
								break;
							}
							if(i==jsonarray.length()-1){
								String date_start = jsonobject.getString("date");
								String date_s = date_start.split("-")[0]+"年"+date_start.split("-")[1].replace("0", "")+"月"+date_start.split("-")[2].replace("0", "")+"号";
								String date_end = jsonarray.getJSONObject(i).getString("date");
								String date_e = date_end.split("-")[0]+"年"+date_end.split("-")[1].replace("0", "")+"月"+date_end.split("-")[2].replace("0", "")+"号";
								re_result.put("rtext", "只能查询"+date_s+"到"+date_e+"的天气");
							}
						}
						semantic_t.put("slots",jsonobject);
						re_result.put("semantic", semantic_t);
						System.out.println(re_result);
						String answer = "";
						String date = queryDate.split("-")[0]+"年"+queryDate.split("-")[1].replace("0", "")+"月"+queryDate.split("-")[2].replace("0", "")+"日";
						String area = city;//province + city;
						String weather = jsonobject.getString("weather");
						String[] temp = jsonobject.getString("tempRange").split("~");
						String low_temp = "温度"+temp[0];
						String high_temp = "到"+temp[1];
						String wind = jsonobject.getString("wind").replace(" ", "");//+jsonobject.get("windLevel").toString()+"级";
						answer = date+","+area+","+weather+","+low_temp+high_temp+","+wind;
						//response.setText(answer);
						System.out.println(answer);
					}
						/*else if(json.get("rc").toString().equals("0") && "QUERY".equals(json.getString("operation")) && "pm25".equals(json.getString("service"))){
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
						}*/
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
					System.out.println("result is : "+re_result.toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//语义错误回调
			public void onError(SpeechError error) {
				//log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
				cd.countDown();
			}
		};

		mTextUnderstander.understandText(msg.replace(" ", ""), searchListener);
		cd.await(1, TimeUnit.SECONDS);
		//log.info("result is:"+re_result.toString());
		return re_result.toString();

	}

	private String get_city(String msg){
		String city="";
		try {
			msg = URLEncoder.encode(msg,"UTF-8");
		}
		catch(UnsupportedEncodingException e1){
			e1.printStackTrace();
			//log.info(e1.getMessage());
		}
		try{
			String url = "http://ltpapi.voicecloud.cn/analysis/?api_key=o1W4g5J1G3p7S895W8A9UqddqA5pGb9JugXPaWwJ&text="+msg+"&pattern=srl&format=json";
			System.out.println(url);
			URL realurl = new URL(url);
			URLConnection con = realurl.openConnection();
			con.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
			String line;
			String result="";
			while((line = in.readLine()) != null){
				result += line;
			}
			System.out.println(result);
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
				System.out.println(jarray.getJSONObject(i).toString());
			}
			System.out.println(jarray);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//log.info(e.getMessage());
		}
		System.out.println(city);
		System.out.println(city.split(" ").length);
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
			System.out.println(result);
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
			e.printStackTrace();
			//log.info(e.getMessage());
		}
		return city;
	}

}
