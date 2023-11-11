package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.SearchService;
import searchengine.services.SiteIndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {


    private final SiteIndexingService siteIndexingService;


    private SiteRepository siteRepository;

    private PageRepository pageRepository;

    private final StatisticsService statisticsService;
    private final SearchService searchService;


    @Autowired
    public ApiController(SiteIndexingService siteIndexingService, StatisticsService statisticsService,
                         SiteRepository siteRepository, PageRepository pageRepository,
                         SearchService searchService) {
        this.siteIndexingService = siteIndexingService;
        this.statisticsService = statisticsService;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.searchService = searchService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<?> startIndexing() {
        return ResponseEntity.ok(siteIndexingService.startIndexing());
    }


    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        return ResponseEntity.ok(siteIndexingService.stopIndexing());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(siteIndexingService.indexingSinglePage(url));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam final String query,
            @RequestParam(required = false) String site,
            @RequestParam final Integer offset,
            @RequestParam final Integer limit) {
        return ResponseEntity.ok(searchService.getResultsPagesForSearch(query, site, offset, limit));
    }
}
