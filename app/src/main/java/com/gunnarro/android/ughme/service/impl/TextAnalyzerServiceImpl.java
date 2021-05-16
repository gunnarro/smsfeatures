package com.gunnarro.android.ughme.service.impl;

import android.util.Log;

import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.report.ReportItem;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import static java.util.stream.Collectors.toMap;

@Singleton
public class TextAnalyzerServiceImpl {

    public static final String DEFAULT_WORD_REGEXP = "\\b\\w{3,}"; // match only word with length > 3
    /**
     * The word with most occurrences will get the largest font size.
     * Thereafter will occurrences of all other words be compared against this number in order to determine font size.
     * <p>
     * That means:
     * MaxWordOccurrences = MAX_WORD_FONT_SIZE
     * <p>
     * (OtherWordOccurrences / MaxWordOccurrences) * MAX_WORD_FONT_SIZE
     */
    private long analyzeTimeMs;

    /**
     * default constructor
     */
    @Inject
    public TextAnalyzerServiceImpl() {
    }

    /**
     * Simply breaks the text based on white spaces and keeps only chars [aA-zZ] and æÆøØåÅ for norwegian.
     * The length of the word is specified in the regexp.
     *
     * @param text   text to split into single words
     * @param regexp regex which hold the word extraction rule
     */
    public AnalyzeReport analyzeText(@NotNull final String text, Integer category, String regexp, int numberOfMostUsedWords) {
        // validate input
        if (text == null || text.isEmpty()) {
            Log.d("TextAnalyzer.analyzeText", "text is null or empty!");
            return getReport(new HashMap<>(), category,0);
        }
        Map<String, Integer> sortedWordMap;
        int totalNumberOfWords = 0;
        Log.d("analyzeText", String.format("start, text.length=%s, regexp=%s", text.length(), regexp));
        long startTimeMs = System.currentTimeMillis();
        Map<String, Integer> tmpWordMap = new HashMap<>();
        Pattern pattern = Pattern.compile(regexp == null ? DEFAULT_WORD_REGEXP : regexp, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        // find matching occurrence, one by one
        while (matcher.find()) {
            totalNumberOfWords++;
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
                .limit(numberOfMostUsedWords)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        analyzeTimeMs = System.currentTimeMillis() - startTimeMs;
        Log.d(Utility.buildTag(getClass(), "analyzeText"), String.format("exeTime=%s ms, thread=%s", analyzeTimeMs, Thread.currentThread().getName()));
        return getReport(sortedWordMap, category, totalNumberOfWords);
    }

    private AnalyzeReport getReport(Map<String, Integer> wordMap, Integer category, int totalNumberOfWords) {
        int numberOfWords = wordMap.values()
                .stream()
                .mapToInt(Integer::valueOf)
                .sum();

        List<ReportItem> reportItems = new ArrayList<>();
        wordMap.forEach((k, v) -> reportItems.add(ReportItem.builder().word(k).category(category).count(v).percentage(v * 100 / numberOfWords).build()));

        int highestWordCount = wordMap.size() > 0 ? wordMap.values().iterator().next() : 0;
        float highestWordCountPercent = highestWordCount > 0 ? (float) highestWordCount * 100 / numberOfWords : 0;

        return AnalyzeReport.builder()
                .textWordCount(totalNumberOfWords)
                .textUniqueWordCount(wordMap.size())
                .textHighestWordCount(highestWordCount)
                .textHighestWordCountPercent(highestWordCountPercent)
                .analyzeTimeMs(analyzeTimeMs)
                .reportItems(reportItems)
                .profileItems(new ArrayList<>())
                .build();
    }
}
