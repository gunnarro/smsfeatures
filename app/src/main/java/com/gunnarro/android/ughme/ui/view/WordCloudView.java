package com.gunnarro.android.ughme.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.gunnarro.android.ughme.analyze.TextAnalyzer;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.sms.Sms;

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
import java.util.stream.Collectors;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class WordCloudView extends View {

    private static final String TAG = WordCloudView.class.getSimpleName();

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    private WordCloudEvent event = WordCloudEvent.builder().setEventType(WordCloudEvent.WordCloudEventTypeEnum.MESSAGE).smsTypeAll().setValue("(.*)").build();
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
        Log.d(buildTag("onDraw"), String.format("Finished! words=%s", wordCloudList.size()));
    }

    /**
     *
     */
    private void updateCanvas(Canvas canvas, Word word) {
        // save current state of the canvas
        int state = canvas.save();
        if (word.getRotationDegree() > 0) {
            canvas.rotate(word.getRotationDegree(),
                    word.getRect().left + word.getRect().height(),
                    word.getRect().top + word.getRect().height());
        }
        // Draw the text, with origin at (x,y), using the specified paint
        canvas.drawText(word.getText(), word.getX(), word.getY(), word.getPaint());
        //canvas.drawRect(word.getRect(), word.getPaint());
       // canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 25, createPaint(Color.GREEN));
        //undo the rotate, if rotated
        if (word.getRotationDegree() > 0) {
            // Revert the Canvas's adjustments back to the last time called save() was called
            canvas.restore();
        }
        canvas.save();
        Log.d(buildTag("updateCanvas"), String.format("canvas updated... %s", word.toString()));
    }

    private Paint createPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setColor(color);
        return paint;

    }

    /**
     *
     */
    private void init(int width, int height) {
        WordCloudBuilder wordCloudBuilder = new WordCloudBuilder(width, height);
        TextAnalyzer textAnalyzer = new TextAnalyzer();
        // StringBuilder smsPlainText = new StringBuilder();
        // smsPlainText.append("Dette, dette, dette er kun en enhets test, og dette er ingenting å tulle med, spør du meg. antall enhets tester er kun 1");
        textAnalyzer.analyzeText(getSmsBackupAsText(), null);
        wordCloudList = wordCloudBuilder.buildWordCloud(textAnalyzer.getWordCountMap(100), textAnalyzer.getHighestWordCount());
        textAnalyzer.printReport();
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
            // get distinct mobile numbers
            List<String> mobileNumbers = smsList.stream()
                    .map(s -> s.getAddress())
                    .collect(Collectors.toList());

            //smsList.forEach(s -> Log.d(buildTag("getSmsBackupAsText"), String.format("sms backup: %s", s.toString())));
            switch (event.getEventType()) {
                case NUMBER:
                    String numbers = smsList.stream()
                            .map(s -> s.getAddress())
                            .collect(Collectors.joining(" "));
                    smsPlainTxt.append(numbers);
                    break;
                case DATE:
                    DateFormat dateFormat = new SimpleDateFormat("ddMMYYYY", Locale.getDefault());
                    smsList.forEach(s -> smsPlainTxt.append(dateFormat.format(new Date(s.getTimeMs()))).append(" "));
                    break;
                case MESSAGE:
                default:
                    String smsTxt = smsList.stream()
                            .filter(s -> s.getAddress().matches(event.getValue()) && s.getType().matches(event.getSmsType()))
                            .map(s -> s.getBody())
                            .collect(Collectors.joining(" "));
                    smsPlainTxt.append(smsTxt);
            }
        } catch (FileNotFoundException e) {
            Log.e(buildTag("getSmsBackupAsText"), String.format("sms backup file not found! error: %s", e.getMessage()));
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
                Log.d(buildTag("getInputObserver.onSubscribe"), "getInputObserver.onSubscribe:");
            }

            @Override
            public void onNext(Object obj) {
                Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    event = (WordCloudEvent) obj;
                    Log.d(buildTag("getInputObserver.onNext"), String.format("handle event: %s", event.toString()));
                    // refresh view
                    invalidate();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(buildTag("getInputObserver.onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(buildTag("getInputObserver.onComplete"), "");
            }
        };
    }
}