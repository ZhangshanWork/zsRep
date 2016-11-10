package im.vinci.server.naturelang.domain;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.List;

/**
 * Created by mlc on 2016/3/9.
 */
public class Mood {
    private String mood;
    private CaseInsensitiveMap<String,String> special_word;
    private String answer;
    private List<String> keywords;
    private List<String> songs;

    public String getMood() {
        return mood;
    }

    public Mood setMood(String mood) {
        this.mood = mood;
        return this;
    }

    public CaseInsensitiveMap<String, String> getSpecial_word() {
        return special_word;
    }

    public Mood setSpecial_word(CaseInsensitiveMap<String, String> special_word) {
        this.special_word = special_word;
        return this;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Mood setKeywords(List<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    public List<String> getSongs() {
        return songs;
    }

    public Mood setSongs(List<String> songs) {
        this.songs = songs;
        return this;
    }

    public String getAnswer() {
        return answer;
    }

    public Mood setAnswer(String answer) {
        this.answer = answer;
        return this;
    }
}
