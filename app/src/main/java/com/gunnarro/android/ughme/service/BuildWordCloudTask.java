package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.model.analyze.AnalyzeReport;
import com.gunnarro.android.ughme.model.analyze.ProfileItem;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;
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
            long startTime = System.currentTimeMillis();
            try {
                postProgress("analyse sms content...", 10);
                AnalyzeReport analyzeReport = textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(contactName, smsType), settings.wordMatchRegex, settings.numberOfWords  );
                analyzeReport.getProfileItems().add(ProfileItem.builder().className("BuildWordCloudTask").method("analyzeText").executionTime(System.currentTimeMillis() - startTime).build());
                smsBackupService.saveAnalyseReport( analyzeReport );
                //postProgress("build word cloud...", 25);
                long startTimeStep2 = System.currentTimeMillis();

                List<Word> wordList = wordCloudService.buildWordCloud(
                        analyzeReport.getWordMap()
                        , cloudDimension
                        , settings);

                analyzeReport.getProfileItems().add(ProfileItem.builder().className("BuildWordCloudTask").method("buildWordCloud").executionTime(System.currentTimeMillis() - startTimeStep2).build());
                analyzeReport.setCloudWordCount(wordList.size());
                analyzeReport.setCloudPlacedWordCount((int)wordList.stream().filter(Word::isPlaced).count());
                analyzeReport.setCloudNotPlacedWordCount((int)wordList.stream().filter(Word::isNotPlaced).count());

                // save last analyze report
                smsBackupService.saveAnalyseReport( analyzeReport );
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("%s", analyzeReport));
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("finished, exeTime=%s ms", (System.currentTimeMillis() - startTime)));
                //postProgress("finished building word list...", 10);
                // finally, publish result so word cloud view can pick up the word list and redraw the word cloud
                RxBus.getInstance().publish(
                        WordCloudEvent.builder()
                                .eventType(WordCloudEvent.WordCloudEventTypeEnum.UPDATE_MESSAGE)
                                .wordList(wordList)
                                .build());
            } catch (Exception e) {
                smsBackupService.profile(Collections.singletonList(ProfileItem.builder().className("BuildWordCloudTask").method("buildWordCloudEventBus").executionTime(System.currentTimeMillis() - startTime).exception(e.getMessage()).build()));
                Log.e(Utility.buildTag(getClass(), "buildWordCloudEventBus"), e.getMessage());
            }
        };
        executor.execute(buildWordCloudRunnable);
    }

    private void postProgress(String msg, int step) {
        RxBus.getInstance().publish(
                WordCloudEvent.builder()
                        .eventType(WordCloudEvent.WordCloudEventTypeEnum.PROGRESS)
                        .wordList(new ArrayList<>())
                        .progressMsg(msg)
                        .progressStep(step)
                        .build());
    }
}