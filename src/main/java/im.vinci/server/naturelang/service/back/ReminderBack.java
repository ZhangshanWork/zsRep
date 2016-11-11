package im.vinci.server.naturelang.service.back;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import im.vinci.server.naturelang.domain.Parameter;
import im.vinci.server.naturelang.domain.Response;
import im.vinci.server.naturelang.service.decision.ReminderDecision;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

public class ReminderBack {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    private Hashtable<String,Integer> number_list= new Hashtable<String,Integer>(),
            day_list=new Hashtable<String,Integer>(),
            time_list=new Hashtable<String,Integer>(),
            week_list=new Hashtable<String,Integer>();
    public DateTime dt = new DateTime();
    private String[] select_list={"查看","查询","看看"};//enter search situation condition
    private String[] delete_list={"删除","取消"};//enter cancel situation condition
    private String[] concentrate_lsit={"专心","专注","静下心"};//enter concentrate situation condition
    private String[] fix_list={"定时"};//enter fix time condition
    public String msg="",result="",message="";//msg:the message send in, result:store the json result, message:store the affair
    public Response response = new Response();
    private String[] time=new String[6]; //store the crontab time information(minute,hour,day,month,week,year)
    public int tomorrow=0,dialogue_time=0;
    private int[] advance = {0,0};
    public ReminderBack(Parameter parameter) throws JsonProcessingException {
        initial();
        preAnalyse(parameter);
    }
    private void initial(){
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
        this.day_list.put("月", 0);
        this.day_list.put("日", 1);
        this.day_list.put("号", 1);
        this.time_list.put("点", 0);
        this.time_list.put("分",1);
        this.time_list.put("的", 1);
        this.time_list.put("提", 1);
        this.week_list.put("一", 1);
        this.week_list.put("二", 2);
        this.week_list.put("三", 3);
        this.week_list.put("四", 4);
        this.week_list.put("五", 5);
        this.week_list.put("六", 6);
        this.week_list.put("日", 7);
        this.week_list.put("天", 7);
        for(int i=0;i<this.time.length;i++){
            this.time[i]="*";
        }
    }

    //预处理
    private void preAnalyse(Parameter parameter) throws JsonProcessingException {
        if(parameter.equals(null)||parameter.getQuery()==null||parameter.getQuery().equals("")){
            response.setRc(4);
            response.setRtext("输入为空");
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                result = objectMapper.writeValueAsString(response).toString();
                return;
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage());
                throw e;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage());
                throw e;
            }
        }
        if(StringUtils.isNotBlank(parameter.getQuery())){
                msg = parameter.getQuery();
        }

        this.response.setText(msg);
        this.msg = insteadWeek(msg);
        this.msg = numberExchange(this.msg);
        this.message = msg;
        //check if he want to search clock
        ReminderDecision cc = new ReminderDecision();
        String operation = cc.service_check(this.msg).getOperation();
        if(operation.equals("VIEW")){
            this.response.setOperation("VIEW");
            selectQuery(this.msg);
            return;
        }
        //check if he want to delete clock
        for(int i=0;i<this.delete_list.length;i++){
            if( this.msg.contains(this.delete_list[i])){
                this.response.setOperation("DELETE");
                selectQuery(this.msg);
                return;
            }
        }
        //check if he want to concentrate
        for(int i=0;i<this.concentrate_lsit.length;i++){
            if( this.msg.contains(this.concentrate_lsit[i])){
                this.response.setOperation("CONCENTRATE");
                int time = getAfterTime(this.msg);
                if(time>0){
                    dt = dt.plusHours(time/60);
                    dt = dt.plusMinutes(time%60);
                }
                else{
                    dt = dt.plusMinutes(30);
                    time=30;
                }
                resultWrite();
                return;
            }
        }
        //check it is fix time or not
        for(int i=0;i<this.fix_list.length;i++){
            if( this.msg.contains(this.fix_list[i])){
                int time = getAfterTime(this.msg);
                if(time>0){
                    dt = dt.plusHours(time/60);
                    dt = dt.plusMinutes(time%60);
                }
                else{
                    dt = dt.plusMinutes(30);
                }
                this.response.setOperation("CREATE");
                resultWrite();
                return;
            }
        }
        //if the action above are not satisfied, to set clock action
        setClock(this.msg);
    }
    //instead the different type of weekday to zhou type
    private String insteadWeek(String msg){
        StringBuffer sb = new StringBuffer(msg);
        Pattern p = Pattern.compile("个*周|个*礼拜|个*星期");
        Matcher m = p.matcher(msg);
        while(m.find()){
            sb = sb.replace(m.start(), m.end(), "周");
        }
        msg = sb.toString();
        return msg;
    }
    //change the chinese number to 1,2,3..
    private String numberExchange(String msg){
        int number = 0;
        if(msg.contains("今晚")){
            msg = msg.replace("今晚", "今天晚上");
        }
        if(msg.contains("明晚")){
            msg = msg.replace("明晚", "明天晚上");
        }
        if(msg.contains("今早")){
            msg = msg.replace("今早", "今天早上");
        }
        if(msg.contains("明早")){
            msg = msg.replace("明早", "明天早上");
        }
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
    //set  the  clock  for  user
    private  void setClock(String  msg){
        this.response.setOperation("CREATE");
        checkFestival(this.msg);
        int  number=0;
//check  if  he  want  set  after  clock
        number= checkAfter(this.msg);
        if(number>0){
            String  str  =  this.msg.substring(((number-10)<0?0:number-10),number);
            int  time  =  getAfterTime(str);
            if(time>0){
                dt  =  dt.plusHours(time/60);
                dt  =  dt.plusMinutes(time%60);
                resultWrite();
                return;
            }
            else{
                Pattern  p  =  Pattern.compile("\\d{1,2}天\\S?后");
                Matcher  m  =  p.matcher(msg);
                if(m.find()){
                    String  s_temp  =  msg.substring(m.start(),m.end());
                    this.message  =  this.message.replace(s_temp,  "  ");
                    p  =  Pattern.compile("\\d{1,2}");
                    m  =  p.matcher(s_temp);
                    if(m.find()){
                        this.dt  =  this.dt.plusDays(Integer.valueOf(s_temp.substring(m.start(),m.end())));
                    }
                }
                else{
                    p  =  Pattern.compile("\\d{1,2}个*月\\S?后");
                    m  =  p.matcher(msg);
                    if(m.find()){
                        String  s_temp  =  msg.substring(m.start(),m.end());
                        this.message  =  this.message.replace(s_temp,  "  ");
                        p  =  Pattern.compile("\\d{1,2}");
                        m  =  p.matcher(s_temp);
                        if(m.find()){
                            this.dt  =  this.dt.plusMonths(Integer.valueOf(s_temp.substring(m.start(),m.end())));
                        }
                    }
                    else{
                        p = Pattern.compile("\\d{1,2}个*周\\S?后");
                        m = p.matcher(msg);
                        if(m.find()){
                            String s_temp = msg.substring(m.start(),m.end());
                            this.message = this.message.replace(s_temp, " ");
                            p = Pattern.compile("\\d{1,2}");
                            m = p.matcher(s_temp);
                            if(m.find()){
                                this.dt = this.dt.plusDays(7*Integer.valueOf(s_temp.substring(m.start(),m.end())));
                            }
                        }
                    }
                }
                int[]  hour  =  getHour(msg);
                if(hour[1]>0  ||  hour[2]>0){
                    this.dt  =  new  DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    lastCheck(hour);
                    resultWrite();
                    return;
                }
                else{
                    this.response.setRtext("我不太懂，请使用明确时间进行设定");
                    resultWrite();
                    return;
                }
            }
        }
        if(msg.contains("提前")){
            this.message  =  this.message.replace("提前",  "  ");
            Pattern  p  =  Pattern.compile("提前");
            Matcher  m  =  p.matcher(msg);
            if(m.find()){
                int  times  =  getAfterTime(msg.substring(m.start(),m.end()+7));
                if(times>0){
                    advance[0]=times/60;
                    advance[1]=times%60;
                }
            }
        }
        if(checkRegular(msg)){
            boolean  duration  =  false;
            Pattern  p  =  Pattern.compile("每隔*\\d{1,2}天");
            Matcher  m  =  p.matcher(msg);
            if(m.find()){
                duration  =  true;
                String  m_temp  =  msg.substring(m.start(),  m.end());
                this.message  =  this.message.replace(m_temp,  "  ");
                p  =  Pattern.compile("\\d{1,2}");
                m  =  p.matcher(m_temp);
                if(m.find()){
                    this.time[2]  =  m_temp.substring(m.start(),m.end());
                }
            }
            p  =  Pattern.compile("每隔*\\d个月");
            m  =  p.matcher(msg);
            if(m.find()){
                duration  =  true;
                String  m_temp  =  msg.substring(m.start(),  m.end());
                this.message  =  this.message .replace(m_temp, " ");
                p = Pattern.compile("\\d{1,2}");
                m = p.matcher(m_temp);
                if(m.find()){
                    this.time[3] = m_temp.substring(m.start(),m.end());
                }
            }
            p = Pattern.compile("每个*月\\d{1,2}(号|日)");
            m = p.matcher(msg);
            if(m.find()){
                String m_temp = msg.substring(m.start(), m.end());
                this.message = this.message.replace(m_temp, " ");
                p = Pattern.compile("\\d{1,2}");
                m = p.matcher(m_temp);
                if(m.find()){
                    this.time[2] = "month"+" "+m_temp.substring(m.start(),m.end());
                }
            }
            if(msg.contains("每天")){
                this.time[2]="day";
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("每天");
                this.message = this.message.replace("每天", " ");
            }
            else if(msg.contains("周末")){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("周末");
                this.time[4]="6-7";
                this.message = this.message.replace("周末", " ");
            }
            else if(msg.contains("工作日")){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("工作日");
                this.time[4]="1-5";
                this.message = this.message.replace("工作日", " ");
            }
            else if(msg.contains("每周")){
                p = Pattern.compile("每周\\d");
                m = p.matcher(msg);
                while(m.find()){
                    this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                    if((msg.charAt(m.end()-2)+"").equals("周")){
                        time[4]=String.valueOf(msg.charAt(m.end()-1));
                        this.response.getSemantic().getSlots().getDatetime().setDateOrig("每周"+time[4]);
                        msg = msg.replace(msg.substring(m.start(),m.end()), " ");
                    }
                }
            }
            int[] hour = getHour(msg);
            if(hour[1]>0 || hour[2]>0){
                this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                lastCheck(hour);
                resultWrite();
                return;
            }
            else if(!duration){
                this.response.setRtext("我不太懂，请使用明确时间进行设定");
                resultWrite();
                return;
            }
            else{
                resultWrite();
                return;
            }
        }
        int day= getWeek(msg);
        if(day>0){
            String replace = "周"+day;
            if(msg.contains("下下周")){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("下下周"+day);
                day=day+14;
                this.message = this.message.replace("下下周", " ");
            }
            else if(msg.contains("下周")){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("下周"+day);
                day=day+7;
                this.message = this.message.replace("下周", " ");
            }
            if(day<this.dt.getDayOfWeek()){//(day<now_day){
                this.response.setOperation("FAILED");
                this.response.setRtext("本周已经没有了");
                resultWrite();
                return;
            }
            else{
                this.dt = this.dt.plusDays(day-this.dt.getDayOfWeek());
            }
            msg = msg.replace(replace,  " ");
            int[] hour= getHour(msg);
            if(hour[1]>0 || hour[2]>0){
                this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                lastCheck(hour);
                resultWrite();
                return;
            }
            else{
                this.response.setRtext("我不太懂，请使用明确时间进行设定");
                resultWrite();
                return;
            }
        }
        int[] month = get_month(this.msg);
        if(month[0]>0 || month[1]>0){
            this.dt = new DateTime((month[2]>0?month[2]:this.dt.getYear()),(month[0]>0?month[0]:this.dt.getMonthOfYear()),(month[1]>0?month[1]:this.dt.getDayOfMonth()),this.dt.getHourOfDay(),this.dt.getMinuteOfHour());
            if(this.dt.compareTo(new DateTime(new DateTime().getYear(),new DateTime().getMonthOfYear(),new DateTime().getDayOfMonth(),0,0))<0){
                if(month[0]==0){
                    this.dt = this.dt.plusMonths(1);
                }
                else{
                    this.dt = this.dt.plusYears(1);
                }
            }
        }
        int[] hour = getHour(this.msg);
        if(true){
            if(hour[1]>0 || hour[2]>0){
                this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1]%24,hour[2]);
                if(hour[1]>23){
                    this.dt = this.dt.plusDays(1);
                }
                lastCheck(hour);
                resultWrite();
                return;
            }
            else{
                this.response.setRtext("我不太懂，请使用明确时间进行设定");
                resultWrite();
                return;
            }
        }
    }
    //check if it contain festival
    private void checkFestival(String msg){
        msg = msg.replace("大年30", "除夕");
        msg = msg.replace("大年初1", "春节");
        msg = msg.replace("正月15", "元宵节");
        msg = msg.replace("8月15", "中秋节");
        this.message = msg;
        FileInputStream f;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader((new ClassPathResource("nlp/dates.txt")).getInputStream(), "utf8"));
            String line = br.readLine();
            String fastivel = "";
            DateTime d2 = new DateTime();
            while(line != null){
                String[] lines = line.split(" ");
                String[] date = lines[0].split("-");
                DateTime d = new DateTime();
                d2 = new DateTime(Integer.valueOf(date[0]),Integer.valueOf(date[1]),Integer.valueOf(date[2]),0,0);
                if(d2.compareTo(d)>0 && d2.compareTo(d.plusYears(1))<0){
                    if(msg.contains(lines[1])||(msg.contains(lines[1].replace("节", ""))&&!lines[1].equals("春节"))){
                        fastivel = lines[1];
                        this.dt = d2;
                        break;
                    }
                }
                else if(d2.compareTo(d.plusYears(1))>0){
                    break;
                }
                line = br.readLine();
            }
            if(!fastivel.equals("")){
                //System.out.println();
                Pattern p = Pattern.compile("("+fastivel+"的*前\\d天)|"+"("+fastivel.replace("节","")+"的*前\\d天)");
                Matcher m = p.matcher(msg);
                //System.out.println(p);
                if(m.find()){
                    this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                    this.response.getSemantic().getSlots().getDatetime().setDateOrig(msg.substring(m.start(),m.end()));
                    //System.out.println(msg.charAt(m.end()-2));
                    d2 = d2.plusDays(48-msg.charAt(m.end()-2));
                    this.msg.replace(this.msg.substring(m.start(),m.end()),"" );
                    this.dt = d2;
                }
                else{

                    this.message = this.message.replace(fastivel, " ");
                }
            }
            //System.out.println(d2.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            //log.info(e.getMessage());
            e.printStackTrace();
        }

    }

    //check if contain regular keywords
    private boolean checkRegular(String msg){
        Pattern p = Pattern.compile("每隔*(周|\\d{0,2}天|\\d个月)|周末|工作日|(每个*月)");
        Matcher m = p.matcher(msg);
        if(m.find()){
            this.response.getSemantic().getSlots().getDatetime().setRepeat(true);
            return true;
        }
        return false;
    }
    //get the day of the week
    private int getWeek(String msg){
    int day=0;
        Pattern p = Pattern.compile("周\\d");
        Matcher m = p.matcher(msg);
        if(m.find()){
            day=Integer.parseInt(msg.charAt(m.end()-1)+"");
        }
        return day;
    }
    //get the hour in the message
    private int[] getHour(String msg){
        //hour[0]:if there two time;hour[1]:the hour number;hour[2]:the minute number
        int[] hour=new int[5];
        int i=0;
        String s = msg;
        for(int j=0;j<hour.length;j++){
            hour[j]=0;
        }
        Pattern p = Pattern.compile("\\d{1,2}点");
        Matcher m = p.matcher(msg);
        String timeOrige = "";
        while(m.find()){
            if(i==0){
                timeOrige = msg.substring(m.start(),m.end());
            }
            s=msg.substring(0,(m.end()+4>msg.length()?msg.length():m.end()+2));
            hour[i*2+1]=Integer.parseInt(msg.substring(m.start(),m.end()-1));
            if ((s.contains("下午") || s.contains("晚上"))&& hour[i*2+1]<12){
                if(i==0){
                    if(s.contains("下午")){
                        timeOrige = "下午"+timeOrige;
                    }
                    else{
                        timeOrige = "晚上"+timeOrige;
                    }
                }
                hour[i*2+1]=hour[i*2+1]+12;
            }
            else if(s.contains("中午") && hour[i*2+1]<4){
                hour[i*2+1]=hour[i*2+1]+12;
                if(i==0){
                    timeOrige = "中午"+timeOrige;
                }
            }
            else if(s.contains("半夜") && hour[i*2+1]<3){
                this.dt = this.dt.plusDays(1);
                if(i==0){
                    timeOrige = "半夜"+timeOrige;
                }
            }
            else if(s.contains("半夜")){
                hour[i*2+1]=hour[i*2+1]+12;
                if(i==0){
                    timeOrige = "半夜"+timeOrige;
                }
            }
            if(m.end()<msg.length() && (msg.charAt(m.end())+"").equals("半")){
                hour[2*i+2]=30;
                s=msg.substring(m.end());
                if(i==0){
                    timeOrige = timeOrige+"半";
                }
            }
            else if(m.end()+2<msg.length() && msg.substring(m.end(),m.end()+2).equals("1刻")){
                hour[2*i+2]=15;
                s=msg.substring(m.end());
                if(i==0){
                    timeOrige = timeOrige+"1刻";
                }
            }
            else{
                Pattern pa = Pattern.compile("(\\d{1,2}分)");
                Matcher ma = pa.matcher(s.substring(m.start()));
                if(ma.find()){
                    if(i==0){
                        timeOrige = timeOrige+s.substring(m.start()).substring(ma.start(),ma.end());
                    }
                    hour[2*i+2]=Integer.parseInt(s.substring(m.start()).substring(ma.start(),ma.end()-1));
                    s=msg.substring(ma.end());
                }
                else{
                    pa = Pattern.compile("(点\\d{1,2})");
                    ma = pa.matcher(s);
                    if(ma.find()){
                        if(i==0){
                            timeOrige = timeOrige+s.substring(ma.start(),ma.end()).replace("点", "");
                        }
                        hour[2*i+2]=Integer.parseInt(s.substring(ma.start()+1,ma.end()));
                        s=msg.substring(ma.end());
                    }
                }
            }
            i++;
        }
        this.response.getSemantic().getSlots().getDatetime().setTimeOrig(timeOrige);
        if (i==2){
            hour[0]=1;
        }
        return hour;
    }
    private int checkAfter(String msg){
        int length=-1;
        Pattern p = Pattern.compile("\\D{0,3}((月)|(周)|(天)|(小时)|(分钟*)|(刻钟*)|(个点)|(秒))(以后|后)");
        Matcher m = p.matcher(msg);
        if(m.find()){
            length=m.end();
            int start = m.start();
            return length;
        }
        return length;
    }
    private int getAfterTime(String msg){
        int number=0;
        Pattern p = Pattern.compile("\\d{1,2}个半小时[以后|后]");
        Matcher m = p.matcher(msg);
        if(m.find()){
            this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
            number=Integer.parseInt(msg.substring(m.start(),m.end()-4))*60+30;
        }
        else{
            p = Pattern.compile("半个?小时[以后|后]");
            m = p.matcher(msg);
            boolean flag = false;
            while (m.find()) {
                flag = true;
                break;
            }
            if(flag){
                this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                number = 30;
            }
            else{
                p = Pattern.compile("\\d{1,2}个*小时\\d{1,2}分钟");
                m = p.matcher(msg);
                if(m.find()){
                    this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                    msg = msg.substring(m.start(),m.end());
                    p = Pattern.compile("\\d{1,2}");
                    m = p.matcher(msg);
                    m.find();
                    number = Integer.parseInt(msg.substring(m.start(),m.end()))*60;
                    m.find();
                    number = number + Integer.parseInt(msg.substring(m.start(),m.end()));
                }
                else {
                    p = Pattern.compile("\\d{1,2}个*(小时|点)");
                    m = p.matcher(msg);
                    if(m.find()){
                        this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                        msg = msg.substring(m.start(),m.end());
                        p = Pattern.compile("\\d{1,2}");
                        m = p.matcher(msg);
                        m.find();
                        number = Integer.parseInt(msg.substring(m.start(),m.end()))*60;
                    }
                    else{
                        p = Pattern.compile("\\d{1,2}分钟+");
                        m = p.matcher(msg);
                        if(m.find()){
                            this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                            msg = msg.substring(m.start(),m.end());
                            p = Pattern.compile("\\d{1,2}");
                            m = p.matcher(msg);
                            m.find();
                            number = Integer.parseInt(msg.substring(m.start(),m.end()));
                        }
                    }
                }
                if(msg.contains("一刻钟")){
                    this.message = this.message.replace("一刻钟", " ");
                    number = 15;
                }
            }
        }

        return number;
    }
    private int[] get_month(String msg){
        int[] month= {0,0,0};//month-day-year
        Pattern p = Pattern.compile("\\d{1,2}月\\d{1,2}(日|号)");
        Matcher m = p.matcher(msg);
        if(m.find()){
            this.response.getSemantic().getSlots().getDatetime().setDateOrig(msg.substring(m.start(),m.end()));
            msg=msg.substring(m.start(),m.end());
            this.message = this.message.replace(msg, " ");
            p = Pattern.compile("\\d{1,2}");
            m = p.matcher(msg);
            if(m.find()){
                month[0]=Integer.parseInt(msg.substring(m.start(),m.end()));
            }
            if(m.find()){
                month[1]=Integer.parseInt(msg.substring(m.start(),m.end()));
            }
        }
        else{
            p = Pattern.compile("下下个*月");
            m = p.matcher(msg);
            if(m.find()){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig(msg.substring(m.start(),m.end()));
                this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                month[0]=this.dt.plusMonths(2).getMonthOfYear();
                month[2]=this.dt.plusMonths(2).getYear();
                if(msg.substring(m.end(),m.end()+1).equals("底")){
                    DateTime d = new DateTime(month[2],month[0],1,0,0);
                    this.dt = d.plusMonths(1).plusDays(-1);
                    month[1] = this.dt.getDayOfMonth();
                    this.message = this.message.replace("底"," ");
                }
            }
            else{
                p = Pattern.compile("下个*月");
                m = p.matcher(msg);
                if(m.find()){
                    this.response.getSemantic().getSlots().getDatetime().setDateOrig(msg.substring(m.start(),m.end()));
                    this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                    month[0]=this.dt.plusMonths(1).getMonthOfYear();
                    month[2]=this.dt.plusMonths(1).getYear();
                    if(msg.substring(m.end(),m.end()+1).equals("底")){
                        DateTime d = new DateTime(month[2],month[0],1,0,0);
                        d = d.plusMonths(1).plusDays(-1);
                        month[1] = d.getDayOfMonth();
                        this.message = this.message.replace("底"," ");
                    }
                }
                else{
                    p = Pattern.compile("(这|本)个*月底");
                    m = p.matcher(msg);
                    if(m.find()){
                        this.response.getSemantic().getSlots().getDatetime().setDateOrig(msg.substring(m.start(),m.end()));
                        this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                        DateTime d = this.dt.plusMonths(1);
                        d = new DateTime(d.getYear(),d.getMonthOfYear(),1,0,0);
                        d = d.plusDays(-1);
                        month[0]=d.getMonthOfYear();
                        month[1]=d.getDayOfMonth();
                        month[2]=d.getYear();
                        System.out.println("这个月底"+month[0]+"?"+month[1]);
                    }
                }
            }
            p = Pattern.compile("月*\\d{1,2}(日|号)");
            m = p.matcher(msg);
            if(m.find()){
                String dateOrig = this.response.getSemantic().getSlots().getDatetime().getDateOrig();
                dateOrig = (dateOrig.equals(""))?msg.substring(m.start(),m.end()).replace("月", ""):(dateOrig+msg.substring(m.start(),m.end()).replace("月", ""));
                this.response.getSemantic().getSlots().getDatetime().setDateOrig(dateOrig);
                this.message = this.message.replace(msg.substring(m.start(),m.end()), " ");
                msg=msg.substring(m.start(),m.end());
                p = Pattern.compile("\\d{1,2}");
                m = p.matcher(msg);
                if(m.find()){
                    month[1] = Integer.parseInt(msg.substring(m.start(),m.end()));
                }
            }
            else if(msg.contains("明天")){
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("明天");
                this.message = this.message.replace("明天", " ");
                month[1] = this.dt.plusDays(1).getDayOfMonth();
                month[0] = this.dt.plusDays(1).getMonthOfYear();
                month[2] = this.dt.plusDays(1).getYear();
                this.tomorrow = 1;
            }
            else if(msg.contains("外天") || msg.contains("大后天")){
                this.message = this.message.replace("外天", " ");
                this.message = this.message.replace("大后天", " ");
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("大后天");
                month[1] = this.dt.plusDays(3).getDayOfMonth();
                month[0] = this.dt.plusDays(3).getMonthOfYear();
                month[2] = this.dt.plusDays(3).getYear();
                this.tomorrow = 1;
            }
            else if(msg.contains("后天")){
                this.message = this.message.replace("后天", " ");
                this.response.getSemantic().getSlots().getDatetime().setDateOrig("后天");
                month[1] = this.dt.plusDays(2).getDayOfMonth();
                month[0] = this.dt.plusDays(2).getMonthOfYear();
                month[2] = this.dt.plusDays(2).getYear();
                this.tomorrow = 1;
            }

        }
        return month;
    }
    private void lastCheck(int[] hour){
        DateTime dat = new DateTime();
        if((new DateTime(dat.getYear(),dat.getMonthOfYear(),dat.getDayOfMonth(),0,0)).compareTo(new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0))>0){
            this.response.setOperation("FAILED");
            this.response.setRtext("无法回到过去提醒您");
            return;
        }
        if(this.dt.compareTo(dat)<0 && time[4].equals("*")&&time[2].equals("*")){
            DateTime dt0 = this.dt;
            if(this.dt.getHourOfDay()<12){
                dt0 = this.dt.plusHours(12);
            }
            if(dt0.compareTo(dat)>0 && !msg.contains("早上") && !msg.contains("上午") && !msg.contains("凌晨")){
                this.dt = dt0;
            }
            else{
                if(this.msg.contains("今天")){
                    this.response.setOperation("FAILED");
                    this.response.setRtext("今天已经没有这个时间了");
                    return;
                }
                else if(this.msg.contains("上午")||this.msg.contains("早上")||this.msg.contains("凌晨")){
                    this.dt = this.dt.plusDays(1);
                    if(advance[0]>0||advance[1]>0){
                        this.dt = this.dt.plusHours(0-advance[0]);
                        this.dt = this.dt.plusMinutes(0-advance[1]);
                    }
                    return;
                }
                else if(this.msg.contains("下午")||this.msg.contains("晚上")){
                    this.dt = this.dt.plusDays(1);
                    if(advance[0]>0||advance[1]>0){
                        this.dt = this.dt.plusHours(0-advance[0]);
                        this.dt = this.dt.plusMinutes(0-advance[1]);
                    }
                    return;
                }
                else{
                    this.dt = this.dt.plusDays(1);
                    if(advance[0]>0||advance[1]>0){
                        this.dt = this.dt.plusHours(0-advance[0]);
                        this.dt = this.dt.plusMinutes(0-advance[1]);
                    }
                    return;
                }
            }
        }
        if(this.tomorrow>0 && (new DateTime().getHourOfDay()<4)){
            String speech = "现在是"+(new DateTime().getDayOfMonth())+"号。请问您是要定"+this.dt.getDayOfMonth()+"号，还是"+this.dt.plusDays(-1).getDayOfMonth();
            this.response.setRtext(speech);
            return;

        }
        if(hour[0]>0 && hour[3]>12 && hour[1]>(hour[3]-12)){
        }
        else if(hour[0]>0 && hour[3]>12 && hour[1]<(hour[3]-12)){
            this.dt = this.dt.plusHours(12);
        }
        else if(this.dt.getHourOfDay()<12 && (msg.contains("早上") || msg.contains("上午") || msg.contains("凌晨"))){

        }
        else if(this.dt.getHourOfDay()<12 && (msg.contains("下午") || msg.contains("晚上") || msg.contains("傍晚"))){
            this.dt = this.dt.plusHours(12);
        }
        else if(this.dt.getHourOfDay()<12 && !msg.contains("早上") && !msg.contains("上午") && !msg.contains("凌晨")){
            /*this.response.setRtext("请问是上午还是下午");
            return;*/

        }
        if(advance[0]>0||advance[1]>0){
            this.dt = this.dt.plusHours(0-advance[0]);
            this.dt = this.dt.plusMinutes(0-advance[1]);
        }
    }
    private void resultWrite(){
        FieldExpression year = questionMark();
        FieldExpression week = questionMark();
        FieldExpression month = questionMark();
        FieldExpression day = questionMark();
        FieldExpression hour = questionMark();
        FieldExpression minute = questionMark();
        FieldExpression second = on(0);
        String result="";
        if(!time[4].equals("*")){
            if(time[4].contains("-")){
                week = between(Integer.valueOf(time[4].split("-")[0]),Integer.valueOf(time[4].split("-")[1]));
            }
            else{
                week = on(Integer.valueOf(time[4]));
            }
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            this.response.getSemantic().getSlots().getDatetime().setRepeat(true);
        }
        else if(time[2].equals("day")){
            month = always();
            day = always();
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            this.response.getSemantic().getSlots().getDatetime().setRepeat(true);
        }
        else if(time[2].contains("month")){
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            day = on(Integer.valueOf(time[2].split(" ")[1]));
            month = always();
        }
        else if(!time[2].equals("*")){
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            day = every(Integer.valueOf(time[2]));
            month = always();
        }
        else if(!time[3].equals("*")){
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            day = on(this.dt.getDayOfMonth());
            month = every(Integer.valueOf(time[3]));
            year = always();
        }
        else{
            minute = on(this.dt.getMinuteOfHour());
            hour = on(this.dt.getHourOfDay());
            day = on(this.dt.getDayOfMonth());
            month = on(this.dt.getMonthOfYear());
            year = on(this.dt.getYear());
        }
        Cron cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ))
                .withYear(year)
                .withDoM(day)
                .withMonth(month)
                .withDoW(week)
                .withHour(hour)
                .withMinute(minute)
                .withSecond(second)
                .instance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(!time[2].equals("*") && !time[3].equals("*") && !time[5].equals("*")){
            this.response.getSemantic().getSlots().getDatetime().setDate(sdf.format(new DateTime(Integer.valueOf(time[5]),Integer.valueOf(time[3]),Integer.valueOf(time[2]),0,0).toDate()));
        }
        else{
            this.response.getSemantic().getSlots().getDatetime().setDate(sdf.format(this.dt.toDate()));
        }
        this.response.getSemantic().getSlots().getDatetime().setTime(cron.asString());
        String message = "";
        if(!this.message.equals(null)|| !this.message.equals("")){
            message = getMessage(this.message.trim());
        }
        Pattern p = Pattern.compile("有\\S{1,2}个");
        Matcher m = p.matcher(this.msg);
        if(m.find() && this.response.getOperation().equals("VIEW")){
            this.response.getSemantic().getSlots().getDatetime().setCount(true);
        }
        this.response.getSemantic().setContent(message);
        this.response.setRc(0);
        this.response.setService("schedule");
        this.response.getSemantic().getSlots().getDatetime().setType("DT_BASIC");
        if(this.msg.contains("提醒") || this.msg.contains("提示")){
            this.response.getSemantic().getSlots().setName("reminder");
        }
        else if(this.msg.contains("定时")){
            this.response.getSemantic().getSlots().setName("fix_time");
        }
        else if(this.msg.contains("专注") || this.msg.contains("专心") || this.msg.contains("静下心")){
            this.response.getSemantic().getSlots().setName("concentrate");
        }
        else{
            this.response.getSemantic().getSlots().setName("schedule");
        }
    }
    //get the message
    private String getMessage(String msg){
        String message = "";
        Pattern p = Pattern.compile("\\d{1,2}点钟*");
        Matcher m = p.matcher(msg);
        int i=0;
        while(m.find()){
            if(i==0){
                i++;
                message = msg.substring(m.end());
                continue;
            }
            p = Pattern.compile("(第\\d个)|(下1(次|个))|(最近1(个|次))|(设置)|(提前)|(查看)|(查询)|(删除)|(取消)|(专心)|(专注)|(静下心)|(定时)|(提醒)|(闹钟)|(闹铃)|(叫醒)|(叫)|(提示)|(的)|((给|帮)*我)|(定1个)|(下周)|(下下周)|(每周\\d)|(每天)|(周末)|(工作日)|(周\\d)|(\\d{1,2}月\\d{1,2}(日|号))|(\\d{1,2}(日|号))|(要)|( )");
            m = p.matcher(msg);
            while(m.find()){
                message = message.replace(msg.substring(m.start(),m.end()), " ");
            }
            return message;
        }
        if(advance[0]>0||advance[1]>0){
            p = Pattern.compile("(第\\d个)|(下1(次|个))|(最近1(个|次))|(设置)|(提前)|(半个+小时)|(\\d个+小时\\d{1,2}分钟+)|(\\d{1,2}个+半+小时)|(\\d{1,2}分钟)|(1刻钟*)|(定时)|(提醒)|(闹钟)|(闹铃)|(叫醒)|(叫)|(提示)|(的)|((给|帮)*我)|(定1个)|(下周)|(下下周)|(每周\\d)|(每天)|(周末)|(工作日)|(周\\d)|(\\d{1,2}月\\d{1,2}(日|号))|(\\d{1,2}(日|号))|(要)|( )");
            m = p.matcher(msg);
            message = msg;
            while(m.find()){
                message = message.replace(msg.substring(m.start(),m.end()), " ");
            }
            return message;
        }
        p = Pattern.compile("(第\\d个)|(下1(次|个))|(最近1(个|次))|(设置)|(提前)|(记得)|新建|(通知)|(查看)|(查询)|(删除)|(取消)|(专心)|(专注)|(静下心)|(定时*)|(提醒)|(闹钟)|(闹铃)|(叫醒)|(叫)|(提示)|(的)|((给|帮)*我)|(定*1个+)|(下周\\d+)|(下下周\\d+)|(每周\\d)|(每天)|(周末)|(工作日)|(周\\d)|(\\d{1,2}月\\d{1,2}(日|号))|(\\d{1,2}(日|号))|(下个+月)|(下下个+月)|(明天)|(后天)|(大后天)|(外天)|(今天)|(早上)|(凌晨)|(中午)|(上午)|(下午)|(晚上)|(半夜)|(要)|(\\d{1,2}点\\d{1,2}(分*|刻))|(\\d{1,2}分钟)|(\\d{1,2}点(钟|半))|(\\d{1,2}点)|(1刻钟+)|(\\d{1,2}个*(小时|点)半*)|(((\\d{1,2})|半)个*小时)|(\\d{1,2}分钟+)|(\\D{0,2}后)");
        m = p.matcher(msg);
        message = msg;
        while(m.find()){
            message = message.replace(msg.substring(m.start(),m.end()), " ");
        }
        String[] strings = message.split(" ");
        message="";
        for(int j=0;j<strings.length;j++){
            strings[j] = strings[j].replace(" ", "");
            if(strings[j].length()>1){
                message = message + strings[j];
            }
        }
        return message;
    }

    //deal the select query
    private void selectQuery(String msg){
        Pattern p = Pattern.compile("(下1(次|个))|(最近1(个|次))|(刚刚)|(刚才)|(上\\S{0,1})(个|条)");
        Matcher m = p.matcher(msg);
        if(m.find()){
            response.getSemantic().getSlots().getDatetime().setDuration(0);
            response.getSemantic().getSlots().getDatetime().setIndex(1);
            return;

        }
        int day= getWeek(msg);
        if(day>0){
            String replace = "周"+day;
            if(msg.contains("下下周")){
                day=day+14;
            }
            else if(msg.contains("下周")){
                day=day+7;
            }
            if(day<this.dt.getDayOfWeek()){
                this.response.setOperation("FAILED");
                this.response.setRtext("本周已经没有了");
                day=day+7;
                return;
            }
            else{
                this.dt = this.dt.plusDays(day-this.dt.getDayOfWeek());
            }
            msg = msg.replace(replace, "");
            int[] hour= getHour(msg);
            if(hour[0]>0){
                this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[3],hour[4]);
                response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());

                p = Pattern.compile("第\\d个");
                m = p.matcher(msg);
                if(m.find()){
                    response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1))-48);
                }
                resultWrite();
            }
            else{
                hour = checkSelectTime(hour);
                if(hour[0]==-1){
                    resultWrite();
                    return;
                }
                if(hour[1]>0&&hour[2]>0){
                    if(msg.contains("之前")||msg.contains("以前")){
                        DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                        response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
                    }
                    else if(msg.contains("之后")||msg.contains("以后")){
                        DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.plusDays(1).getDayOfMonth(),0,0);
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                        response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
                    }
                    else{
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    }
                    resultWrite();

                }
                else if(hour[1]>0){
                    if(msg.contains("之前")||msg.contains("以前")){
                        DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                        response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
                    }
                    else if(msg.contains("之后")||msg.contains("以后")){
                        DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.plusDays(1).getDayOfMonth(),0,0);
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                        response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
                    }
                    else{
                        this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                        response.getSemantic().getSlots().getDatetime().setDuration(3600000);
                    }

                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                }
                else if(msg.contains("早上")||msg.contains("上午")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else if(msg.contains("下午")||msg.contains("晚上")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),12,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else{
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(86400000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
            }
            return;
        }

        int[] month = get_month(this.msg);
        int day_change=0;
        if(month[0]>0 || month[1]>0){
            day_change=1;
            this.dt = new DateTime((month[2]>0?month[2]:this.dt.getYear()),(month[0]>0?month[0]:this.dt.getMonthOfYear()),(month[1]>0?month[1]:this.dt.getDayOfMonth()),this.dt.getHourOfDay(),this.dt.getMinuteOfHour());
            if(this.dt.compareTo(new DateTime(new DateTime().getYear(),new DateTime().getMonthOfYear(),new DateTime().getDayOfMonth(),0,0))<0){
                if(month[0]==0){
                    this.dt = this.dt.plusMonths(1);
                }
                else{
                    this.dt = this.dt.plusYears(1);
                }
            }
        }
        int[] hour = getHour(this.msg);
        if(hour[0]>0){
            this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
            DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[3],hour[4]);
            if(this.dt.compareTo(new DateTime())<0 && d.compareTo(new DateTime())>0){
                this.dt = new DateTime();
                response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
            }
            else if(d.compareTo(new DateTime())<0){
                this.dt.plusDays(1);
                response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
            }
            else{
                response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
            }

            p = Pattern.compile("第\\d个");
            m = p.matcher(msg);
            if(m.find()){
                response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
            }
            resultWrite();
            return;
        }
        else{
            hour = checkSelectTime(hour);
            if(hour[0]==-1){
                resultWrite();
                return;
            }
            if(hour[1]>0&&hour[2]>0){
                this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                if(msg.contains("之前")||msg.contains("以前")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                }
                else if(msg.contains("之后")||msg.contains("以后")){
                    DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.plusDays(1).getDayOfMonth(),0,0);
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                }
                else{
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                }
                p = Pattern.compile("第\\d个");
                m = p.matcher(msg);
                if(m.find()){
                    response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                }
                resultWrite();
                return;
            }
            else if(hour[1]>0){
                if(msg.contains("之前")||msg.contains("以前")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(this.dt.getMillis()-d.getMillis());
                }
                else if(msg.contains("之后")||msg.contains("以后")){
                    DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.plusDays(1).getDayOfMonth(),0,0);
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    response.getSemantic().getSlots().getDatetime().setDuration(d.getMillis()-this.dt.getMillis());
                }
                else{
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
                    response.getSemantic().getSlots().getDatetime().setDuration(3600000);
                }
                p = Pattern.compile("第\\d个");
                m = p.matcher(msg);
                if(m.find()){
                    response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                }
                resultWrite();
                return;
            }
            else if(day_change>0){
                if(msg.contains("早上")||msg.contains("上午")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else if(msg.contains("下午")||msg.contains("晚上")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),12,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else{
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(86400000);
                    //this.a.getData().setDuration(86400000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                        //this.a.getData().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
            }
            else if(this.msg.contains("今天")){
                if(msg.contains("早上")||msg.contains("上午")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else if(msg.contains("下午")||msg.contains("晚上")){
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),12,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(43200000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
                else{
                    this.dt = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0);
                    response.getSemantic().getSlots().getDatetime().setDuration(86400000);
                    p = Pattern.compile("第\\d个");
                    m = p.matcher(msg);
                    if(m.find()){
                        response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
                    }
                    resultWrite();
                    return;
                }
            }

        }
        p = Pattern.compile("第\\d个");
        m = p.matcher(msg);
        if(m.find()){
            response.getSemantic().getSlots().getDatetime().setIndex(Integer.valueOf(msg.charAt(m.start()+1)-48));
        }
        else{
            response.getSemantic().getSlots().getDatetime().setIndex(-1);
        }
        resultWrite();
        return;
    }
    //check the select time
    private int[] checkSelectTime(int[] hour){
        if(msg.contains("早上") || msg.contains("上午") || msg.contains("凌晨")){
            if(msg.contains("早上")){
                String dateorig = "早上"+this.response.getSemantic().getSlots().getDatetime().getTimeOrig();
                this.response.getSemantic().getSlots().getDatetime().setTimeOrig(dateorig);
            }if(msg.contains("上午")){
                String dateorig = "上午"+this.response.getSemantic().getSlots().getDatetime().getTimeOrig();
                this.response.getSemantic().getSlots().getDatetime().setTimeOrig(dateorig);
            }if(msg.contains("凌晨")){
                String dateorig = "凌晨"+this.response.getSemantic().getSlots().getDatetime().getTimeOrig();
                this.response.getSemantic().getSlots().getDatetime().setTimeOrig(dateorig);
            }
        }
        DateTime dat = new DateTime();
        if((new DateTime(dat.getYear(),dat.getMonthOfYear(),dat.getDayOfMonth(),0,0)).compareTo(new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),0,0))>0){
            this.response.setOperation("FAILED");
            this.response.setRtext("无法回到过去");
            hour[0]=-1;
            return hour;
        }
        if(hour[1]>0 || hour[2]>0){
            DateTime d = new DateTime(this.dt.getYear(),this.dt.getMonthOfYear(),this.dt.getDayOfMonth(),hour[1],hour[2]);
            if(d.compareTo(dat)<0 && time[4].equals("*")&&time[2].equals("*")){
                DateTime dt0 = d;
                if(d.getHourOfDay()<12){
                    dt0 = d.plusHours(12);
                }
                if(dt0.compareTo(dat)>0 && !msg.contains("早上") && !msg.contains("上午") && !msg.contains("凌晨")){
                    this.dt = dt0;
                    hour[1]=(hour[1]<12?hour[1]+12:hour[1]);
                    return hour;
                }
                else{
                    if(this.msg.contains("今天")){
                        this.response.setOperation("FAILED");
                        this.response.setRtext("今天已经没有这个时间了");
                        hour[0]=-1;
                        return hour;
                    }
                    else if(this.msg.contains("上午")||this.msg.contains("早上")||this.msg.contains("凌晨")){
                        this.dt = this.dt.plusDays(1);
                        return hour;
                    }
                    else if(this.msg.contains("下午")||this.msg.contains("晚上")){
                        this.dt = this.dt.plusDays(1);
                        return hour;
                    }
                    else{
                        this.dt = this.dt.plusDays(1);
                        return hour;
                    }
                }
            }
            if(this.tomorrow>0 && (new DateTime().getHourOfDay()<4)){
                String speech = "现在是"+(new DateTime().getDayOfMonth())+"号。请问是"+this.dt.getDayOfMonth()+"号，还是"+this.dt.plusDays(-1).getDayOfMonth();
                this.response.setRtext(speech);
                hour[0]=-1;
                return hour;
            }
            if(hour[1]<12 && (msg.contains("下午") || msg.contains("晚上") || msg.contains("傍晚"))){
                this.dt = this.dt.plusHours(12);
                hour[1]=hour[1]+12;
            }
            else if(hour[1]<12 && !msg.contains("早上") && !msg.contains("上午") && !msg.contains("凌晨")){
               /* this.response.setRtext("请问是上午还是下午");
                hour[0]=-1;*/
                return hour;

            }
            for(int i=0;i<hour.length;i++){
            }
        }
        return hour;
    }


}

