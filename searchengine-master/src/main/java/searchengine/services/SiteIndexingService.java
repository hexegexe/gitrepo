package searchengine.services;

import searchengine.dto.statistics.Response;

public interface SiteIndexingService {

   Response startIndexing();

   Response stopIndexing();

   Response indexingSinglePage(String url);


}
