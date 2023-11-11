package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaModel;

import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaModel,Integer> {
    LemmaModel findFirstByLemmaAndSiteIdId(String lemma,int siteId);
    Integer countBySiteIdId(int id);
    List<LemmaModel> findByLemmaAndSiteIdUrl(String lemma,String url);

}
