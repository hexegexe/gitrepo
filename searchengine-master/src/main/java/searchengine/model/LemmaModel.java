package searchengine.model;

import com.sun.istack.NotNull;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "lemma", indexes =@Index (name = "lemma_index", columnList = "lemma, site_id, id", unique = true))
public class LemmaModel {

    @Id
    @Column(nullable = false)
    @SequenceGenerator(
            name = "lemma_seq",
            sequenceName = "lemma_sequence",
            allocationSize = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lemma_seq")
    private int id;

    @NotNull
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "site_id",nullable = false)
    private SiteModel siteId;

    @OneToMany(mappedBy = "lemmaId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<IndexModel> indexes = new HashSet<>();

    @NotNull
    private String lemma;

    @NotNull
    private int frequency;

    public LemmaModel() {
    }

    public LemmaModel(SiteModel siteId, String lemma, int frequency) {
        this.siteId = siteId;
        this.lemma = lemma;
        this.frequency = frequency;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SiteModel getSiteId() {
        return siteId;
    }

    public void setSiteId(SiteModel siteId) {
        this.siteId = siteId;
    }

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

}
