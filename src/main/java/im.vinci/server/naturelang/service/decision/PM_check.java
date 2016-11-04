package im.vinci.server.naturelang.service.decision;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import im.vinci.server.naturelang.domain.ServiceRet;

public class PM_check {
	public ServiceRet pm_check(String msg){
		msg = msg.toLowerCase();
		ServiceRet sr = new ServiceRet();
		Pattern p = Pattern.compile("(空气质量)|(空气\\S{0,2}((如何)|(怎么样)))|(pm\\S{0,1}2.5)|(pm值)|(空气污染)|(雾霾)");
		Matcher m = p.matcher(msg);
		if(m.find()){
			sr.setRc(0);
			sr.setService("PM2.5");
			sr.setOperation("query");
		}
		else{

			sr.setRc(4);
			sr.setService("");
			sr.setOperation("");
		}
		return sr;
	}

}
