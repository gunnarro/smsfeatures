package com.gunnarro.android.ughme.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.gunnarro.android.ughme.R;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.WordCloudService;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class WordCloudView extends View {

    private static final String TAG = WordCloudView.class.getSimpleName();
    private static final int NUMBER_OF_WORDS = 150;

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    private static WordCloudEvent event = WordCloudEvent.builder().eventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE).smsTypeAll().setValue("(.*)").build();

    @Inject
    SmsBackupServiceImpl smsBackupService;
    @Inject
    TextAnalyzerServiceImpl textAnalyzerService;
    @Inject
    WordCloudService wordCloudService;

    private List<Word> wordList = new ArrayList<>();

    /**
     *
     */
    public WordCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    /**
     *
     */
    public WordCloudView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init(getWidth(), getHeight());
        for (int i = 0; i < wordList.size(); i++) {
            updateCanvasText(canvas, wordList.get(i));
        }
        RxBus.getInstance().listen().subscribe(getInputObserver());
        Log.d(buildTag("onDraw"), String.format("Finished! words=%s", wordList.size()));
    }

    /**
     *
     */
    private void updateCanvasText(Canvas canvas, Word word) {
        // save current state of the canvas
        if (word.getRotationAngle() > 0) {
            canvas.rotate(word.getRotationAngle(),
                    word.getRect().left + word.getRect().height(),
                    word.getRect().top + word.getRect().height());
        }
        // Draw the text, with origin at (x,y), using the specified paint
        canvas.drawText(word.getText(), word.getX(), word.getY(), word.getPaint());
        //undo the rotate, if rotated
        if (word.getRotationAngle() > 0) {
            // Revert the Canvas's adjustments back to the last time called save() was called
            canvas.restore();
        }
        canvas.save();
        Log.d(buildTag("updateCanvas"), String.format("canvas updated... %s", word.toString()));
    }

    /**
     *
     */
    private void init(int width, int height) {
        Log.d(TAG, "init view...");
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
            alertDialog.setView(R.layout.dlg_progress);
            BuildWordCloudTask task = new BuildWordCloudTask(alertDialog.setTitle("Build WordCloud").setCancelable(false).create()
                    , textAnalyzerService
                    , smsBackupService
                    , wordCloudService
                    , event);
            task.execute(width, height);
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void updateWordCloud(List<Word> list) {
        this.wordList = list;
    }


    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(buildTag("getInputObserver.onSubscribe"), "getInputObserver.onSubscribe:");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    event = (WordCloudEvent) obj;
                    Log.d(buildTag("getInputObserver.onNext"), String.format("handle event: %s", event.toString()));
                    // refresh view
                    invalidate();
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(buildTag("getInputObserver.onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(buildTag("getInputObserver.onComplete"), "");
            }
        };
    }

    /**
     * run the word cloud build as a background task
     */
    class BuildWordCloudTask extends AsyncTask<Integer, Void, List<Word>> {

        private final SmsBackupServiceImpl smsBackupService;
        private final TextAnalyzerServiceImpl textAnalyzerService;
        private final WordCloudService wordCloudService;
        private final WordCloudEvent event;

        private Dialog progressDialog;

        public BuildWordCloudTask(AlertDialog alertDialog,
                                  TextAnalyzerServiceImpl textAnalyzerService,
                                  SmsBackupServiceImpl smsBackupService,
                                  WordCloudService wordCloudService,
                                  WordCloudEvent event) {
            this.progressDialog = alertDialog;
            this.textAnalyzerService = textAnalyzerService;
            this.smsBackupService = smsBackupService;
            this.wordCloudService = wordCloudService;
            this.event = event;
        }

        @Override
        protected List<Word> doInBackground(Integer... values) {
            Log.d("BuildWordCloudTask", "start build word cloud background task");
            long startTimeMs = System.currentTimeMillis();
            textAnalyzerService.analyzeText(smsBackupService.getSmsBackupAsText(event.getValue(), event.getSmsType()), null);
            Log.d(TAG, textAnalyzerService.getReport(true).toString());
            List<Word> wordList = wordCloudService.buildWordCloud(textAnalyzerService.getWordCountMap(NUMBER_OF_WORDS), textAnalyzerService.getHighestWordCount());
            Log.d("BuildWordCloudTask", String.format("finished, buildTime=%s ms", (System.currentTimeMillis() - startTimeMs)));
            return wordList;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<Word> wordList) {
            updateWordCloud(wordList);
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }
}