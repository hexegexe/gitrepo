package searchengine.utils.recursive;

import lombok.extern.slf4j.Slf4j;
import searchengine.utils.lemmaFinder.LemmaFinder;
import searchengine.config.ConnectionConfig;
import searchengine.model.SiteModel;
import searchengine.model.enums.SiteModelEnum;
import searchengine.repository.*;
import searchengine.services.SiteIndexingServiceImpl;

import java.time.LocalDateTime;
import java.util.concurrent.*;



@Slf4j
public class SiteBuilder implements Runnable {

    private final SiteModel siteModel;
    private final static int BATCH_SIZE = 50;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinder lemmaFinder;
    private LinksStorage linksStorage;
    private ConnectionConfig connectionConfig;


    public SiteBuilder(SiteModel siteModel, SiteRepository siteRepository,
                       PageRepository pageRepository, LemmaRepository lemmaRepository,
                       IndexRepository indexRepository, LemmaFinder lemmaFinder,
                       LinksStorage linksStorage,ConnectionConfig connectionConfig) {
        this.siteModel = siteModel;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.lemmaFinder = lemmaFinder;
        this.linksStorage = linksStorage;
        this.connectionConfig = connectionConfig;
    }

    @Override
    public void run() {
        if (!SiteIndexingServiceImpl.isIndexing) {
            return;
        }
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        log.info("Запущена индексация сайта '" + siteModel.getName() + "'.");
        siteRepository.save(siteModel);
        SiteModel siteFromDB = siteRepository.findByNameAndStatus(siteModel.getName(), SiteModelEnum.INDEXING).orElse(siteModel);
        long start = System.currentTimeMillis();
        buildSite(forkJoinPool);
        forkJoinPool.shutdownNow();
        long finish = System.currentTimeMillis();
        siteFromDB.setStatusTime(LocalDateTime.now());
        if (!SiteIndexingServiceImpl.isStopped) {
            siteFromDB.setStatus(SiteModelEnum.INDEXED);
        }
        log.info("Индексация сайта \"" + siteModel.getName() + "\" завершена [" + (finish - start) / 1000 + " сек.].");
        log.info("Количество записанных страниц = " + pageRepository.countBySiteIdId(siteFromDB.getId()) +
                " Количество найденных лемм = " + lemmaRepository.countBySiteIdId(siteFromDB.getId()));
        siteRepository.save(siteFromDB);
    }

    private void buildSite(ForkJoinPool forkJoinPool) {
        if (!SiteIndexingServiceImpl.isIndexing) {
            return;
        }
        SiteMapRecursiveTask crawlingPages = new SiteMapRecursiveTask(siteModel.getUrl(), siteModel,
                siteRepository, pageRepository, lemmaRepository, indexRepository, lemmaFinder, linksStorage,connectionConfig);
        forkJoinPool.invoke(crawlingPages);
    }
}