package searchengine.dto.search;

import org.springframework.stereotype.Component;
import searchengine.model.PageModel;

import java.util.Objects;

@Component
public class PageFound {

    private String url;

    private String title;
    private String snippet;
    private float relevance;

    private PageModel pageModel;

    public PageFound(String url, String title, String snippet, float relevance, PageModel pageModel) {
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
        this.pageModel = pageModel;
    }

    public PageFound() {
    }

    public PageModel getPageModel() {
        return pageModel;
    }

    public void setPageModel(PageModel pageModel) {
        this.pageModel = pageModel;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public float getRelevance() {
        return relevance;
    }

    public void setRelevance(float relevance) {
        this.relevance = relevance;
    }

}
