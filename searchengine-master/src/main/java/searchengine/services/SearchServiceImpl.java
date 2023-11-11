package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.DetailedSearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.utils.lemmaFinder.LemmaFinder;
import searchengine.dto.search.PageFound;
import searchengine.utils.buildResultSearch.PageFoundResults;
import searchengine.repository.SiteRepository;

import java.util.*;


@Service
@Slf4j
public class SearchServiceImpl implements SearchService {


    private PageFoundResults snippetBuild;
    @Autowired
    private SiteRepository siteRepository;

    public SearchServiceImpl(PageFoundResults snippetBuild) {
        this.snippetBuild = snippetBuild;

    }

    @Override
    public SearchResponse getResultsPagesForSearch(String query, String site, Integer offset, Integer limit) {
        List<DetailedSearchItem> items = getItems(query, site);
        List<DetailedSearchItem> dataSnippet;
        int countResultForPage = Math.min((offset + limit), items.size());
        if (items.size() > 10) {
            dataSnippet = items.subList(offset, countResultForPage);
        } else {
            dataSnippet = items;
        }
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setData(dataSnippet);
        searchResponse.setResult(true);
        searchResponse.setCount(items.size());
        return searchResponse;
    }

    private List<DetailedSearchItem> getItems(String query, String site) {
        List<DetailedSearchItem> items = new ArrayList<>();
        List<PageFound> resultList;
        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
            resultList = snippetBuild.getResultsSearch(query, site, lemmaFinder);
            items.addAll(addItems(resultList));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }


    private List<DetailedSearchItem> addItems(List<PageFound> pageFoundList) {
        List<DetailedSearchItem> detailed = new ArrayList<>();
        for (PageFound sm : pageFoundList) {
            DetailedSearchItem item = new DetailedSearchItem();
            item.setUri(sm.getUrl());
            String siteUrl = sm.getPageModel().getSiteId().getUrl();
            if (siteUrl.endsWith("/")) {
                siteUrl = siteUrl.substring(0, siteUrl.length() - 1);
            }
            item.setSite(siteUrl);
            item.setSiteName(siteRepository.findByUrl(sm.getPageModel().getSiteId().getUrl()).getName());
            item.setSnippet(sm.getSnippet());
            item.setTitle(sm.getTitle());
            item.setRelevance(sm.getRelevance());
            detailed.add(item);
        }
        return detailed;
    }
}
