package searchengine.utils.recursive;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import searchengine.utils.lemmaFinder.LemmaFinder;
import searchengine.config.ConnectionConfig;
import searchengine.model.*;
import searchengine.repository.*;
import searchengine.services.SiteIndexingServiceImpl;

import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
public class SiteMapRecursiveTask extends RecursiveAction {

    private ConnectionConfig config;
    private final static int BATCH_SIZE = 200;
    private String url;
    private SiteModel siteModel;
    private LemmaRepository lemmaRepository;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private IndexRepository indexRepository;
    private LemmaFinder lemmaFinder;
    private LinksStorage linksStorage;
    public static Set<PageModel> pages = ConcurrentHashMap.newKeySet();

    public SiteMapRecursiveTask() {
    }

    public SiteMapRecursiveTask(String url, SiteModel siteModel,
                                SiteRepository siteRepository, PageRepository pageRepository,
                                LemmaRepository lemmaRepository, IndexRepository indexRepository,
                                LemmaFinder lemmaFinder, LinksStorage linksStorage, ConnectionConfig config ) {
        this.url = url;
        this.siteModel = siteModel;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaFinder = lemmaFinder;
        this.linksStorage = linksStorage;
        this.config = config;
    }

    @Override
    protected void compute() {
        if (!SiteIndexingServiceImpl.isIndexing) {
            return;
        }

        try {
            if (!linksStorage.getVisitLinks().contains(url)) {
                Connection connection = getConnection(url);
                lockThreadForRemoveLink(url);
                Document document = connection.get();
                int statusCode = connection.response().statusCode();

                if (statusCode == 200) {
                    String pagePath = new URL(url).getFile();
                    if (pagePath.isEmpty()) {
                        pagePath = url;
                    }
                    PageModel pageModel = new PageModel(
                            siteModel,
                            pagePath,
                            statusCode,
                            document.html(),
                            document.title());
                    linksStorage.getVisitLinks().add(url);
                        synchronized (lemmaRepository) {
                            batchInsertLemmas(findDuplicateEntity(pageModel), lemmaFinder);
                    }
                }

                Elements linksOnPageElements = document.select("a[href]");
                List<SiteMapRecursiveTask> tasks = new ArrayList<>();

                for (Element element : linksOnPageElements) {
                    String link = element.absUrl("href");
                    if (!linksStorage.getNoVisitLinks().contains(link) && isValidUrl(link)) {
                        linksStorage.getNoVisitLinks().add(link);
                        SiteMapRecursiveTask task = new SiteMapRecursiveTask(link, siteModel,
                                siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaFinder, linksStorage,config);
                        tasks.add(task);
                    }
                }

                if (!tasks.isEmpty()) {
                    invokeAll(tasks);
                }
            }
        } catch (Exception exception) {
            log.error("Error while processing URL '" + url + "': " + exception.getMessage());
        }
    }

    private Connection getConnection(String url) {
        try {
            Connection connection = Jsoup.connect(url);
            connection.userAgent(config.getUserAgent());
            connection.referrer(config.getReferrer());
            connection.ignoreHttpErrors(true);
            connection.followRedirects(false);
            return connection;
        } catch (Exception e) {
            throw new RuntimeException("Error while connecting to: " + url, e);
        }
    }

    private void batchInsertLemmas(PageModel pageModel, LemmaFinder lemmaFinder) {
            Map<String, Integer> lemmaMap = lemmaFinder.lemmaCollection(pageModel);
            Set<LemmaModel> lemmaList = new HashSet<>(lemmaMap.size());
            Set<IndexModel> indexList = new HashSet<>(lemmaMap.size());

            for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
                String lemma = entry.getKey();
                int frequency = entry.getValue();
                LemmaModel lemmaModel = lemmaRepository.findFirstByLemmaAndSiteIdId(lemma, pageModel.getSiteId().getId());
                if (lemmaModel != null) {
                    lemmaModel.setFrequency(lemmaModel.getFrequency() + 1);
                } else {
                    lemmaModel = new LemmaModel(pageModel.getSiteId(), lemma, 1);
                }

                IndexModel indexModel = new IndexModel(pageModel, lemmaModel, frequency);
                indexList.add(indexModel);
                lemmaList.add(lemmaModel);

                if (lemmaList.size() >= BATCH_SIZE) {
                    saveLemmasAndIndexesInDB(lemmaList,indexList);
                    indexList.clear();
                    lemmaList.clear();
                }
            }
            if (!lemmaList.isEmpty() && !indexList.isEmpty()) {
                saveLemmasAndIndexesInDB(lemmaList,indexList);
            }
    }

    private PageModel findDuplicateEntity(PageModel pageModel) {
        if (pageRepository.findByPathAndSiteIdId(pageModel.getPath(), pageModel.getSiteId().getId()) == null) {
            pageRepository.save(pageModel);
            return pageModel;
        }
        return null;
    }

    private boolean isValidUrl(String url) {
        String path = url.toLowerCase(Locale.ROOT);
        String regex = "([^\\sА-я]+(\\.(?i)(jpg|png|gif|bmp|pdf|doc|xlsx|jpeg|eps|docx|xml))$)";
        return path.startsWith(siteModel.getUrl()) && !path.contains("#") && !path.contains("?") && !path.equals(siteModel.getUrl()) && !url.matches(regex);
    }

    public void singleIndexingUrl(String url, SiteModel siteModel, LemmaFinder lemmaFinder) {
        try {
            Connection connection = getConnection(url);
            Document document = connection.get();
            PageModel pageModel = new PageModel(siteModel,
                    new URL(url).getFile(),
                    connection.response().statusCode(),
                    document.html(),
                    document.title());
            pageRepository.save(pageModel);
            batchInsertLemmas(pageModel, lemmaFinder);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void lockThreadForRemoveLink(String url) {
        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        try {
            if (!linksStorage.getVisitLinks().contains(url)) {
                linksStorage.addLinkToVisit(url);
                linksStorage.getNoVisitLinks().remove(url);
            }
        } finally {
            lock.unlock();
        }
    }

    private void saveLemmasAndIndexesInDB(Set<LemmaModel> lemmaList, Set<IndexModel> indexList){
            lemmaRepository.saveAll(lemmaList);
            indexRepository.saveAll(indexList);
    }
}
