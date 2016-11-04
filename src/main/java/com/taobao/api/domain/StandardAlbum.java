package com.taobao.api.domain;

import com.taobao.api.internal.mapping.ApiField;
import com.taobao.api.TaobaoObject;


/**
 * 专辑类
 *
 * @author top auto create
 * @since 1.0, null
 */
public class StandardAlbum extends TaobaoObject {

	private static final long serialVersionUID = 3127364371274957697L;

	/**
	 * 专辑ID
	 */
	@ApiField("album_id")
	private Long albumId;

	/**
	 * 专辑名
	 */
	@ApiField("album_name")
	private String albumName;

	/**
	 * 艺人ID
	 */
	@ApiField("artist_id")
	private Long artistId;

	/**
	 * 艺人名
	 */
	@ApiField("artist_name")
	private String artistName;

	/**
	 * 类型
	 */
	@ApiField("category")
	private Long category;

	/**
	 * CD碟数
	 */
	@ApiField("cd_count")
	private Long cdCount;

	/**
	 * 收藏数
	 */
	@ApiField("collects")
	private Long collects;

	/**
	 * 发行公司
	 */
	@ApiField("company")
	private String company;

	/**
	 * 专辑介绍
	 */
	@ApiField("description")
	private String description;

	/**
	 * 发布的时间戳
	 */
	@ApiField("gmt_publish")
	private Long gmtPublish;

	/**
	 * 专辑评分
	 */
	@ApiField("grade")
	private Long grade;

	/**
	 * 上下架信息(3为下架 禁止播放)
	 */
	@ApiField("is_check")
	private Long isCheck;

	/**
	 * 语言类型
	 */
	@ApiField("language")
	private String language;

	/**
	 * 专辑LOGO
	 */
	@ApiField("logo")
	private String logo;

	/**
	 * 推荐数
	 */
	@ApiField("recommends")
	private Long recommends;

	/**
	 * 专辑包含歌曲数目
	 */
	@ApiField("song_count")
	private Long songCount;

	/**
	 * 专辑别名
	 */
	@ApiField("sub_title")
	private String subTitle;

	/**
	 * 播放次数
	 */
	@ApiField("play_count")
	private Long playCount;


	public Long getAlbumId() {
		return this.albumId;
	}
	public void setAlbumId(Long albumId) {
		this.albumId = albumId;
	}

	public String getAlbumName() {
		return this.albumName;
	}
	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public Long getArtistId() {
		return this.artistId;
	}
	public void setArtistId(Long artistId) {
		this.artistId = artistId;
	}

	public String getArtistName() {
		return this.artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	public Long getCategory() {
		return this.category;
	}
	public void setCategory(Long category) {
		this.category = category;
	}

	public Long getCdCount() {
		return this.cdCount;
	}
	public void setCdCount(Long cdCount) {
		this.cdCount = cdCount;
	}

	public Long getCollects() {
		return this.collects;
	}
	public void setCollects(Long collects) {
		this.collects = collects;
	}

	public String getCompany() {
		return this.company;
	}
	public void setCompany(String company) {
		this.company = company;
	}

	public String getDescription() {
		return this.description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public Long getGmtPublish() {
		return this.gmtPublish;
	}
	public void setGmtPublish(Long gmtPublish) {
		this.gmtPublish = gmtPublish;
	}

	public Long getGrade() {
		return this.grade;
	}
	public void setGrade(Long grade) {
		this.grade = grade;
	}

	public Long getIsCheck() {
		return this.isCheck;
	}
	public void setIsCheck(Long isCheck) {
		this.isCheck = isCheck;
	}

	public String getLanguage() {
		return this.language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLogo() {
		return this.logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}

	public Long getRecommends() {
		return this.recommends;
	}
	public void setRecommends(Long recommends) {
		this.recommends = recommends;
	}

	public Long getSongCount() {
		return this.songCount;
	}
	public void setSongCount(Long songCount) {
		this.songCount = songCount;
	}

	public String getSubTitle() {
		return this.subTitle;
	}
	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public Long getPlayCount() {
		return playCount;
	}

	public void setPlayCount(Long playCount) {
		this.playCount = playCount;
	}
}
