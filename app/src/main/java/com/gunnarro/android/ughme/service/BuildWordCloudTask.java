package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.report.ProfileItem;
import com.gunnarro.android.ughme.model.report.ReportItem;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
        Log.d(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("Start build word cloud task... %s", settings));
        Runnable buildWordCloudRunnable = () -> {
            long startTime = System.currentTimeMillis();
            try {
                postProgress("analyse sms content...", 10);
                Map<String, String> map = smsBackupService.getSmsBackupAsText(contactName, smsType);
                Log.d(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("messages map: %s, inbox=%s, outbox=%s", map, map.get(WordCloudEvent.MESSAGE_TYPE_INBOX), map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX)));
                AnalyzeReport inboxAnalyzeReport = textAnalyzerService.analyzeText(map.get(WordCloudEvent.MESSAGE_TYPE_INBOX), Sms.INBOX, settings.wordMatchRegex, settings.numberOfWords, settings.minWordOccurrences);
                AnalyzeReport outboxAnalyzeReport = textAnalyzerService.analyzeText(map.get(WordCloudEvent.MESSAGE_TYPE_OUTBOX), Sms.OUTBOX, settings.wordMatchRegex, settings.numberOfWords, settings.minWordOccurrences);

                inboxAnalyzeReport.getProfileItems().add(ProfileItem.builder().className("BuildWordCloudTask").method("analyzeText").executionTime(System.currentTimeMillis() - startTime).build());

                List<ReportItem> items = new ArrayList<>();
                items.addAll(inboxAnalyzeReport.getReportItems());
                items.addAll(outboxAnalyzeReport.getReportItems());
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("inbox words=%s, outbox words=%s", inboxAnalyzeReport.getReportItems(), outboxAnalyzeReport.getReportItems()));
                List<ReportItem> reportItems = items.stream().sorted(Comparator.comparing(ReportItem::getCount).reversed()).limit(settings.numberOfWords).collect(Collectors.toList());

                List<Word> wordList = wordCloudService.buildWordCloud(
                        reportItems
                        , cloudDimension
                        , settings);

                saveAnalyseReport(inboxAnalyzeReport, wordList, System.currentTimeMillis() - startTime);
             //   Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("%s", analyzeReport));
                Log.i(Utility.buildTag(getClass(), "buildWordCloudEventBus"), String.format("finished, words=%s, exeTime=%s ms", wordList.size(), (System.currentTimeMillis() - startTime)));
                postProgress("finished building word list...", 15);
                // finally, publish result so word cloud view can pick up the word list and redraw the word cloud
                RxBus.getInstance().publish(
                        WordCloudEvent.builder()
                                .eventType(WordCloudEvent.WordCloudEventTypeEnum.UPDATE_MESSAGE)
                                .wordList(wordList.stream().filter(Word::isPlaced).collect(Collectors.toList()))
                                .animationInterval(settings.wordAnimationInterval)
                                .build());

            } catch (Exception e) {
                smsBackupService.profile(Collections.singletonList(ProfileItem.builder().className("BuildWordCloudTask").method("buildWordCloudEventBus").executionTime(System.currentTimeMillis() - startTime).exception(e.getMessage()).build()));
                e.printStackTrace();
                Log.e(Utility.buildTag(getClass(), "buildWordCloudEventBus"), e.getMessage());
            }
        };
        executor.execute(buildWordCloudRunnable);
    }

    private void saveAnalyseReport(AnalyzeReport analyzeReport, List<Word> wordList, long exeTime) {
        wordList.forEach(w -> analyzeReport.getReportItems().forEach(r -> updateStatus(r, w)));
        analyzeReport.getProfileItems().add(ProfileItem.builder().className("BuildWordCloudTask").method("buildWordCloud").executionTime(exeTime).build());
        analyzeReport.setCloudWordCount(wordList.size());
        analyzeReport.setCloudPlacedWordCount((int) wordList.stream().filter(Word::isPlaced).count());
        analyzeReport.setCloudNotPlacedWordCount((int) wordList.stream().filter(Word::isNotPlaced).count());
        smsBackupService.saveAnalyseReport(analyzeReport);
    }

    private void updateStatus(ReportItem r, Word w) {
        r.setStatus(w.isPlaced());
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