package im.vinci.server.naturelang.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.cloud.speech.*;
import im.vinci.server.naturelang.domain.ServiceRet;
import im.vinci.server.naturelang.domain.XunfeiModel;
import im.vinci.server.naturelang.utils.ObjectUtils;
import im.vinci.server.utils.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
@Service
public class XunFeiSearchService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    // 设置科大讯飞的接入appid
    private final String APPID = "5682479d";
    
    @PostConstruct
    public void init() {
        //在应用发布版本中，请勿显示日志，详情见此函数说明。
//      com.iflytek.cloud.speech.Setting.setSaveTestLog(true);
//      com.iflytek.cloud.speech.Setting.setShowLog(true);
        SpeechUtility.createUtility("appid=" + APPID);
                
    }

    public XunfeiModel getXunFeiResult(String str) throws Exception {
         //创建文本语义理解对象
        //SpeechUtility.createUtility("appid=" + APPID);
        TextUnderstander mTextUnderstander = TextUnderstander.createTextUnderstander();
        log.info("i am comming");
        final XunfeiModel xunf = new XunfeiModel();
        final CountDownLatch cd = new CountDownLatch(1);
        //初始化监听器
        TextUnderstanderListener searchListener = new TextUnderstanderListener(){
        //语义结果回调
            public void onResult(UnderstanderResult result){
                log.info("#################this is  xunfei: "+result.getResultString());
                if(StringUtils.isNotBlank(result.getResultString())){
                    JSONObject json = (JSONObject) JSON.parse(result.getResultString());
                    log.info(json.getString("service") + "------" + json.getString("operation"));
                    if(StringUtils.isNotBlank(json.getString("service"))//只有返回结果为openQAQ时处理
                            &&("ANSWER".equals(json.getString("operation"))
                    )){
                        xunf.setFlag("answer");
                        xunf.setText(((JSONObject)json.get("answer")).getString("text"));
                        log.info(xunf.getText());
                    }else if (StringUtils.isNotBlank(json.getString("service"))//只有返回结果为openQAQ时处理
                            &&("weather".equals(json.getString("service"))
                    )) {

                        String nowDate = json.getJSONObject("semantic").getJSONObject("slots").getJSONObject("datetime").getString("date");
                        if(nowDate.equalsIgnoreCase("CURRENT_DAY")){
                            nowDate = DateUtils.today();
                        }
                        JSONObject jsonObject = json.getJSONObject("data").getJSONArray("result").getJSONObject(0);
                        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("result");
                        for(int i=0;i<jsonArray.size();i++) {
                            //如果存在时间此时间重合
                             if(nowDate.equals(jsonArray.getJSONObject(i).getString("date"))){
                                jsonObject = jsonArray.getJSONObject(i);
                                 break;
                            }
                        }
                        String answer = "";
                        if(ObjectUtils.isNotEmperty(jsonObject.get("province"))){
                            answer = answer + jsonObject.getString("province");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("city"))){
                            answer = answer + jsonObject.getString("city");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("weather"))){
                            answer = answer + " 天气：" +jsonObject.getString("weather");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("tempRange"))){
                            answer = answer + " 温度：" +jsonObject.getString("tempRange");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("wind"))){
                            answer = answer + jsonObject.getString("wind");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("windLevel"))){
                            answer = answer + " 风力：" +jsonObject.getString("windLevel");
                        }
                        if (StringUtils.isNotBlank(answer)&&ObjectUtils.isNotEmperty(jsonObject.get("lastUpdateTime"))) {
                            answer = " 日期: " + jsonObject.getString("date") + "," + answer;
                        }
                        xunf.setFlag(json.getString("service"));
                        xunf.setText(answer);
                    }else if(StringUtils.isNotBlank(json.getString("service"))//只有返回结果为openQAQ时处理
                            &&("pm25".equals(json.getString("service")))){
                        JSONObject jsonObject = json.getJSONObject("data").getJSONArray("result").getJSONObject(0);
                        String answer = "";
                        if(ObjectUtils.isNotEmperty(jsonObject.get("area"))){
                            answer = answer + " 地区：" +jsonObject.getString("area");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("pm25"))){
                            answer = answer + " pm2.5：" +jsonObject.getString("pm25");
                        }
                        if(ObjectUtils.isNotEmperty(jsonObject.get("quality"))){
                            answer = answer + " 空气质量：" +jsonObject.getString("quality");
                        }
                        if (StringUtils.isNotBlank(answer)&&ObjectUtils.isNotEmperty(jsonObject.get("publishDateTime"))) {
                            answer = " 信息发布时间: " + jsonObject.getString("publishDateTime") + "," + answer;
                        }
                        xunf.setFlag(json.getString("service"));
                        xunf.setText(answer);
                    }else if(StringUtils.isNotBlank(json.getString("service"))&&("TRANSLATION".equalsIgnoreCase(json.getString("service")))){
                        JSONObject jsonObject = json.getJSONObject("semantic").getJSONObject("slots");
                        xunf.setFlag(json.getString("service"));
                        xunf.setText(jsonObject.getString("content"));
                    }
                }
                cd.countDown();
            }  
            //语义错误回调  
            public void onError(SpeechError error) {
                log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
                cd.countDown();
            }  
        };  
        //开始语义理解  
        mTextUnderstander.understandText(str, searchListener);
        cd.await(1, TimeUnit.SECONDS);
        log.info("end---");
        return xunf;
    }

    public ServiceRet getXunFeiResultV2(String str) throws Exception {
        //创建文本语义理解对象
        //SpeechUtility.createUtility("appid=" + APPID);
        ServiceRet serviceRet = new ServiceRet();
        TextUnderstander mTextUnderstander = TextUnderstander.createTextUnderstander();
        log.info("i am comming");
        final CountDownLatch cd = new CountDownLatch(1);
        //初始化监听器
        TextUnderstanderListener searchListener = new TextUnderstanderListener(){
            //语义结果回调
            public void onResult(UnderstanderResult result){
                log.info("#################this is  xunfei: "+result.getResultString());
                if(StringUtils.isNotBlank(result.getResultString())){
                    JSONObject json = (JSONObject) JSON.parse(result.getResultString());
                    serviceRet.setRc(0);
                    serviceRet.setService(json.getString("service"));
                    serviceRet.setOperation(json.getString("operation"));
                    serviceRet.setBody((JSONObject) JSON.parse(result.getResultString()));
                    log.info(json.getString("service") + "------" + json.getString("operation"));

                }
                cd.countDown();
            }
            //语义错误回调
            public void onError(SpeechError error) {
                serviceRet.setRc(4);
                log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
                cd.countDown();
            }
        };
        //开始语义理解
        mTextUnderstander.understandText(str, searchListener);
        cd.await(1, TimeUnit.SECONDS);
        log.info("end---");
        return serviceRet;
    }


    public Map reponseXunfei(String str) throws Exception {
        //创建文本语义理解对象
        //SpeechUtility.createUtility("appid=" + APPID);
        Map map = new HashMap<String, JSONObject>();
        TextUnderstander mTextUnderstander = TextUnderstander.createTextUnderstander();
        log.info("i am comming");
        final CountDownLatch cd = new CountDownLatch(1);
        //初始化监听器
        TextUnderstanderListener searchListener = new TextUnderstanderListener(){
            //语义结果回调
            public void onResult(UnderstanderResult result){
                log.info("#################this is  xunfei: "+result.getResultString());
                JSONObject jsonObject = (JSONObject) JSON.parse(result.getResultString());
                map.put("result", jsonObject);
                log.info(jsonObject.getString("service") + "------" + jsonObject.getString("operation"));
                cd.countDown();
            }
            //语义错误回调
            public void onError(SpeechError error) {
                log.info("&&&&&&&&&&&&&&&&&&&&&&&&&&&&"+error.getErrorCode()+" "+error.getErrorDesc());
                cd.countDown();
            }
        };
        //开始语义理解
        mTextUnderstander.understandText(str, searchListener);
        cd.await(1, TimeUnit.SECONDS);
        log.info("end---");
        return map;
    }
   /* public static void main(String[] args) throws Exception {
        new XunFeiSearchService().getXunFeiResult("adele");
    }*/

}
