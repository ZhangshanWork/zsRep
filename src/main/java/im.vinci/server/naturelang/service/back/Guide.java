package im.vinci.server.naturelang.service.back;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.TextUnderstander;
import com.iflytek.cloud.speech.TextUnderstanderListener;
import com.iflytek.cloud.speech.UnderstanderResult;

import im.vinci.server.naturelang.domain.Parameter;
import im.vinci.server.naturelang.domain.Response_guide;
import im.vinci.server.naturelang.domain.Xunfei_guide_result;


public class Guide {
	private Response_guide rg = new Response_guide();
	public Response_guide guide(Parameter msg){
		//String str = "从北京王府井大街到北京海淀区中央财经大学";
		//String m = "我要到中央财经大学";
		//msg = "{\"query\":\""+m+"\",\"location\":{\"latitude\" : 39.9498, \"longitude\": 116.4166 }}";
		JSONObject json;
		rg.setService("map");
		rg.setOperation("ROUTE");
		try {
			String str = msg.getQuery();
			rg.setText(str);
			String lat_lon = msg.getLocation().getLongitude()+""+msg.getLocation().getLatitude();
//			System.out.println(lat_lon);
			Xunfei_guide_result xgr = get_xunfei(str);
//			System.out.println(new JSONObject(xgr));
			if(xgr.getStart_current() && xgr.getStartPOI() == null){
				//直接用定位的经纬度
				rg.getSemantic().getSlots().setGuide(true);
				System.out.println("从定位地点出发");
				//start = lat_lon;
			}
			else if(xgr.getStart_current() && xgr.getStartPOI() != null){
				//根据传入经纬度获取城市信息
				Gaode gaode = get_city(lat_lon);
				String province = gaode.getProvince()!=null?gaode.getProvince():"";
				rg.getSemantic().getSlots().getStartLoc().setProvince(province);
				String city = gaode.getCity()!=null?gaode.getCity():"";
				rg.getSemantic().getSlots().getStartLoc().setCity(city);
				String district = gaode.getDistrict()!=null?gaode.getDistrict():"";
				rg.getSemantic().getSlots().getStartLoc().setArea(district);
				if(gaode.getCitycode()!=null || !gaode.getCitycode().equals("")){
					rg.getSemantic().getSlots().getStartLoc().setCitycode(gaode.getCitycode());
				}
				String location = province + city + district;
				location = location + xgr.getStartPOI();
				//System.out.println("location is :"+location);
				rg.getSemantic().getSlots().getStartLoc().setLocation(location);
				//start = gp.get_lat(location);
			}
			else if(!xgr.getStart_current() && xgr.getStartPOI() != null){
				//获取经纬度信息
				rg.getSemantic().getSlots().getStartLoc().setLocation(xgr.getStartLoc());
				//start = gp.get_lat(xgr.getStartLoc());
			}
			else if(!xgr.getStart_current() && xgr.getStartPOI() == null){
				rg.setRtext("请说出具体出发地点");
				//System.out.println("请说出具体出发地点");
			}
			if(xgr.getEndPOI() == null){
				rg.setRtext("请说出具体目的地点");
				//System.out.println("请说出具体目的地");
			}
			else if(xgr.getEnd_current()){
				//根据传入经纬度获取城市信息
				Gaode gaode = get_city(lat_lon);
				String province = gaode.getProvince()!=null?gaode.getProvince():"";
				rg.getSemantic().getSlots().getEndLoc().setProvince(province);
				String city = gaode.getCity()!=null?gaode.getCity():"";
				rg.getSemantic().getSlots().getEndLoc().setCity(city);
				String district = gaode.getDistrict()!=null?gaode.getDistrict():"";
				rg.getSemantic().getSlots().getEndLoc().setArea(district);
				if(gaode.getCitycode()!=null || !gaode.getCitycode().equals("")){
					rg.getSemantic().getSlots().getEndLoc().setCitycode(gaode.getCitycode());
				}
				String location = province + city + district;//gp.get_city(lat_lon);
				location = location + xgr.getEndPOI();
				rg.getSemantic().getSlots().getEndLoc().setLocation(location);
				//end = gp.get_lat(location);
			}
			else{
				rg.getSemantic().getSlots().getEndLoc().setLocation(xgr.getEndLoc());
				//end = gp.get_lat(xgr.getEndLoc());
			}
			if(rg.getSemantic().getSlots().getStartLoc().getCitycode()==null || rg.getSemantic().getSlots().getStartLoc().getCitycode().equals("")){
				if(rg.getSemantic().getSlots().getStartLoc().getLocation()!=null && !rg.getSemantic().getSlots().getStartLoc().getLocation().equals("")){
					String citycode = get_citycode(rg.getSemantic().getSlots().getStartLoc().getLocation());
					rg.getSemantic().getSlots().getStartLoc().setCitycode(citycode);
				}
			}
			if(rg.getSemantic().getSlots().getEndLoc().getCitycode()==null || rg.getSemantic().getSlots().getEndLoc().getCitycode().equals("")){
				if(rg.getSemantic().getSlots().getEndLoc().getLocation()!=null && !rg.getSemantic().getSlots().getEndLoc().getLocation().equals("")){
					String citycode = get_citycode(rg.getSemantic().getSlots().getEndLoc().getLocation());
					rg.getSemantic().getSlots().getEndLoc().setCitycode(citycode);
				}
			}
//			System.out.println(rg.getSemantic().getSlots().getStartLoc().getLocation());
//			System.out.println(rg.getSemantic().getSlots().getEndLoc().getLocation());
//			System.out.println(new JSONObject(rg));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rg;
	}
	//获取讯飞地图导航服务的结果
	private Xunfei_guide_result get_xunfei(String msg){
		final CountDownLatch cd = new CountDownLatch(1);
		final Xunfei_guide_result xgr = new Xunfei_guide_result();
		//创建文本语义理解对象
		TextUnderstander mTextUnderstander = TextUnderstander.createTextUnderstander();
		//初始化监听器
		TextUnderstanderListener searchListener = new TextUnderstanderListener(){
			//语义结果回调
			public void onResult(UnderstanderResult result){
				try {
					if(!result.getResultString().isEmpty()){
						JSONObject json = new JSONObject(result.getResultString());
						if(json.getInt("rc")==0 && !json.isNull("service") && json.getString("service").equals("map") && !json.isNull("operation") && json.getString("operation").equals("ROUTE")){
							if(!json.isNull("semantic") && !json.getJSONObject("semantic").isNull("slots") && !json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("city").equals("CURRENT_CITY")){
								String address = "";
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").isNull("province")){
									String province = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("province");
									address += province;
									rg.getSemantic().getSlots().getStartLoc().setProvince(province);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").isNull("city")){
									String city = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("city");
									address += city;
									rg.getSemantic().getSlots().getStartLoc().setCity(city);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").isNull("area")){
									String area = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("area");
									address += area;
									rg.getSemantic().getSlots().getStartLoc().setArea(area);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").isNull("poi")){
									String poi = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("poi");
									if(!poi.equals("CURRENT_POI")){
										address += poi;
										xgr.setStartPOI(poi);
										rg.getSemantic().getSlots().getStartLoc().setPoi(poi);
									}
								}
								//System.out.println("startLoc is:"+address);
								xgr.setStartLoc(address);
							}
							else if(json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("city").equals("CURRENT_CITY")){
								xgr.setStart_current(true);
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").isNull("poi")){
									String poi = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("startLoc").getString("poi");
									if(!poi.equals("CURRENT_POI")){
										rg.getSemantic().getSlots().getStartLoc().setPoi(poi);
										xgr.setStartPOI(poi);
									}
								}
							}
							if(!json.isNull("semantic") && !json.getJSONObject("semantic").isNull("slots") && !json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("city").equals("CURRENT_CITY")){
								String address = "";
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").isNull("province")){
									String province = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("province");
									address += province;
									rg.getSemantic().getSlots().getEndLoc().setProvince(province);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").isNull("city")){
									String city = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("city");
									address += city;
									rg.getSemantic().getSlots().getEndLoc().setCity(city);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").isNull("area")){
									String area = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("area");
									address += area;
									rg.getSemantic().getSlots().getEndLoc().setArea(area);
								}
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").isNull("poi")){
									String poi = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("poi");
									address += poi;
									xgr.setEndPOI(poi);
									rg.getSemantic().getSlots().getEndLoc().setPoi(poi);
								}
								xgr.setEndLoc(address);
								//System.out.println("endLoc is:"+address);
							}
							else if(json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("city").equals("CURRENT_CITY")){
								xgr.setEnd_current(true);
								if(!json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").isNull("poi")){
									String poi = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("endLoc").getString("poi");
									xgr.setEndPOI(poi);
									rg.getSemantic().getSlots().getEndLoc().setPoi(poi);
								}
							}
						}
						else{
							//System.out.println("不是导航服务");
							rg.setRc(4);
						}
						//System.out.println(json.toString());
					}
					else{
						rg.setRc(4);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				cd.countDown();
			}
			//语义错误回调
			public void onError(SpeechError error) {
				//log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
				cd.countDown();
				rg.setRc(4);
			}
		};
		//开始语义理解
		//String str = "从合肥到北京";
		mTextUnderstander.understandText(msg, searchListener);
		try {
			cd.await(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xgr;
	}

	//根据地理位置信息获取经纬度信息
	private String get_lat(String address){
		String lat = "";
		String key = "2fc9168c57e034f73dca05ed44091b2b";
		//System.out.println(address);
		try {
			address = URLEncoder.encode(address,"UTF-8");
		}
		catch(UnsupportedEncodingException e1){
			e1.printStackTrace();
			//log.info(e1.getMessage());
		}
		String url = "http://restapi.amap.com/v3/geocode/geo?key="+key+"&address="+address;
		//System.out.println(url);
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
			//System.out.println(result);
			if(!result.equals("")){
				JSONObject json = new JSONObject(result);
				if(json.getString("status").equals("1") && !json.isNull("geocodes")){
					if(!json.getJSONArray("geocodes").getJSONObject(0).isNull("location")){
						lat = json.getJSONArray("geocodes").getJSONObject(0).getString("location");
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			//log.info(e.getMessage());
		}
		return lat;
	}

	//根据经纬度信息获取城市具体信息
	private Gaode get_city(String msg){
		//String city = "";
		Gaode gaode = new Gaode() ;
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
			//System.out.println("逆地址解析："+result);
			JSONObject json = new JSONObject(result);
			json = json.getJSONObject("regeocode").getJSONObject("addressComponent");
			if(!json.get("province").toString().contains("[")){
				String province = json.get("province").toString();
				gaode.setProvince(province);
				//	city +=;
			}
			if(!json.get("city").toString().contains("[")){
				String city = json.get("city").toString();
				gaode.setCity(city);
				//city +=
			}
			/*if(!json.get("district").toString().contains("[")){
				String district = json.getString("district");
				gaode.setDistrict(district);
				//city +=
			}*/
			if(!json.isNull("citycode")){
				String citycode = json.getString("citycode");
				gaode.setCitycode(citycode);
			}
			//city = json.get("province")+" "+json.get("city");
		}
		catch(Exception e){
			e.printStackTrace();
			//log.info(e.getMessage());
		}
		return gaode;
	}

	private String get_citycode(String location){
		String citycode = "";
		String key = "2fc9168c57e034f73dca05ed44091b2b";
		try {
			location = URLEncoder.encode(location,"UTF-8");
		}
		catch(UnsupportedEncodingException e1){
			e1.printStackTrace();
			//log.info(e1.getMessage());
		}
		String url = "http://restapi.amap.com/v3/geocode/geo?key="+key+"&address="+location+"&city=";
		//System.out.println(url);
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
			//System.out.println(result);
			JSONObject json = new JSONObject(result);
			if(!json.isNull("status") && json.getString("status").equals("1")){
				if(!json.isNull("geocodes") && json.getJSONArray("geocodes").length()>0){
					if(!json.getJSONArray("geocodes").getJSONObject(0).isNull("citycode")){
						citycode = json.getJSONArray("geocodes").getJSONObject(0).getString("citycode");
						//System.out.println(citycode);
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			//log.info(e.getMessage());
		}
		return citycode;
	}

	class Gaode{
		private String province;
		private String city;
		private String district;
		private String citycode;
		public String getProvince() {
			return province;
		}
		public void setProvince(String province) {
			this.province = province;
		}
		public String getCity() {
			return city;
		}
		public void setCity(String city) {
			this.city = city;
		}
		public String getDistrict() {
			return district;
		}
		public void setDistrict(String district) {
			this.district = district;
		}
		public String getCitycode() {
			return citycode;
		}
		public void setCitycode(String citycode) {
			this.citycode = citycode;
		}
	}

}
