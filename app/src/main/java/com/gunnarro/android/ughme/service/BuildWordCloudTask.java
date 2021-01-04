package com.gunnarro.android.ughme.service;

import android.os.AsyncTask;
import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.ui.dialog.ProgressInfoDialog;

import java.util.List;

/**
 * Run the word cloud build as a background task
 */

public class BuildWordCloudTask extends AsyncTask<Integer, Void, List<Word>> {

    private static final String TAG = BuildWordCloudTask.class.getSimpleName();


    SmsBackupServiceImpl smsBackupService;

    WordCloudService wordCloudService;

    TextAnalyzerServiceImpl textAnalyzerService;

    ProgressInfoDialog progressDialog;

    public BuildWordCloudTask() {
    }

    @Override
    protected List<Word> doInBackground(Integer... values) {
        Log.d("BuildWordCloudTask", "start build word cloud background task");
        long startTimeMs = System.currentTimeMillis();
        WordCloudEvent event = WordCloudEvent.builder().eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE).smsTypeAll().build();
        textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(event.getValue(), event.getSmsType()), null);
        Log.d(TAG, textAnalyzerService.getReport(true).toString());
        List<Word> wordList = wordCloudService.buildWordCloud(textAnalyzerService.getWordCountMap(200),
                textAnalyzerService.getHighestWordCount(),
                new Dimension(values[0], values[1]), new Settings());
        Log.d("BuildWordCloudTask", String.format("finished, buildTime=%s ms", (System.currentTimeMillis() - startTimeMs)));
        return wordList;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }


    @Override
    protected void onPostExecute(List<Word> result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}

