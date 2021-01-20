package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

/**
 * Run the word cloud build as a background task
 */
public class BuildWordCloudTask {

    private static final String TAG = BuildWordCloudTask.class.getSimpleName();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    final WordCloudService wordCloudService;
    final SmsBackupServiceImpl smsBackupService;
    final TextAnalyzerServiceImpl textAnalyzerService;

    @Inject
    public BuildWordCloudTask(WordCloudService wordCloudService, SmsBackupServiceImpl smsBackupService, TextAnalyzerServiceImpl textAnalyzerService) {
        this.wordCloudService = wordCloudService;
        this.smsBackupService = smsBackupService;
        this.textAnalyzerService = textAnalyzerService;
    }

    @Deprecated
    public Future<List<Word>> buildWordCloud(final Settings settings, final Dimension cloudDimension, final String contactName, final String smsType) {
        Callable<List<Word>> buildWordListCallable = () -> {
            long startTimeMs = System.currentTimeMillis();
            textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(contactName, smsType), settings.wordMatchRegex);
            Log.i(TAG, textAnalyzerService.getReport(true).toString());
            List<Word> wordList = wordCloudService.buildWordCloud(textAnalyzerService.getWordCountMap(settings.numberOfWords)
                    , textAnalyzerService.getHighestWordCount()
                    , cloudDimension
                    , settings);
            Log.i(TAG, String.format("buildWordCloud finished, dimension=%s ,buildTime=%s ms, tread: %s", cloudDimension, (System.currentTimeMillis() - startTimeMs), Thread.currentThread().getName()));
            return wordList;
        };
        return executor.submit(buildWordListCallable);
    }


    public void buildWordCloudEventBus(final Settings settings, final Dimension cloudDimension, final String contactName, final String smsType) {
        Runnable buildWordCloudRunnable = () -> {
            try {
                long startTimeMs = System.currentTimeMillis();
                textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(contactName, smsType), settings.wordMatchRegex);
                Log.i(TAG, textAnalyzerService.getReport(true).toString());
                List<Word> wordList = wordCloudService.buildWordCloud(textAnalyzerService.getWordCountMap(settings.numberOfWords)
                        , textAnalyzerService.getHighestWordCount()
                        , cloudDimension
                        , settings);
                // when finished publish result so fragment can pick up the word list
                RxBus.getInstance().publish(
                        WordCloudEvent.builder()
                                .eventType(WordCloudEvent.WordCloudEventTypeEnum.UPDATE_MESSAGE)
                                .smsTypeAll()
                                .wordList(wordList)
                                .build());
                Log.i(TAG, String.format("buildWordCloud finished, buildTime=%s ms, tread: %s", (System.currentTimeMillis() - startTimeMs), Thread.currentThread().getName()));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        };
        executor.execute(buildWordCloudRunnable);
    }
}


