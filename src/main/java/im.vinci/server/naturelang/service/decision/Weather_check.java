package im.vinci.server.naturelang.service.decision;

import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import im.vinci.server.naturelang.domain.ServiceRet;

public class Weather_check {
	private Hashtable<String,Integer> number_list= new Hashtable<String,Integer>();
	private Logger log = LoggerFactory.getLogger(this.getClass());
	public ServiceRet weather_check(String msg){
		//log.info("Weather check service");
		String[] function_word = {"怎么样","如何"};//有云，有太阳，有雾，有霾，有风，有雨，有雷电，闪电，打雷
		//String msg = "";
		//JSONObject result = new JSONObject();
		//log.info("weather check message : "+msg);
		ServiceRet sr = new ServiceRet();
		msg = msg.replace("月份", "月 ");
		msg = number_exchange(msg);
		boolean time = false;
		boolean city = false;
		try{
			if(msg.equals(null) || msg.equals("")){
				//result.put("rc", 4);
				sr.setRc(4);
				//result.put("service", "record");
				sr.setService("");
				//result.put("operation", "");
				sr.setOperation("");
				//log.info("message is null");
				return sr;
				//return result.toString();
			}
			String[] keywords = {"天气","气温","温度","湿度","气候","降水量","外面"};//"空气怎么样",,"空气质量","","","","","","","","","","",""};
			Result r = ToAnalysis.parse(msg);
			String[] str = r.toString().split(",");
			for(int i=0;i<str.length;i++){
				if(str[i].contains("/")){
					if(str[i].split("/")[1].equals("t")){
						time = true;
					}
					else if(str[i].split("/")[1].equals("ns") || str[i].split("/")[1].equals("f")){
						city = true;
					}
				}
			}
			Pattern p = Pattern.compile("(\\d{1,2}点钟+)|(\\d{1,2}点\\d{1,2}分)|(\\d{1,2}点(半|1刻))|(个月\\d{1,2}(号|日))|(\\d{1,2}(号|日))");
			Matcher m ;
			if(time == false){
				m = p.matcher(msg);
				if(m.find()){
					time = true;
				}
			}
			p = Pattern.compile("(多少度)|(热不热)|(冷不冷)|((冷|热)吗)|((有(雨|雪|雾|风|(太阳)))|((雨|雪|雾|风|(太阳))大)|((下|有)\\D{0,2}(雨|雪))|(晴天)|(阴天)|(多云)|(有雾)|(适合((出门)|(出去玩)|(去旅游)))|(要(打|拿|带)*\\D{0,1}伞)|(雨衣))");
			m = p.matcher(msg);
			//System.out.println("time:"+time+"\ncity:"+city);
			if(time || city){
				if(m.find()){
					Pattern pa = Pattern.compile("(播放)|(来\\S?(首|个))|(听)");
					Matcher ma = pa.matcher(msg);
					if(ma.find()){
						//System.out.println(false);
						log.info("weather service failed");
						//result.put("rc", 4);
						sr.setRc(4);
						//result.put("service", "record");
						sr.setService("");
						//result.put("operation", "");
						sr.setOperation("");
						return sr;
					}
					else{
						//System.out.println(true);
						log.info("weather service success");
						//result.put("rc", 0);
						sr.setRc(0);
						sr.setService("weather");
						sr.setOperation("query");
						return sr;
						//result.put("service", "weather");
						//result.put("operation", "query");
					}
				}
				else{
					for(int i=0;i<keywords.length;i++){
						if(msg.contains(keywords[i])){
							//System.out.println(true);
							log.info("weather service success");
							//result.put("rc", 0);
							sr.setRc(0);
							sr.setService("weather");
							sr.setOperation("query");
							return sr;
						}
					}
				}
			}
			else{
				m = p.matcher(msg);
				for(int i=0;i<keywords.length;i++){
					if(msg.contains(keywords[i])){
						if(m.find()){
							//System.out.println(true);
							sr.setRc(0);
							sr.setService("weather");
							sr.setOperation("query");
							return sr;
							//result.put("rc", 0);
						}
						else{
							for(int j=0;j<function_word.length;j++){
								if(msg.contains(function_word[j])){
									//System.out.println(true);
									log.info("weather service success");
									sr.setRc(0);
									sr.setService("weather");
									sr.setOperation("query");
									return sr;
									//result.put("rc", 0);
									//result.put("service", "weather");
									//result.put("operation", "query");
								}
							}
						}
					}
					else if(msg.length()<5){
						if(m.find()){
							//System.out.println(true);
							log.info("weather service success");
							sr.setRc(0);
							sr.setService("weather");
							sr.setOperation("query");
							return sr;
							//result.put("rc", 0);
							//result.put("service", "weather");
							//result.put("operation", "query");
						}
					}
				}
			}

			sr.setRc(4);
			sr.setService("");
			sr.setOperation("");
		}
		catch(Exception e){
			//e.printStackTrace();
			log.info(e.getMessage());
		}
		return sr;
	}


	//exchange the number
	private String number_exchange(String msg){
		this.number_list.put("零", 0);
		this.number_list.put("一", 1);
		this.number_list.put("二", 2);
		this.number_list.put("两", 2);
		this.number_list.put("三", 3);
		this.number_list.put("四", 4);
		this.number_list.put("五", 5);
		this.number_list.put("六", 6);
		this.number_list.put("七", 7);
		this.number_list.put("八", 8);
		this.number_list.put("九", 9);
		int number=0;
		StringBuffer sb = new StringBuffer(msg);
		for(int i = 0;i < msg.length();){
			if (this.number_list.containsKey(msg.charAt(i)+"")){
				if (i+1<msg.length() && msg.charAt(i+1)=='十'){
					if (i+2<msg.length() && this.number_list.containsKey(msg.charAt(i+2)+"")){
						number = this.number_list.get(msg.charAt(i)+"")*10 + this.number_list.get(msg.charAt(i+2)+"");
						sb=sb.replace(i, i+3, String.valueOf(number));
						msg=sb.toString();
						i=i+3;
						continue;
					}
					else{
						number = this.number_list.get(msg.charAt(i)+"")*10;
						sb=sb.replace(i, i+2, String.valueOf(number));
						msg=sb.toString();
						i=i+2;
						continue;
					}
				}
				else{
					number=this.number_list.get(msg.charAt(i)+"");
					sb=sb.replace(i, i+1, String.valueOf(number));
					i=i+1;
					continue;
				}
			}
			else if (msg.charAt(i)=='十'){
				if(i+1<msg.length() && this.number_list.containsKey(msg.charAt(i+1)+"")){
					number = 10+this.number_list.get(msg.charAt(i+1)+"");
					sb=sb.replace(i, i+2, String.valueOf(number));
					msg=sb.toString();
					i=i+2;
					continue;
				}
				else{
					number = 10;
					sb=sb.replace(i, i+1, String.valueOf(number));
					msg=sb.toString();
					i=i+1;
					continue;
				}
			}
			i=i+1;
		}
		Pattern p = Pattern.compile("周(日|天)");
		Matcher m = p.matcher(sb);
		while(m.find()){
			sb = sb.replace(m.start()+1, m.end(), "7");
		}
		msg=sb.toString();
		return msg;
	}

}