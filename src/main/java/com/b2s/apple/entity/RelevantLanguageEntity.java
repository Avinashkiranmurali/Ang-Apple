package com.b2s.apple.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author rjesuraj Date : 11/4/2019 Time : 5:25 PM
 */
@Entity
@Table(name = "relevant_language")
public class RelevantLanguageEntity {

    @Id
    @Column( name = "id")
    private Long id;

    @Column( name = "locale")
    private String locale;

    @Column( name = "relevant_language")
    private String relevantLanguage;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(final String locale) {
        this.locale = locale;
    }

    public String getRelevantLanguage() {
        return relevantLanguage;
    }

    public void setRelevantLanguage(final String relevantLanguage) {
        this.relevantLanguage = relevantLanguage;
    }
}
