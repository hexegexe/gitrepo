package searchengine.repository;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class LinksStorage {

    private final Set<String> noVisitLinks;
    private final Set<String> visitLinks = Collections.synchronizedSet(new HashSet<>());


    public LinksStorage(Set<String> noVisitLinks) {
        this.noVisitLinks = noVisitLinks;
    }


    public Set<String> getNoVisitLinks() {
        return noVisitLinks;
    }

    public Set<String> getVisitLinks() {
        return visitLinks;
    }
    public void addLinkToVisit(String url) {
        if (url != null && !visitLinks.contains(url)) {
            visitLinks.add(url);
        }
    }
}

