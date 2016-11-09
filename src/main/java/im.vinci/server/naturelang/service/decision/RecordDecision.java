package im.vinci.server.naturelang.service.decision;

import im.vinci.server.naturelang.domain.ServiceRet;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//问题：没有进行数字的转换呢

public class RecordDecision {
	private Hashtable<String,Integer> number_list= new Hashtable<String,Integer>();
	private Logger log = LoggerFactory.getLogger(this.getClass());
	public ServiceRet record_check(String msg){
		//String[] keywords = {"???","?????","?1??","???","?1??","???","????????","","","","","","",""};
		//JSONObject result = new JSONObject();
		ServiceRet sr = new ServiceRet();
		//log.info("record check service start");
		try{
			if(msg.equals(null) || msg.equals("")){
				sr.setRc(4);
				sr.setService("");
				sr.setOperation("");
				return sr;
				//result.put("rc", 4);
				//result.put("service", "record");
				//result.put("operation", "");
				//log.info("message is null");
				//return result.toString();
			}
			msg = number_exchange(msg);
			boolean time = false;
			Result r = ToAnalysis.parse(msg);
			String[] str = r.toString().split(",");
			for(int i=0;i<str.length;i++){
				if(str[i].split("/")[1].equals("t")){
					time = true;
					break;
				}
			}
			if(!time){
				Pattern p = Pattern.compile("(\\d{1,2}(日|号))|(刚才)");
				Matcher m = p.matcher(msg);
				if(m.find()){
					time = true;
				}
			}
			Pattern p = Pattern.compile("(录\\S{0,2}音)|(录(\\S?)个)|(录\\S?下)|(录\\S?(段|条|了))|(语音备忘)|(录\\S{0,4}声)");
			Matcher m = p.matcher(msg);
			m = p.matcher(msg);
			if(m.find()){
				sr.setRc(0);
				sr.setService("record");
				//result.put("rc", 0);
				//result.put("service", "record");
				if(msg.contains("播放")||msg.contains("听")||time){
					sr.setOperation("query");
					return sr;
					//result.put("operation", "query");
					//Select_record sr = new Select_record();
					//sr.selectRecord(msg);
				}
				sr.setOperation("set");
				return sr;
				//result.put("opration", "set");
			}
			else if(msg.contains("录的")&&time){
				sr.setRc(0);
				sr.setService("record");
				sr.setOperation("query");
				return sr;
				//result.put("rc", 0);
				//result.put("service", "record");
				//result.put("operation", "query");
				//Select_record sr = new Select_record();
				//sr.selectRecord(msg);
			}
		}
		catch(Exception e){
			//e.printStackTrace();
			log.error(e.getMessage());
		}
		sr.setRc(4);
		sr.setService("");
		sr.setOperation("");
		return sr;
		//log.info("result is:"+result.toString());
		//return result.toString();
	}

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

