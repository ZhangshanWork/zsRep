package im.vinci.server.naturelang.domain;

import im.vinci.server.search.domain.himalayas.GetHimalayaAlbumDetailResponse;
import im.vinci.server.search.domain.himalayas.GetHimalayaRecommendTrackInCategoryResponse;
import im.vinci.server.search.domain.himalayas.QueryHimalayaTrackByKeywordResponse;
import im.vinci.server.search.domain.music.MusicAlbum;
import im.vinci.server.search.domain.music.MusicSong;
import im.vinci.server.utils.apiresp.ResponsePageVo;

import java.io.Serializable;
import java.util.List;

public class UnderstandModel implements Serializable{
	private String code;
	private String text;   //语言反馈
	private String from; //来源-spotify，soundcloud，alexa（仅限国际版使用）

	private AnswerSemantic answer;
	//机器指令识别带回结果 --仅供国际版使用
	private InstructSemantic instructSemantic;
	private List<String> moodSongs;
	//喜马拉雅的识别和回带结果
	private XMLYSemantic xmlySemantic;
	private QueryHimalayaTrackByKeywordResponse himalayaTrackByKeyword;
	private GetHimalayaAlbumDetailResponse himalayaAlbumDetial;
	private GetHimalayaRecommendTrackInCategoryResponse recommendTrack;

	//虾米音乐的识别和回带结果
	private MusicSemantic musicSemantic;
	private ResponsePageVo<MusicSong> musicSong;
	private ResponsePageVo<MusicAlbum> musicAlbum;
	private MusicAlbum album;
	
	public MusicAlbum getAlbum() {
		return album;
	}
	public void setAlbum(MusicAlbum album) {
		this.album = album;
	}
	public GetHimalayaRecommendTrackInCategoryResponse getRecommendTrack() {
		return recommendTrack;
	}
	public void setRecommendTrack(GetHimalayaRecommendTrackInCategoryResponse recommendTrack) {
		this.recommendTrack = recommendTrack;
	}
	public ResponsePageVo<MusicSong> getMusicSong() {
		return musicSong;
	}
	public void setMusicSong(ResponsePageVo<MusicSong> musicSong) {
		this.musicSong = musicSong;
	}
	public ResponsePageVo<MusicAlbum> getMusicAlbum() {
		return musicAlbum;
	}
	public void setMusicAlbum(ResponsePageVo<MusicAlbum> musicAlbum) {
		this.musicAlbum = musicAlbum;
	}
	public QueryHimalayaTrackByKeywordResponse getHimalayaTrackByKeyword() {
		return himalayaTrackByKeyword;
	}
	public void setHimalayaTrackByKeyword(QueryHimalayaTrackByKeywordResponse himalayaTrackByKeyword) {
		this.himalayaTrackByKeyword = himalayaTrackByKeyword;
	}
	public GetHimalayaAlbumDetailResponse getHimalayaAlbumDetial() {
		return himalayaAlbumDetial;
	}
	public void setHimalayaAlbumDetial(GetHimalayaAlbumDetailResponse himalayaAlbumDetial) {
		this.himalayaAlbumDetial = himalayaAlbumDetial;
	}
	
	public XMLYSemantic getXmlySemantic() {
		return xmlySemantic;
	}
	public void setXmlySemantic(XMLYSemantic xmlySemantic) {
		this.xmlySemantic = xmlySemantic;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public MusicSemantic getMusicSemantic() {
		return musicSemantic;
	}
	public void setMusicSemantic(MusicSemantic musicSemantic) {
		this.musicSemantic = musicSemantic;
	}
	public AnswerSemantic getAnswer() {
		return answer;
	}
	public void setAnswer(AnswerSemantic answer) {
		this.answer = answer;
	}
	public InstructSemantic getInstructSemantic() {
		return instructSemantic;
	}

	public void setInstructSemantic(InstructSemantic instructSemantic) {
		this.instructSemantic = instructSemantic;
	}

	public List<String> getMoodSongs() {
		return moodSongs;
	}

	public void setMoodSongs(List<String> moodSongs) {
		this.moodSongs = moodSongs;
	}
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
}
