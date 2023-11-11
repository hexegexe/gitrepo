package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchData {
    private List<DetailedSearchItem> data;

    public List<DetailedSearchItem> getData() {
        return data;
    }
}
