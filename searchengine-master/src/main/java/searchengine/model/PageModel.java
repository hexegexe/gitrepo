package searchengine.model;


import com.sun.istack.NotNull;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Data
@Table(name = "page", indexes = @javax.persistence.Index(name = "path_siteId_index", columnList = "path, site_id", unique = true))
public class PageModel {

    @Id
    @Column(name = "id", nullable = false)
    @SequenceGenerator(
            name = "page_seq",
            sequenceName = "page_sequence",
            allocationSize = 40)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "page_seq")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = SiteModel.class, cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(foreignKey = @ForeignKey(name = "site_page_FK"), columnDefinition = "Integer",
            referencedColumnName = "id", name = "site_id", nullable = false, updatable = false)
    private SiteModel siteId;

    @NotNull
    @Column(columnDefinition = "VARCHAR(768) CHARACTER SET utf8")
    private String path;

    @OneToMany(mappedBy = "pageId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<IndexModel> index = new HashSet<>();
    @NotNull
    private int code;
    @NotNull
    @Column(length = 16777215, columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci")
    private String content;

    @NotNull
    @Column(columnDefinition = "VARCHAR(255)")
    private String title;

    public PageModel() {
    }

    public PageModel(SiteModel siteId, String path, int code, String content, String title) {
        this.siteId = siteId;
        this.path = path;
        this.code = code;
        this.content = content;
        this.title = title;
    }


}
