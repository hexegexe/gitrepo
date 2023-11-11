package searchengine.utils.lemmaFinder;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import searchengine.model.PageModel;

import java.io.IOException;
import java.util.*;

@Component
public class LemmaFinder {

    private LuceneMorphology luceneMorphology;

    private static final String[] PARTICLES_NAMES = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ", "ЧАСТ", "МС", "МС-П", "ВВОДН"};
    private static final String WORD_TYPE_REGEX = "[^а-яА-Я]";

    public LemmaFinder() {
    }

    public LemmaFinder(LuceneMorphology luceneMorphology) {
        this.luceneMorphology = luceneMorphology;
    }

    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology morphology = new RussianLuceneMorphology();
        return new LemmaFinder(morphology);
    }

    public Map<String, Integer> lemmaCollection(PageModel page) {
        String[] words = arrayContainsRussianWords(cleanHTMLTags(page));
        Map<String, Integer> lemma = new HashMap<>();
        for (String word : words) {
            String replaceWord = word.replaceAll("ё", "е");
            if (replaceWord.isBlank() || replaceWord.length() <= 2) {
                continue;
            }

            List<String> wordBase = luceneMorphology.getMorphInfo(replaceWord);
            if (anyWordBaseBelongToParticle(wordBase)) {
                continue;
            }

            List<String> normalWord = luceneMorphology.getNormalForms(replaceWord);
            if (normalWord.isEmpty()) {
                continue;
            }

            String normalForm = normalWord.get(0);
            if (lemma.containsKey(normalForm)) {
                lemma.put(normalForm, lemma.get(normalForm) + 1);
            } else {
                lemma.put(normalForm, 1);
            }
        }
        return lemma;
    }

    public Set<String> getLemmaSet(String text) {
        String[] textArray = arrayContainsRussianWords(text);
        Set<String> lemmaSet = new HashSet<>();
        for (String word : textArray) {
            if (!word.isEmpty() && isCorrectedWord(word)) {
                List<String> wordBaseForms = luceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticle(wordBaseForms)) {
                    continue;
                }
                lemmaSet.addAll(luceneMorphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("ё","е")
                .replaceAll("[^А-я]", " ")
                .trim()
                .split("\\s+");
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::isProperty);
    }

    private boolean isProperty(String word) {
        for (String property : PARTICLES_NAMES) {
            if (word.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    private boolean isCorrectedWord(String word) {
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morhInfo : wordInfo) {
            if (morhInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }

    private String cleanHTMLTags(PageModel page) {
        return Jsoup.parse(page.getContent()).text().replaceAll("[^а-яА-Я]", " ").toLowerCase();
    }
}
