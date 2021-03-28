package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

/**
 * Run the word cloud build as a background task
 */
public class BuildWordCloudTask {

    final WordCloudService wordCloudService;
    final SmsBackupServiceImpl smsBackupService;
    final TextAnalyzerServiceImpl textAnalyzerService;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public BuildWordCloudTask(WordCloudService wordCloudService, SmsBackupServiceImpl smsBackupService, TextAnalyzerServiceImpl textAnalyzerService) {
        this.wordCloudService = wordCloudService;
        this.smsBackupService = smsBackupService;
        this.textAnalyzerService = textAnalyzerService;
    }

    public void buildWordCloudEventBus(final Settings settings, final Dimension cloudDimension, final String contactName, final String smsType) {
        Log.d("", "Start build word cloud task..." + settings.toString());
        Runnable buildWordCloudRunnable = () -> {
            try {
                long startTimeMs = System.currentTimeMillis();
                textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(contactName, smsType), settings.wordMatchRegex);
                List<Word> wordList = wordCloudService.buildWordCloud(textAnalyzerService.getWordCountMap(settings.numberOfWords)
                        , textAnalyzerService.getHighestWordCount()
                        , cloudDimension
                        , settings);
                // when finished publish result so word cloud view can pick up the word list and redraw the word cloud
                RxBus.getInstance().publish(
                        WordCloudEvent.builder()
                                .eventType(WordCloudEvent.WordCloudEventTypeEnum.UPDATE_MESSAGE)
                                .wordList(wordList)
                                .build());
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("%s", textAnalyzerService.getReport(false)));
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("finished, exeTime=%s ms", (System.currentTimeMillis() - startTimeMs)));
            } catch (Exception e) {
                Log.e(Utility.buildTag(getClass(), "buildWordCloudEventBus"), e.getMessage());
            }
        };
        executor.execute(buildWordCloudRunnable);
    }
}