package searchengine.model;

import com.sun.istack.NotNull;
import org.hibernate.annotations.Cascade;
import searchengine.model.enums.SiteModelEnum;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sites")
public class SiteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @OneToMany(mappedBy = "siteId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PageModel> pages = new HashSet<>();

    @OneToMany(mappedBy = "siteId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<LemmaModel> lemmas = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @NotNull
    private SiteModelEnum status;

    @NotNull
    @Column(name = "status_time", columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @NotNull
    @Column(columnDefinition = "VARCHAR(255)")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private String url;
    @NotNull
    @Column(columnDefinition = "VARCHAR(255)")
    private String name;

    public SiteModel() {
    }

    public SiteModel(SiteModelEnum status, LocalDateTime statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public SiteModelEnum getStatus() {
        return status;
    }

    public void setStatus(SiteModelEnum status) {
        this.status = status;
    }

    public LocalDateTime getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(LocalDateTime statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<PageModel> getPages() {
        return pages;
    }

}
