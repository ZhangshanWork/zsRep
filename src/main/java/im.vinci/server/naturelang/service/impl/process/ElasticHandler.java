package im.vinci.server.naturelang.service.impl.process;

import org.springframework.core.io.ClassPathResource;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ElasticHandler {
	//用来读取词典，dict_name为读入词典的文件名
	public Map<String,String> readDict(String dic_name) throws IOException
	{
		Map<String,String> dic = new HashMap<>();
		BufferedReader Reader = null;
		Reader = new BufferedReader(new InputStreamReader(new ClassPathResource("nlp/"+dic_name).getInputStream(), "utf8"));
		String line = null;
		while ((line = Reader.readLine()) != null) {
				dic.put(line.trim().toLowerCase(),"");
		}
		return dic;
	}

	//用于分词,str为待分词的文本，useSmart = true表示smart模式,返回token组成的ArrayList<String>
	public  ArrayList<String> cutNation(String str,boolean useSmart)
	{
		ArrayList<String> list = new ArrayList<String>();
		Configuration cfg  = DefaultConfig.getInstance();
		cfg.setUseSmart(useSmart);
		StringReader reader=new StringReader(str);
		IKSegmenter seg = new IKSegmenter(reader,cfg);
		Lexeme lex = null;
		try {
			while((lex = seg.next()) != null) {
				list.add(lex.getLexemeText());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return list;
	}

	//用于分词,str为待分词的文本，useSmart = true表示smart模式,返回token组成的ArrayList<String>
	public  ArrayList<String> cut(String str,boolean useSmart)
	{
		ArrayList<String> list = new ArrayList<String>();
		Configuration cfg  = DefaultConfig.getInstance();
		cfg.setUseSmart(useSmart);
		StringReader reader=new StringReader(str);
		IKSegmenter seg = new IKSegmenter(reader,cfg);
		Lexeme lex = null;
		try {
			while((lex = seg.next()) != null) {
				list.add(lex.getLexemeText());
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return list;
	}
	//用于去除前缀，tokens为分词之后得到的token的arraylist，dict_name为前缀词典的文件名
	public  float[] remove_prefix(ArrayList<String> tokens,String dict_name,float[] score_array) throws IOException
	{
		score_array[0]= 16;
		score_array[1] = 8;
		score_array[2] = 32;
		//对应singer_score,album_score,song_score;
		Map<String,Float> singer_dic = new HashMap<>();
		Map<String,Float> album_dic = new HashMap<>();
		Map<String,Float> song_dic = new HashMap<>();
		Map<String,String> dic = readDict(dict_name);

		//singer_dic.put("的歌", 4.0f);
		//album_dic.put("的歌", 2.0f);
		//singer_dic.put("的歌曲", 4.0f);//增加
		//album_dic.put("的歌曲", 2.0f);//增加
		//singer_dic.put("播放", 4.0f);//改动，原为song_dic
		////		song_dic.put("播放", 4.0f);//改动，mlc
		//song_dic.put("Play", 4.0f);
		//song_dic.put("放一首", 4.0f);
		//song_dic.put("推荐", 0.25f);
		//song_dic.put("一些", 0.25f);
        if(tokens.contains("专辑"))
        	score_array[1]*=4;
        if(tokens.size()<=1)
        	return score_array;
        String prefix=tokens.get(0)+tokens.get(1);
        if((tokens.size()>=2)&&(dic.containsKey(prefix)))
		{
			if(singer_dic.containsKey(prefix))
        		score_array[0]=(score_array[0]*singer_dic.get(prefix));
        	if(album_dic.containsKey(prefix))
        		score_array[1]=(score_array[1]*album_dic.get(prefix));
        	if(song_dic.containsKey(prefix))
        		score_array[2]=(score_array[2]*song_dic.get(prefix));
        	tokens.remove(0);
        	tokens.remove(0);
		}
        else
        {
        	prefix= tokens.get(0);
	        if(dic.containsKey(prefix))
	        {
	        	//System.out.println(prefix);
	        	if(singer_dic.containsKey(prefix))
	        		score_array[0]=(score_array[0]*singer_dic.get(prefix));
	        	if(album_dic.containsKey(prefix))
	        		score_array[1]=(score_array[1]*album_dic.get(prefix));
	        	if(song_dic.containsKey(prefix))
	        		score_array[2]=(score_array[2]*song_dic.get(prefix));
	        	tokens.remove(0);
	        }
        }
        return score_array;
	}
	//去除后缀，tokens为分词之后得到的token的arraylist，dict_name为前缀词典的文件名
	public  float[] remove_suffix(ArrayList<String> tokens,String dict_name,float[] score_array) throws IOException
	{
		score_array[0]= score_array[1] = score_array[2] = 1;
		//对应singer_score,album_score,song_score;
		
		Map<String,Float> singer_dic = new HashMap<>();
		Map<String,Float> album_dic = new HashMap<>();
		Map<String,Float> song_dic = new HashMap<>();
		Map<String,String> dic = readDict(dict_name);

		//singer_dic.put("的歌", 4.0f);
		//album_dic.put("的歌", 2.0f);
		//singer_dic.put("的歌曲", 4.0f);//增加
		//album_dic.put("的歌曲", 2.0f);//增加
		//singer_dic.put("播放", 4.0f);//改动，原为song_dic
		//song_dic.put("Play", 4.0f);
		//song_dic.put("推荐", 0.25f);
		//song_dic.put("一些", 0.25f);
        if(tokens.size()<=1)
        	return score_array;
        int size = tokens.size();
        String suffix = tokens.get(size-2)+tokens.get(size-1);
        if((tokens.size()>=2) && (dic.containsKey(suffix)))
		{
    		if(singer_dic.containsKey(suffix))
        		score_array[0]=(score_array[0]*singer_dic.get(suffix));
        	if(album_dic.containsKey(suffix))
        		score_array[1]=(score_array[1]*album_dic.get(suffix));
        	if(song_dic.containsKey(suffix))
        		score_array[2]=(score_array[2]*song_dic.get(suffix));
        	tokens.remove(tokens.size()-1);
        	tokens.remove(tokens.size()-1);
		}
        else
        {
        	suffix = tokens.get(size-1);
	        if(dic.containsKey(suffix))
	        {
	        	if(singer_dic.containsKey(suffix))
	        		score_array[0]=(score_array[0]*singer_dic.get(suffix));
	        	if(album_dic.containsKey(suffix))
	        		score_array[1]=(score_array[1]*album_dic.get(suffix));
	        	if(song_dic.containsKey(suffix))
	        		score_array[2]=(score_array[2]*song_dic.get(suffix));
	        	tokens.remove(tokens.size()-1);
	        }
        }
        return score_array;
	}
	//用来用于去除字符串中的无用符号,dict_name为词典的文件名
	public void filter(String str,String dict_name) throws IOException
	{
		Map<String,String> dic = readDict(dict_name);
		Iterator iter = dic.entrySet().iterator();	
		while(iter.hasNext())
		{
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			str = str.replaceAll(key, "");	
		}
	}
	//用来判断
}
