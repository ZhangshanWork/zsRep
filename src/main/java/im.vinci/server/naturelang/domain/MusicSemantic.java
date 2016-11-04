package im.vinci.server.naturelang.domain;

import java.io.Serializable;

public class MusicSemantic implements Serializable {
	private String song;
	private String artist;
	private String album;
	private String genre;
	private String billboard;
	private String keyword;
	private String rank;
	private String scene;

	public String getScene() {
		return scene;
	}

	public void setScene(String scene) {
		this.scene = scene;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	public String getAlbum() {
		return album;
	}

	public String getArtist() {
		return artist;
	}

	public String getBillboard() {
		return billboard;
	}

	public String getGenre() {
		return genre;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getSong() {
		return song;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public void setBillboard(String billboard) {
		this.billboard = billboard;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public void setSong(String song) {
		this.song = song;
	}

}
