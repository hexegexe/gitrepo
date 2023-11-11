package searchengine.services;

import searchengine.dto.search.SearchResponse;

public interface SearchService {

    SearchResponse getResultsPagesForSearch(String query, String site, Integer offset, Integer limit);
}
