package com.gunnarro.android.ughme.analyze;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class TextAnalyzer {

    private static final String TAG = TextAnalyzer.class.getSimpleName();

    public static final String DEFAULT_WORD_REGEXP = "\\b\\w{3,}"; // match only word with length > 3
    private Map<String, Integer> sortedWordMap = new LinkedHashMap<>();
    private Integer numberOfWords = 0;
    private int highestWordCount = 0;

    public TextAnalyzer() {
    }

    public void analyzeText(final String text, String regexp) {
        Map<String, Integer> tmpWordMap = new HashMap<>();
        if (text == null || text.isEmpty()) {
            Log.d("TextAnalyzer.analyzeText", "text is null or empty!");
            return;
        }
        Pattern pattern = Pattern.compile(regexp == null ? DEFAULT_WORD_REGEXP : regexp, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        // find matching occurrence, one by one
        while (matcher.find()) {
            numberOfWords++;
            // do not care about upper and lower case
            String word = Objects.requireNonNull(matcher.group(0)).toLowerCase().trim();
            if (!tmpWordMap.containsKey(word)) {
                tmpWordMap.put(word, 1);
            } else {
                tmpWordMap.compute(word, (k, v) -> v == null ? 1 : v + 1);
            }
        }
        // finally sort the word map by number of word hits
        sortedWordMap = tmpWordMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        if (!sortedWordMap.isEmpty()) {
            highestWordCount = sortedWordMap.values().iterator().next();
        }
    }

    /**
     * word count map sorted by occurrences
     *
     * @return sorted Linked Hash Map
     */
    public Map<String, Integer> getWordCountMap(int numberOfMostUsedWords) {
        return sortedWordMap.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .limit(numberOfMostUsedWords)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
    }

    public Integer getNumberOfUniqueWords() {
        return this.sortedWordMap.size();
    }

    public Integer getNumberOfWords() {
        return this.numberOfWords;
    }

    public void printReport() {
        Log.d(TAG, String.format("Analyze Report: number of words=%s, unique words=%s", numberOfWords, getNumberOfUniqueWords()));
        sortedWordMap.forEach((k, v) -> Log.i(TAG, String.format("Analyze Report: word=%s, count=%s, percentage=%s", k,v, (v*100/numberOfWords))));
    }
    /**
     * The word with most occurrences will get the largest font size.
     * Thereafter will occurrences of all other words be compared against this number in order to determine font size.
     * <p>
     * That means:
     * MaxWordOccurrences = MAX_WORD_FONT_SIZE
     * <p>
     * (OtherWordOccurrences / MaxWordOccurrences) * MAX_WORD_FONT_SIZE
     */
    public int getHighestWordCount() {
        return highestWordCount;
    }

    public float getHighestWordCountPercent() {
        return (float) this.numberOfWords * (float) highestWordCount / 100;
    }
}
