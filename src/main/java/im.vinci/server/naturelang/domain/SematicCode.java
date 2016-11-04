package im.vinci.server.naturelang.domain;

public class SematicCode {
	public enum CodeEnum {
		answer,
		music, music_song,music_album,music_recommend,//虾米排行版的返回type直接使用需求定义的type，不在此处体现
		xmly,xmly_track,xmly_album,xmly_recommend,
		machine_instruct,music_mood,music_genre,music_scene,music_artist,//此列为国际版专用type，国内版请勿使用
        translate
		;
	}
}


