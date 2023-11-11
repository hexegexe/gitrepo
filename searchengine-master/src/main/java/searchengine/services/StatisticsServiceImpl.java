package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteModel;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(((List<SiteModel>)siteRepository.findAll()).size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        addAllItem(total,detailed);
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private void addAllItem(TotalStatistics total, List<DetailedStatisticsItem> detailed) {
        List<Site> siteList = sites.getSites();
        int size = siteList.size();
        int randomNumber = random.nextInt(10_000);
        for (int i = 0; i < size; i++) {
            Site site = siteList.get(i);
            SiteModel siteModel = siteRepository.findByUrl(site.getUrl());
            if (siteModel == null) {
                continue;
            }
            int pages = pageRepository.countBySiteIdId(siteModel.getId());
            int lemmas = lemmaRepository.countBySiteIdId(siteModel.getId());
            String error = siteModel.getLastError().isEmpty() ? "" : siteModel.getLastError();
            long statusTime = System.currentTimeMillis() - randomNumber;
            DetailedStatisticsItem item = new DetailedStatisticsItem(
                    site.getUrl(), site.getName(),
                    siteModel.getStatus().toString(), statusTime, error,
                    pages, lemmas);
            detailed.add(item);
        }

        int totalPages = detailed.stream().mapToInt(DetailedStatisticsItem::getPages).sum();
        int totalLemmas = detailed.stream().mapToInt(DetailedStatisticsItem::getLemmas).sum();
        total.setPages(total.getPages() + totalPages);
        total.setLemmas(total.getLemmas() + totalLemmas);
    }

}
