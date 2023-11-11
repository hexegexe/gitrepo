package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexModel;
import searchengine.model.LemmaModel;
import searchengine.model.PageModel;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<IndexModel,Integer> {

    List<IndexModel> findByLemmaId(LemmaModel lemmaModel);
    List<IndexModel> findAllByPageIdAndPageIdSiteIdId(PageModel pageModel, int id);

}
