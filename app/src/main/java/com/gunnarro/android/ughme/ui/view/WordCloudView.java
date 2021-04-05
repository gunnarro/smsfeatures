package com.gunnarro.android.ughme.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.analyze.ProfileItem;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.BuildWordCloudTask;
import com.gunnarro.android.ughme.service.WordCloudService;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.utility.Utility;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class WordCloudView extends androidx.appcompat.widget.AppCompatImageView {

    @Inject
    SmsBackupServiceImpl smsBackupService;
    @Inject
    TextAnalyzerServiceImpl textAnalyzerService;
    @Inject
    WordCloudService wordCloudService;
    @Inject
    BuildWordCloudTask buildWordCloudTask;
    // canvas to hold the word cloud image
    private Canvas canvas;

    public WordCloudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // this will trig an event
        RxBus.getInstance().listen().subscribe(getInputObserver());
    }

    public WordCloudView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initCanvas(int width, int height) {
        Log.d(Utility.buildTag(getClass(), "initCanvas"), String.format("initialize word cloud bitmap, width=%s, height=%s", width, height));
        if (this.canvas == null) {
            clearDrawing();
            Log.d(Utility.buildTag(getClass(), "initCanvas"), String.format("initialized word cloud bitmap, width=%s, height=%s", width, height));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        super.onDraw(canvas);
        initCanvas(getWidth(), getHeight());
        Log.i(Utility.buildTag(getClass(), "onDraw"), String.format("Finished! width=%s, height=%s, exeTime=%s ms", getWidth(), getHeight(), (System.currentTimeMillis() - startTime)));
    }

    private void clearDrawing() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // Associate the bitmap to the ImageView.
        setImageBitmap(bitmap);
        // Create a Canvas with the bitmap.
        this.canvas = new Canvas(bitmap);
        this.canvas.drawColor(getDrawingCacheBackgroundColor());
        Log.d(Utility.buildTag(getClass(), "clearDrawing"), "clear current drawing");
    }

    private void updateCanvasText(Word word) {
        // save current state of the canvas
        if (word.getRotationAngle() > 0) {
            this.canvas.rotate(word.getRotationAngle(),
                    word.getRect().left,
                    word.getRect().top);
        }
        int textWidth = (int) word.getPaint().measureText(word.getText());
        StaticLayout sLayout = StaticLayout.Builder.obtain(word.getText(), 0, word.getText().length(), new TextPaint(word.getPaint()), textWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .setLineSpacing(0, 1)
                .setIndents(new int[]{0}, new int[]{0})
                .setMaxLines(1)
                .build();

        //Log.d(Utility.buildTag(getClass(),"updateCanvasText"), String.format("padding=%s, baseline=%s, ascent=%s", sLayout.getBottomPadding(), sLayout.getLineBaseline(0), word.getPaint().getFontMetrics().descent));
        this.canvas.save();
        this.canvas.translate(word.getX() + textWidth / 2, word.getY() - word.getPaint().getFontMetrics().descent);
        sLayout.draw(canvas);
        this.canvas.restore();

        //Log.d(Utility.buildTag(getClass(),"updateCanvasText"), String.format("text= %s, %s, rect= %s, %s", word.getX(), word.getY(), word.getRect().left, word.getRect().top));
        //undo the rotate, if rotated
        if (word.getRotationAngle() > 0) {
            // Revert the Canvas's adjustments back to the last time called save() was called
            this.canvas.restore();
        }
        this.canvas.save();
        Log.d(Utility.buildTag(getClass(), "updateCanvasText"), String.format("canvas updated...word=%s", word.getText()));
    }

    private void runOnUiThread(final List<Word> wordList) {
        new Thread(new Runnable() {
            public void run() {
                post(updateViewTask(wordList));
            }
        }).start();

    }

    /**
     * Note that you cannot update the UI from any thread other than the UI thread or the "main" thread.
     */
    private Runnable updateViewTask(final List<Word> wordList) {
        return () -> {
            long startTime = System.currentTimeMillis();
            try {
                RxBus.getInstance().publish(
                        WordCloudEvent.builder()
                                .eventType(WordCloudEvent.WordCloudEventTypeEnum.PROGRESS)
                                .wordList(new ArrayList<>())
                                .progressMsg("draw word cloud view...")
                                .progressStep(10)
                                .build());
                clearDrawing();

                // filter out not placed words
                List<Word> placedWords = wordList.stream().filter(Word::isPlaced).collect(Collectors.toList());

                // simply place each word on the bitmap
                placedWords.forEach(this::updateCanvasText);
                postInvalidate();
                smsBackupService.profile(Collections.singletonList(ProfileItem.builder().className("WordCloudView").method("updateViewTask").executionTime(System.currentTimeMillis() - startTime).build()));
                Log.i(Utility.buildTag(getClass(), "updateViewTask"), String.format("words=%s, exeTime=%s ms", placedWords.size(), (System.currentTimeMillis() - startTime)));
            } catch (Exception e) {
                e.printStackTrace();
                smsBackupService.profile(Collections.singletonList(ProfileItem.builder().className("WordCloudView").method("updateViewTask").executionTime(System.currentTimeMillis() - startTime).exception(e.getMessage()).build()));
                throw new ApplicationException(e.getMessage(), e);
            }
        };
    }


    // Listen to RxJava publish event
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(Utility.buildTag(getClass(), "onSubscribe"), "");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                //Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    WordCloudEvent event = (WordCloudEvent) obj;
                    if (event.isUpdateEvent()) {
                        Log.d(Utility.buildTag(getClass(), "onNext"), String.format("handle word cloud event: %s", event.toString()));
                        if (Build.VERSION.SDK_INT < 26) {
                            runOnUiThread(event.getWordList());
                        } else {
                            Executors.newSingleThreadExecutor().execute(updateViewTask(event.getWordList()));
                        }
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(Utility.buildTag(getClass(), "onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(Utility.buildTag(getClass(), "onComplete"), "");
            }
        };
    }
}