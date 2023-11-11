package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageModel;

@Repository
public interface PageRepository extends CrudRepository<PageModel, Integer> {
    PageModel findByPathAndSiteIdId(String url, int id);
    Integer countBySiteIdId(int id);
 }
