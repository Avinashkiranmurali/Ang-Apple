package com.b2s.rewards.apple.model;

import javax.persistence.*;



@Entity
@Table(name="whitelist_word")
public class WhiteListWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "locale")
    private String locale;

    @Column(name = "word")
    private String word;

    @Column(name = "pattern")
    private String pattern;

    @Column(name = "match_whole_word")
    private Integer matchWholeWord;

    @Column(name="language")
    private String language;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public Integer getMatchWholeWord() {
        return matchWholeWord;
    }

    public void setMatchWholeWord(Integer matchWholeWord) {
        this.matchWholeWord = matchWholeWord;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}