package searchengine.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteModel;
import searchengine.model.enums.SiteModelEnum;

import java.util.Optional;

@Repository
public interface SiteRepository extends CrudRepository<SiteModel,Integer> {
    SiteModel findByUrl(String url);
    Optional<SiteModel> findByNameAndStatus(String name, SiteModelEnum status);

}
