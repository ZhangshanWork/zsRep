package im.vinci.server.naturelang.domain;

import im.vinci.server.search.domain.music.MusicSong;

import java.io.Serializable;
import java.util.List;

//曲风流派
public class Genre implements Serializable{
	private String playlistname;
	private List<MusicSong> musics;
	private List<String> tags;

	public String getPlaylistname() {
		return playlistname;
	}

	public void setPlaylistname(String playlistname) {
		this.playlistname = playlistname;
	}

	public List<MusicSong> getMusics() {
		return musics;
	}

	public void setMusics(List<MusicSong> musics) {
		this.musics = musics;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
