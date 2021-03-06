package im.vinci.server.naturelang.service.back;

import im.vinci.server.naturelang.domain.Parameter;
import im.vinci.server.naturelang.domain.RecordResponse;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 录音处理
 */
public class RecordBack {
	private Hashtable<String,Integer> number_list= new Hashtable<String,Integer>();
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private String oriDate = "";

	public RecordResponse selectRecord(Parameter parameter){
		RecordResponse response = new RecordResponse();
		try {
			if(parameter.getQuery().equals(null) || parameter.getQuery().equals("")){
				response.setRc(4);
				response.setText(parameter.getQuery());
				response.setRtext("输入是空");
				return response;
			}
			String query = parameter.getQuery();//json.getString("query");
			response.setText(query);
			response.setRtext("");
			response.setService("record");
			response.setOperation("query");
			query = numberExchange(query);
			int[] month = timeGet(query);
			DateTime dt = new DateTime();
			DateTime dat = new DateTime(dt.getYear(),dt.getMonthOfYear(),dt.getDayOfMonth(),0,0);
			if(month[1]>0||month[2]>0){
				dt = new DateTime(month[0]>0?month[0]:dt.getYear(),month[1]>0?month[1]:dt.getMonthOfYear(),month[2]>0?month[2]:dt.getDayOfMonth(),0,0);
				if(dt.compareTo(dat)>0){
					if(month[1]==0 && dt.getMonthOfYear()==dat.getMonthOfYear()){
						dt = dt.plusMonths(-1);
					}
					else{
						dt = dt.plusYears(-1);
					}
				}
				SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
				Date d = dt.toDate();
				response.setRc(0);
				response.getSemantic().getSlots().setDatetime(sf.format(d));
				response.getSemantic().getSlots().setDateOrig(oriDate);
				response.getSemantic().getSlots().setType("all");
			} else {
				Pattern p = Pattern.compile("(最(新|近))|(刚(才|刚))|(上1?(个|条|次))");
				Matcher m = p.matcher(query);
				if (m.find()) {
					response.setRc(0);
					response.getSemantic().getSlots().setDatetime("");
					response.getSemantic().getSlots().setDateOrig(oriDate);
					response.getSemantic().getSlots().setName("");
					response.getSemantic().getSlots().setType("last");
				} else {
					p = Pattern.compile("录音\\d{7,9}");
					m = p.matcher(query);
					if (m.find()) {
						response.setRc(0);
						response.getSemantic().getSlots().setDatetime("");
						response.getSemantic().getSlots().setDateOrig(oriDate);
						response.getSemantic().getSlots().setType("special");
						response.getSemantic().getSlots().setName(query.substring(m.start(), m.end()));
					} else {
						response.setRc(0);
						response.getSemantic().getSlots().setDatetime("");
						response.getSemantic().getSlots().setDateOrig(oriDate);
						response.getSemantic().getSlots().setType("all");
						response.getSemantic().getSlots().setName("");
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			response.getSemantic().getSlots().setType("wrong");
			log.info(e.getMessage());
			throw e;
		}
		return response;
	}

	public int[] timeGet(String msg){
		int[] month = new int[3];//year-month-day
		Pattern p = Pattern.compile("\\d{1,2}月\\d{1,2}(日|号)");
		Matcher m = p.matcher(msg);
		if(m.find()){
			this.oriDate = msg.substring(m.start(),m.end());
			month[0] = new DateTime().getYear();
			p = Pattern.compile("\\d{1,2}");
			m = p.matcher(msg);
			if(m.find()){
				month[1]=Integer.parseInt(msg.substring(m.start(),m.end()));
			}
			if(m.find()){
				month[2]=Integer.parseInt(msg.substring(m.start(),m.end()));
			}
		}
		else{
			p = Pattern.compile("上上个+月");
			m = p.matcher(msg);
			if(m.find()){
				month[0]=(new DateTime()).plusMonths(-2).getYear();
				month[1]=(new DateTime()).plusMonths(-2).getMonthOfYear();
			}
			else{
				p = Pattern.compile("上个+月");
				m = p.matcher(msg);
				if(m.find()){
					this.oriDate = msg.substring(m.start(),m.end());
					month[0]=(new DateTime()).plusMonths(-1).getYear();
					month[1]=(new DateTime()).plusMonths(-1).getMonthOfYear();
				}
			}
			p = Pattern.compile("月*\\d{1,2}(日|号)");
			m = p.matcher(msg);
			if(m.find()){
				msg=msg.substring(m.start(),m.end());
				this.oriDate = this.oriDate + msg.substring(m.start(),m.end());
				p = Pattern.compile("\\d{1,2}");
				m = p.matcher(msg);
				if(m.find()){
					month[2] = Integer.parseInt(msg.substring(m.start(),m.end()));
				}
			}
			else if(msg.contains("昨天")){
				this.oriDate = "昨天";
				month[0] = new DateTime().plusDays(-1).getYear();
				month[1] = new DateTime().plusDays(-1).getMonthOfYear();
				month[2] = new DateTime().plusDays(-1).getDayOfMonth();
			}
			else if(msg.contains("大前天")){
				this.oriDate = "大前天";
				month[0] = new DateTime().plusDays(-3).getYear();
				month[1] = new DateTime().plusDays(-3).getMonthOfYear();
				month[2] = new DateTime().plusDays(-3).getDayOfMonth();
			}
			else if(msg.contains("前天")){
				this.oriDate = "前天";
				month[0] = new DateTime().plusDays(-2).getYear();
				month[1] = new DateTime().plusDays(-2).getMonthOfYear();
				month[2] = new DateTime().plusDays(-2).getDayOfMonth();
			}
			else if(msg.contains("今天")){
				this.oriDate = "今天";
				month[0] = new DateTime().getYear();
				month[1] = new DateTime().getMonthOfYear();
				month[2] = new DateTime().getDayOfMonth();
			}

		}
		return month;
	}

	private String numberExchange(String msg){
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
