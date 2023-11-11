package searchengine.utils.buildResultSearch;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.utils.lemmaFinder.LemmaFinder;
import searchengine.dto.search.PageFound;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;


@Data
@Component
@Slf4j
public class PageFoundResults {

    private static final String REGEX = "[^а-яА-Я0-9.,]";

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    private List<LemmaModel> allLemmaList = new ArrayList<>();
    private Set<PageModel> allPagesList = new HashSet<>();
    private Iterable<Site> allSites = new ArrayList<>();
    private Integer countLemmasAfterChange = 0;
    @Autowired
    private SitesList sitesList;

    public PageFoundResults() {
    }

    private void addAllLemmasInList(String query, String site, LemmaFinder lemmaFinder) {
        List<LemmaModel> uniqueLemmas = new ArrayList<>();
        Set<String> setLemmas = lemmaFinder.getLemmaSet(query);
        try {
            if (site == null) {
                for (Site sm : allSites) {
                    uniqueLemmas.addAll(getLemmas(sm.getUrl(), setLemmas));
                }
            } else {
                uniqueLemmas.addAll(getLemmas(site, setLemmas));
            }
            for (LemmaModel lemmaModel : uniqueLemmas) {
                int siteId = lemmaModel.getSiteId().getId();
                double countPagesForSite = pageRepository.countBySiteIdId(siteId);
                double frequency = (double) lemmaModel.getFrequency() / countPagesForSite;
                if (frequency < 0.8) {
                    allLemmaList.add(lemmaModel);
                }
            }
            countLemmasAfterChange = setLemmas.size();
            allLemmaList.sort(Comparator.comparingInt(LemmaModel::getFrequency));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LemmaModel> getLemmas(String site, Set<String> setLemmas) {
        List<LemmaModel> foundLemmas = new ArrayList<>();
        for (String lemma : setLemmas) {
            foundLemmas.addAll(lemmaRepository.findByLemmaAndSiteIdUrl(lemma, site));
        }
        return foundLemmas;
    }


    private Set<PageModel> getPagesForLemmas(List<LemmaModel> listLemma) {
        Set<PageModel> pagesForSearch = new HashSet<>();
        if (listLemma.isEmpty()) {
            return Collections.emptySet();
        }
        for (int i = 0; i < listLemma.size(); i++) {
            List<PageModel> matchingPages = new ArrayList<>();
            List<IndexModel> indexAllByLemma = indexRepository.findByLemmaId(listLemma.get(i));
            for (IndexModel im : indexAllByLemma) {
                matchingPages.add(im.getPageId());
            }
            if (i > 0) {
                pagesForSearch.retainAll(matchingPages);
            } else {
                pagesForSearch = new HashSet<>(matchingPages);
            }
        }
        return pagesForSearch;
    }

    private void isEqualsSiteIdAndAddPages(String site) {
        List<LemmaModel> siteIdListLemma = allLemmaList.stream()
                .filter(l -> l.getSiteId().getUrl().equals(site)).toList();
        if (siteIdListLemma.size() >= countLemmasAfterChange) {
            Set<PageModel> listPageByLemma = getPagesForLemmas(siteIdListLemma);
            allPagesList.addAll(listPageByLemma);
        }
    }

    private void isCheckEmptyUrl(String site) {
        if (site == null) {
            for (Site sm : allSites) {
                isEqualsSiteIdAndAddPages(sm.getUrl());
            }
        } else {
            isEqualsSiteIdAndAddPages(site);
        }
    }

    private Map<PageModel, Float> getAbsRelevance(String site) {
        isCheckEmptyUrl(site);
        HashMap<PageModel, Float> absoluteRelevance = new HashMap<>();
        if (allPagesList.isEmpty()) {
            return Collections.emptyMap();
        }
        for (PageModel pg : allPagesList) {
            List<IndexModel> allLemmasInPage = indexRepository.findAllByPageIdAndPageIdSiteIdId(pg, pg.getSiteId().getId());
            float absRelevanceForPage = (float) allLemmasInPage.stream().mapToDouble(IndexModel::getRank).sum();
            absoluteRelevance.put(pg, absRelevanceForPage);
        }
        return absoluteRelevance;
    }


    private Map<PageModel, Float> getRelRelevance(String site) {
        Map<PageModel, Float> relativeRelevance = new HashMap<>();
        Map<PageModel, Float> mapAbsRel = getAbsRelevance(site);

        if (mapAbsRel.isEmpty()) {
            return Collections.emptyMap();
        }
        float maxAbsRelevance = Collections.max(mapAbsRel.values());
        for (Map.Entry<PageModel, Float> entry : mapAbsRel.entrySet()) {
            relativeRelevance.put(entry.getKey(), entry.getValue() / maxAbsRelevance);
        }
        return relativeRelevance.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }


    public List<PageFound> getResultsSearch(String query, String site, LemmaFinder lemmaFinder) {
        allSites = sitesList.getSites();
        addAllLemmasInList(query, site, lemmaFinder);
        try {
            if (query.isEmpty()) {
                return Collections.emptyList();
            }
            Map<PageModel, Float> relRelevance = getRelRelevance(site);
            if (relRelevance.isEmpty() || allLemmaList.isEmpty()) {
                return Collections.emptyList();
            }
            List<PageFound> pageFoundList = new ArrayList<>(relRelevance.size());
            for (Map.Entry<PageModel, Float> entry : relRelevance.entrySet()) {
                PageModel pageModel = entry.getKey();
                Float relevance = entry.getValue();
                String textSnippet = getSnippet(pageModel, allLemmaList, lemmaFinder);

                if (textSnippet.isEmpty()) {
                    continue;
                }

                PageFound pageFound = new PageFound(
                        pageModel.getPath(),
                        pageModel.getTitle(),
                        textSnippet,
                        relevance,
                        pageModel);
                pageFoundList.add(pageFound);
            }
            allLemmaList.clear();
            allPagesList.clear();
            return pageFoundList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private String getSnippet(PageModel pageModel, List<LemmaModel> lemma, LemmaFinder lemmaFinder) {
        StringBuilder snippet = new StringBuilder();
        String textForSnippet = "";
        Map<Integer, String> badWord = getResultsWords(lemma, lemmaFinder, pageModel);
        String contentClean = getRegexContent(pageModel.getContent());
        for (Map.Entry<Integer, String> badWords : badWord.entrySet()) {
            int index = badWords.getKey();
            String word = badWords.getValue();
            int maxLengthSnippet = 240 / badWord.size();
            textForSnippet = generateSnippet(contentClean, index, maxLengthSnippet);
            if (textForSnippet.contains(word)) {
                textForSnippet = getFattyWord(textForSnippet, word);
            }
            if (!snippet.isEmpty()) {
                snippet.append(" ... ");
            }
            snippet.append(textForSnippet);
        }

        return isCheckContainsAllWord(badWord, snippet.toString());
    }

    private String generateSnippet(String text, int index, int maxLength) {
        int halfMax = maxLength / 2;
        int preIndex = Math.max(index - halfMax, 0);
        int endIndex = Math.min(preIndex + maxLength, text.length());
        return text.substring(preIndex, endIndex);
    }

    private String isCheckContainsAllWord(Map<Integer, String> badWord, String text) {
        int count = 0;
        for (Map.Entry<Integer, String> entry : badWord.entrySet()) {
            String word = entry.getValue();
            if (text.contains(word)) {
                count += 1;
            }
        }
        for(Site site : allSites) {
            List<LemmaModel> lemmasForSite = allLemmaList.stream()
                    .filter(l -> l.getSiteId().getUrl().equals(site.getUrl())).toList();
            if (count < lemmasForSite.size()) {
                text = "";
            }
        }
        return text;
    }

    private String getFattyWord(String snippet, String word) {
        return snippet.replace(word, "<b>" + word + "</b>");
    }

    private Map<Integer, String> getBadWordsInContent(PageModel pageModel) {
        Map<Integer, String> getAllBadWords = new HashMap<>();
        String contentClean = getRegexContent(pageModel.getContent());
        String[] split = contentClean.split("\\s");
        for (String s : split) {
            getAllBadWords.put(contentClean.indexOf(s), s);
        }

        return getAllBadWords;
    }

    private Map<Integer, String> getResultsWords(List<LemmaModel> lemmas, LemmaFinder lemmaFinder, PageModel pageModel) {
        Map<Integer, String> resultsWord = new HashMap<>();
        try {
            Map<Integer, String> badWordsMap = getBadWordsInContent(pageModel);

            processBadWords(badWordsMap, lemmas, lemmaFinder, resultsWord);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultsWord;
    }

    private void processBadWords(Map<Integer, String> badWordsMap, List<LemmaModel> lemmas,
                                 LemmaFinder lemmaFinder, Map<Integer, String> resultsWord) {
        for (Map.Entry<Integer, String> entryBadWord : badWordsMap.entrySet()) {
            int index = entryBadWord.getKey();
            String badWord = entryBadWord.getValue();
            Set<String> lemmaSet = lemmaFinder.getLemmaSet(badWord);

            addWordsInResultMap(lemmas, resultsWord, index, badWord, lemmaSet);
        }
    }

    private void addWordsInResultMap(List<LemmaModel> lemmas, Map<Integer, String> resultsWord, int index,
                                     String badWord, Set<String> lemmaSet) {
        for (LemmaModel lm : lemmas) {
            String lemmaStr = lm.getLemma();
            if (isLemmaContained(lemmaSet, lemmaStr)) {
                if (!hasDuplicateRoot(resultsWord, lemmaStr)) {
                    resultsWord.put(index, badWord);
                }
            }
        }
    }

    private boolean isLemmaContained(Set<String> lemmaSet, String lemma) {
        return lemmaSet.contains(lemma.toLowerCase());
    }

    private boolean hasDuplicateRoot(Map<Integer, String> resultsWord, String lemmaStr) {
        for (String word : resultsWord.values()) {
            if (word.startsWith(lemmaStr.substring(0, lemmaStr.length() - 3))) {
                return true;
            }
        }
        return false;
    }
    private String getRegexContent(String content) {
        return Jsoup.parse(content).text().replaceAll(REGEX, " ");
    }
}