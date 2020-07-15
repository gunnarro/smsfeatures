package com.gunnarro.android.ughme.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.analyze.TextAnalyzer;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.sms.Sms;
import com.mordred.wordcloud.WordCloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class WordCloudView extends View {

    private static final String TAG = WordCloud.class.getSimpleName();

    private WordCloudFragment.WordCloudTypeEnum type = WordCloudFragment.WordCloudTypeEnum.MESSAGE;
    private List<Word> wordCloudList;

    public WordCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WordCloudView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        init(canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < wordCloudList.size(); i++) {
            updateCanvas(canvas, wordCloudList.get(i));
        }

        RxBus.getInstance().listen().subscribe(getInputObserver());
        Log.d(TAG, String.format("Finished! words=%s", wordCloudList.size()));
    }

    /**
     *
     */
    private void updateCanvas(Canvas canvas, Word word) {
        // save current state of the canvas
        //int state = canvas.save();
        if (word.getRotationDegree() > 0) {
            canvas.rotate(word.getRotationDegree());
        }
        canvas.drawText(word.getText(), word.getX(), word.getY(), word.getPaint());
        //undo the rotate, if rotated
        if (word.getRotationDegree() > 0) {
            // Revert the Canvas's adjustments back to the last time called save() was called
            canvas.restore();
        }
        Log.d(TAG, String.format("canvas updated... x=%s, y=%s, size=%s, word=%s, count=%s", word.getX(), word.getY(), word.getSize(), word.getText(), word.getCount()));
    }

    /**
     *
     */
    private void init(int width, int height) {
        WordCloudBuilder wordCloudBuilder = new WordCloudBuilder(width, height);
        TextAnalyzer textAnalyzer = new TextAnalyzer("Sms Word Cloud");
        StringBuilder smsPlainText = new StringBuilder();
        smsPlainText.append("Dette, dette, dette er kun en enhets test, og dette er ingenting å tulle med, spør du meg. antall enhets tester er kun 1");
        textAnalyzer.analyzeText(getSmsBackupAsText(), null);
        wordCloudList = wordCloudBuilder.buildWordCloud(textAnalyzer.getWordCountMap(1000), textAnalyzer.getHighestWordCount());
        Log.d(TAG, String.format("init, words=%s", wordCloudList.size()));
    }

    /**
     *
     */
    private String getSmsBackupAsText() {
        Gson gson = new GsonBuilder().setLenient().create();
        Type smsListType = new TypeToken<ArrayList<Sms>>() {
        }.getType();
        StringBuilder smsPlainTxt = new StringBuilder();
        try {
            File f = new File(getSmsBackupFilePath());
            List<Sms> smsList = gson.fromJson(new FileReader(f.getPath()), smsListType);
           // smsList.forEach(s -> Log.d(TAG, String.format("sms backup: %s", s.toString())));
            switch (type) {
                case NUMBER:
                    smsList.forEach(s -> smsPlainTxt.append(s.getAddress()).append(" "));
                    break;
                case DATE:
                    DateFormat dateFormat = new SimpleDateFormat("ddMMYYYY", Locale.getDefault());
                    smsList.forEach(s -> smsPlainTxt.append(dateFormat.format(new Date(s.getTimeMs()))).append(" "));
                    break;
                default:
                    smsList.forEach(s -> smsPlainTxt.append(s.getBody()).append(" "));
            }
        } catch (FileNotFoundException e) {
            Log.d(TAG, String.format("sms backup file not found! error: %s", e.getMessage()));
            return null;
        }
        return smsPlainTxt.toString();
    }

    /**
     *
     */
    private String getSmsBackupFilePath() {
        return String.format("%s/sms-backup-all.json", getContext().getFilesDir().getPath());
    }

    // Get RxJava input observer instance
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "getInputObserver.onSubscribe:");
            }

            @Override
            public void onNext(Object obj) {
                Log.d(TAG, String.format("getInputObserver.onNext: Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudFragment.WordCloudTypeEnum) {
                    type = (WordCloudFragment.WordCloudTypeEnum)obj;
                    Log.d(TAG, String.format("getInputObserver.onNext: switch word cloud type to %s", type.name()));
                    // refresh view
                    invalidate();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, String.format("getInputObserver.onError: %s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "getInputObserver.onComplete:");
            }
        };
    }
}
