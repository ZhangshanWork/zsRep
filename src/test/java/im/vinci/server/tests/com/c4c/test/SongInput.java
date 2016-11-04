package im.vinci.server.tests.com.c4c.test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.xml.FilterBuilder;
import org.apache.lucene.queryparser.xml.builders.TermsFilterBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.support.ValuesSource.Bytes;
import org.elasticsearch.index.query.QueryBuilders.*;
import org.junit.After;
import org.junit.Before;

/**
 * @author cuicunjin
 *
 */

public class SongInput{
	
	private String song_name;
	private String song_style;
	private String singer_name;
	private String singer_eng_name;
	private String album_name;
	private String album_des;
	private String song_source;
	private long song_id;
	private long singer_id;
	private long album_id;
	private long original_id;
	private long type_id;
	private long play_count;
	private long length;

	public void setsong_name(String song_name){
		this.song_name = song_name;
	}
	public String getsong_name(){
		return song_name;
	}
	
	public void setsong_style(String song_style){
		this.song_style = song_style;
	}
	public String getsong_style(){
		return song_style;
	}
	
	public void setsinger_name(String singer_name){
		this.singer_name = singer_name;
	}
	public String getsinger_name(){
		return singer_name;
	}
	
	public void setsinger_eng_name(String singer_eng_name){
		this.singer_eng_name = singer_eng_name;
	}
	public String getsinger_eng_name(){
		return singer_eng_name;
	}
	
	public void setalbum_name(String album_name){
		this.album_name = album_name;
	}
	public String getalbum_name(){
		return album_name;
	}
	
	public void setalbum_des(String album_des){
		this.album_des = album_des;
	}
	public String getalbum_des(){
		return album_des;
	}
	
	public void setsong_source(String song_source){
		this.song_source = song_source;
	}
	public String getsong_source(){
		return song_source;
	}
	
	public void setsong_id(long song_id){
		this.song_id = song_id;
	}
	public long getsong_id(){
		return song_id;
	}
	
	public void setsinger_id(long singer_id){
		this.singer_id = singer_id;
	}
	public long getsinger_id(){
		return singer_id;
	}
	
	public void setalbum_id(long album_id){
		this.album_id = album_id;
	}
	public long getalbum_id(){
		return album_id;
	}
	
	public void setoriginal_id(long original_id){
		this.original_id = original_id;
	}
	public long getoriginal_id(){
		return original_id;
	}
	
	public void settype_id(long type_id){
		this.type_id = type_id;
	}
	public long gettype_id(){
		return type_id;
	}
	
	public void setplay_count(long play_count){
		this.play_count = play_count;
	}
	public long getplay_count(){
		return play_count;
	}
	
	public void setlength(long length){
		this.length = length;
	}
	public long getlength(){
		return length;
	}
}

