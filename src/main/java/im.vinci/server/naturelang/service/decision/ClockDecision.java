package im.vinci.server.naturelang.service.decision;

import im.vinci.server.naturelang.domain.ServiceRet;
import org.ansj.domain.Result;
import org.ansj.splitWord.analysis.ToAnalysis;

import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ClockDecision {
	private Hashtable<String,Integer> number_list= new Hashtable<String,Integer>();
	private String[] select_list={"查看","查询","看看"};//enter search situation condition
	private String[] delete_list={"删除","取消"};//enter cancel situation condition
	//private Logger log = LoggerFactory.getLogger(this.getClass());
	public ServiceRet service_check(String msg){
		//log.info("service: clock check");
		//log.info("message is:"+msg);
		msg = number_exchange(msg);
		//JSONObject result = new JSONObject();
		ServiceRet sr = new ServiceRet();
		try{
			if(msg.equals(null) || msg.equals("")){
				sr.setRc(4);
				sr.setService("");
				sr.setOperation("");
				return sr;
			}
			String[] enter_condition = {"定时","提醒","闹钟","闹铃","叫醒","提示","专注","静心","专心","通知"};
			for(int i=0;i<enter_condition.length;i++){
				if(msg.contains(enter_condition[i])){
					sr.setRc(0);
					sr.setService("schedule");
				}
			}
			boolean time = false;
			Result r = ToAnalysis.parse(msg);
			String[] str = r.toString().split(",");
			for(int i=0;i<str.length;i++){
				if(str[i].split("/").length>1 && str[i].split("/")[1].equals("t")){
					time = true;
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
			p = Pattern.compile("(吃饭)|(睡觉)|(起床)|(开会)|(约会)|(打电话)|(叫\\D)|(买|卖)|(出门)");
			if(time == true){
				m = p.matcher(msg);
				if(m.find()){
					sr.setRc(0);
					sr.setService("schedule");
				}
			}

			sr.setOperation("");
			//check if he want to search clock
			for(int i=0;i<this.select_list.length;i++){
				if( msg.contains(this.select_list[i])){
					//query();
					sr.setOperation("VIEW");
					return sr;
				}
			}
			p = Pattern.compile("有\\S{0,2}个");
			m = p.matcher(msg);
			if(m.find()){
				sr.setOperation("VIEW");
				return sr;
			}
			//check if he want to delete clock
			for(int i=0;i<this.delete_list.length;i++){
				if( msg.contains(this.delete_list[i])){
					//delete();
					sr.setOperation("DELETE");
					return sr;
				}
			}
			if(sr.getOperation().equals("") && sr.getService() != null){
				sr.setOperation("CREATE");
				return sr;
			}
			if(sr.getService() == null){
				//System.out.println("hah ");
				sr.setRc(4);
				sr.setService("");
			}
		}
		catch(Exception e){
			//	log.info("error is:"+e.getMessage());
		}
		//log.info(result.toString());
		return sr;
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
