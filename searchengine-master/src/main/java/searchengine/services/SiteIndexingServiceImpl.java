package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.utils.recursive.*;
import searchengine.dto.statistics.Response;
import searchengine.utils.lemmaFinder.LemmaFinder;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.enums.SiteModelEnum;

import searchengine.repository.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class SiteIndexingServiceImpl implements SiteIndexingService {
    private final SitesList sitesList;
    private SiteMapRecursiveTask siteMapRecursiveTask;
    private ExecutorService service;
    private long startTime;
    private final ConnectionConfig connectionConfig;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;
    public static volatile boolean isIndexing;
    public static volatile boolean isStopped;
    @Autowired
    private SiteRepository siteRepository;


    public SiteIndexingServiceImpl(SitesList sitesList, SiteMapRecursiveTask siteMapRecursiveTask, ConnectionConfig connectionConfig) {
        this.sitesList = sitesList;
        this.siteMapRecursiveTask = siteMapRecursiveTask;
        this.connectionConfig = connectionConfig;

    }

    @Override
    public Response startIndexing() {
        Response response = new Response();
        if (isIndexing) {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else {
            new Thread(this::startIndexingSite).start();
            response.setResult(true);
        }
        return response;
    }

    @Override
    public Response stopIndexing() {
        Response response = new Response();
        if (isIndexing) {
            stopIndexingSite();
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }
        return response;
    }

    @Override
    public Response indexingSinglePage(String url) {
        Response response = new Response();
        if (!isIndexing) {
            isIndexing = true;
            updateOrBuildPage(url);
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, \n" +
                    "указанных в конфигурационном файле\n");
        }
        return response;
    }

    public void startIndexingSite()  {
        try {
            log.info("ИНДЕКСАЦИЯ ЗАПУЩЕНА");
            int countSites = sitesList.getSites().size();
            service = Executors.newFixedThreadPool(countSites);
            isStopped = false;
            if (!service.isTerminated()) {
                startTime = System.currentTimeMillis();
                log.info("ExecutorService ЗАПУЩЕН");
            }
            isIndexing = true;
            cleanSitesAndPagesInDB();

            for (Site site : sitesList.getSites()) {
                String url = site.getUrl();
                String name = site.getName();
                SiteModel siteModel = new SiteModel(SiteModelEnum.INDEXING, LocalDateTime.now(), "", url, name);
                buildSite(siteModel);
            }
            service.shutdown();
            handleIndexingShutdown();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void buildSite(SiteModel siteModel) {
        if (!isIndexing) {
            return;
        }
        try {
            Set<String> noVisitLinks = ConcurrentHashMap.newKeySet();
            LinksStorage linksStorage = new LinksStorage(noVisitLinks);
            LemmaFinder lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
            SiteBuilder siteBuilder = new SiteBuilder(
                    siteModel, siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaFinder,linksStorage,
                    connectionConfig);
            service.submit(siteBuilder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopIndexingSite() {
        if (isIndexing) {
            isIndexing = false;
            isStopped = true;
            service.shutdown();
            try {
                if (!service.awaitTermination(15, TimeUnit.SECONDS)) {
                    service.shutdownNow();
                    log.warn("Trying shutdownNow poolOfSites");
                    if (!service.awaitTermination(15, TimeUnit.SECONDS)) {
                        log.error("Pool did not terminate");
                    }
                }
            } catch (InterruptedException ie) {
                service.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void updateOrBuildPage(String url) {
        log.info("ИНДЕКСАЦИЯ СТРАНИЦЫ " + url + " ЗАПУЩЕНА");
        try {
            LemmaFinder lemmaFinder = new LemmaFinder(new RussianLuceneMorphology());
            String getUrl = new URL(url).getFile();
            for (Site site : sitesList.getSites()) {
                if (site.getUrl().startsWith(url)) {
                    SiteModel siteModel = siteRepository.findByUrl(site.getUrl());
                    Set<String> noVizitLinks = new HashSet<>();
                    LinksStorage linksStorage = new LinksStorage(noVizitLinks);
                    siteMapRecursiveTask = new SiteMapRecursiveTask(getUrl,siteModel,
                            siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaFinder,
                            linksStorage,connectionConfig);
                    PageModel searchPage = pageRepository.findByPathAndSiteIdId(getUrl, siteModel.getId());
                    if (searchPage != null) {
                        pageRepository.delete(searchPage);
                    }
                    siteMapRecursiveTask.singleIndexingUrl(url, siteModel, lemmaFinder);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        isIndexing = false;
        log.info("ИНДЕКСАЦИЯ СТРАНИЦЫ " + url + "  ЗАКОНЧЕНА");
    }

    private void handleIndexingShutdown() {
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (isStopped) {
                for (Site site : sitesList.getSites()) {
                    SiteModel siteModel = siteRepository.findByUrl(site.getUrl());
                    if (siteModel.getStatus() == SiteModelEnum.INDEXED) {
                        continue;
                    }
                    siteModel.setStatus(SiteModelEnum.FAILED);
                    siteModel.setLastError("Индексация остановлена пользователем");
                    siteRepository.save(siteModel);
                    log.info("ИНДЕКСАЦИЯ САЙТА " + siteModel.getUrl() + " ОСТАНОВЛЕНА ПОЛЬЗОВАТЕЛЕМ");
                }
            } else {
                isIndexing = false;
                service.shutdown();
                long minute = ((System.currentTimeMillis() - startTime) / 1000) / 60;
                log.info("ИНДЕКСАЦИЯ ОСТАНОВЛЕНА И ВЫПОЛНЯЛАСЬ [" + minute + " мин.].");
            }
            log.info("ExecutorService ОСТАНОВЛЕН");
        } catch (Exception e) {
            log.error("Ошибка при изменении", e);
            Thread.currentThread().interrupt();
        }
    }
    private void cleanSitesAndPagesInDB(){
        pageRepository.deleteAll();
        siteRepository.deleteAll();
    }
}