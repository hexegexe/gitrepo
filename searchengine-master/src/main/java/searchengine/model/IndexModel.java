package searchengine.model;

import com.sun.istack.NotNull;

import javax.persistence.*;

@Entity
@Table(name = "`index`")
public class IndexModel {

    @javax.persistence.Id
    @Column(nullable = false)
    @SequenceGenerator(
            name = "index_seq",
            sequenceName = "index_sequence",
            allocationSize = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "index_seq")
    private int id;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_index_page_id"), name = "page_id", nullable = false)
    private PageModel pageId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_index_lemma_id"), name = "lemma_id", nullable = false)
    private LemmaModel lemmaId;

    public IndexModel() {
    }

    public IndexModel(PageModel pageId, LemmaModel lemmaId, float rankByIndex) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.rankByIndex = rankByIndex;
    }

    @NotNull
    private float rankByIndex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public PageModel getPageId() {
        return pageId;
    }

    public void setPageId(PageModel pageId) {
        this.pageId = pageId;
    }

    public LemmaModel getLemmaId() {
        return lemmaId;
    }

    public void setLemmaId(LemmaModel lemmaIdByIndex) {
        this.lemmaId = lemmaIdByIndex;
    }

    public float getRank() {
        return rankByIndex;
    }

    public void setRank(float rank) {
        this.rankByIndex = rank;
    }
}
