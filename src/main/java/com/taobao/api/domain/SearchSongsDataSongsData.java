package com.taobao.api.domain;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;


/**
 * 歌曲搜索结果集合（最小单元）
 *
 * @author top auto create
 * @since 1.0, null
 */
public class SearchSongsDataSongsData extends TaobaoObject {

	private static final long serialVersionUID = 1539472317978142376L;

	/**
	 * 专辑ID
	 */
	@ApiField("album_id")
	private Long albumId;

	/**
	 * 专辑LOGO
	 */
	@ApiField("album_logo")
	private String albumLogo;

	/**
	 * 专辑名称
	 */
	@ApiField("album_name")
	private String albumName;

	/**
	 * 专辑别名
	 */
	@ApiField("album_sub_title")
	private String albumSubTitle;

	/**
	 * 艺人ID
	 */
	@ApiField("artist_id")
	private Long artistId;

	/**
	 * 艺人头像LOGO
	 */
	@ApiField("artist_logo")
	private String artistLogo;

	/**
	 * 艺人别名
	 */
	@ApiField("artist_name")
	private String artistName;
	/**
	 * 艺人别名
	 */
	@ApiField("artist_sub_title")
	private String artistSubTitle;

	/**
	 * CD 序号
	 */
	@ApiField("cd_serial")
	private Long cdSerial;

	/**
	 * 是否音乐人demo (0,否,1,是）
	 */
	@ApiField("demo")
	private Long demo;

	/**
	 * doc_id
	 */
	@ApiField("doc_id")
	private Long docId;

	/**
	 * 是否可以播放
	 */
	@ApiField("is_play")
	private Long isPlay;

	@ApiField("listen_file")
	private String listenFile;

	/**
	 * 歌词URL
	 */
	@ApiField("lyric")
	private String lyricFile;

	/**
	 * 播放次数
	 */
	@ApiField("play_counts")
	private Long playCounts;

	/**
	 * 推荐指数
	 */
	@ApiField("recommends")
	private Long recommends;

	/**
	 * 歌手名
	 */
	@ApiField("singer")
	private String singer;

	/**
	 * 歌曲ID
	 */
	@ApiField("song_id")
	private Long songId;

	/**
	 * 歌曲名
	 */
	@ApiField("song_name")
	private String songName;

	/**
	 * 歌曲别名
	 */
	@ApiField("sub_title")
	private String subTitle;

	/**
	 * weight
	 */
	@ApiField("weight")
	private Long weight;

	@ApiField("length")
	private Long playSeconds;

	public Long getAlbumId() {
		return this.albumId;
	}
	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}

	public String getAlbumLogo() {
		return this.albumLogo;
	}
	public void setAlbumLogo(String albumLogo) {
		this.albumLogo = albumLogo;
	}

	public String getAlbumName() {
		return this.albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getAlbumSubTitle() {
		return this.albumSubTitle;
	}
	public void setAlbumSubTitle(String albumSubTitle) {
		this.albumSubTitle = albumSubTitle;
	}

	public Long getArtistId() {
		return this.artistId;
	}
	public void setArtistId(Long artistId) {
		this.artistId = artistId;
	}

	public String getArtistLogo() {
		return this.artistLogo;
	}
	public void setArtistLogo(String artistLogo) {
		this.artistLogo = artistLogo;
	}

	public String getArtistSubTitle() {
		return this.artistSubTitle;
	}
	public void setArtistSubTitle(String artistSubTitle) {
		this.artistSubTitle = artistSubTitle;
	}

	public Long getCdSerial() {
		return this.cdSerial;
	}
	public void setCdSerial(Long cdSerial) {
		this.cdSerial = cdSerial;
	}

	public Long getDemo() {
		return this.demo;
	}
	public void setDemo(Long demo) {
		this.demo = demo;
	}

	public Long getDocId() {
		return this.docId;
	}
	public void setDocId(Long docId) {
		this.docId = docId;
	}

	public Long getIsPlay() {
		return this.isPlay;
	}
	public void setIsPlay(Long isPlay) {
		this.isPlay = isPlay;
	}

	public Long getPlayCounts() {
		return this.playCounts;
	}
	public void setPlayCounts(Long playCounts) {
		this.playCounts = playCounts;
	}

	public Long getRecommends() {
		return this.recommends;
	}
	public void setRecommends(Long recommends) {
		this.recommends = recommends;
	}

	public String getSinger() {
		return this.singer;
	}
	public void setSinger(String singer) {
		this.singer = singer;
	}

	public Long getSongId() {
		return this.songId;
	}
	public void setSongId(Long songId) {
		this.songId = songId;
	}

	public String getSongName() {
		return this.songName;
	}
	public void setSongName(String songName) {
		this.songName = songName;
	}

	public String getSubTitle() {
		return this.subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public Long getWeight() {
		return this.weight;
	}
	public void setWeight(Long weight) {
		this.weight = weight;
	}

	public String getArtistName() {
		return artistName;
	}

	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public Long getPlaySeconds() {
		return playSeconds;
	}

	public void setPlaySeconds(Long playSeconds) {
		this.playSeconds = playSeconds;
	}

	public String getListenFile() {
		return listenFile;
	}

	public void setListenFile(String listenFile) {
		this.listenFile = listenFile;
	}

	public String getLyricFile() {
		return lyricFile;
	}

	public void setLyricFile(String lyricFile) {
		this.lyricFile = lyricFile;
	}
}
