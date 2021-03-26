package com.gunnarro.android.ughme.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.observable.RxBus;
import com.gunnarro.android.ughme.observable.event.WordCloudEvent;
import com.gunnarro.android.ughme.service.BuildWordCloudTask;
import com.gunnarro.android.ughme.service.WordCloudService;
import com.gunnarro.android.ughme.service.impl.SmsBackupServiceImpl;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

@AndroidEntryPoint
public class WordCloudView extends androidx.appcompat.widget.AppCompatImageView {

    private static final String TAG = WordCloudView.class.getSimpleName();
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

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    private void initCanvas(int width, int height) {
        Log.d(TAG, String.format("initialize word cloud bitmap, width=%s, height=%s", width, height));
        if (this.canvas == null) {
            clearDrawing();
            Log.d(TAG, String.format("initialized word cloud bitmap, width=%s, height=%s", width, height));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long startTime = System.currentTimeMillis();
        super.onDraw(canvas);
        initCanvas(getWidth(), getHeight());
        Log.i(buildTag("onDraw"), String.format("Finished! width=%s, height=%s, time=%s ms, thread=%s", getWidth(), getHeight(), (System.currentTimeMillis() - startTime), Thread.currentThread().getName()));
    }

    private void clearDrawing() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // Associate the bitmap to the ImageView.
        setImageBitmap(bitmap);
        // Create a Canvas with the bitmap.
        this.canvas = new Canvas(bitmap);
        this.canvas.drawColor(getDrawingCacheBackgroundColor());
        Log.d(TAG, "clear current drawing");
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

        Log.d(TAG, String.format("padding=%s, baseline=%s, ascent=%s", sLayout.getBottomPadding(), sLayout.getLineBaseline(0), word.getPaint().getFontMetrics().descent));
        this.canvas.save();
        this.canvas.translate(word.getX() + textWidth / 2, word.getY() - word.getPaint().getFontMetrics().descent);
        sLayout.draw(canvas);
        this.canvas.restore();

        Log.d(TAG, String.format("text= %s, %s, rect= %s, %s", word.getX(), word.getY(), word.getRect().left, word.getRect().top));
        //undo the rotate, if rotated
        if (word.getRotationAngle() > 0) {
            // Revert the Canvas's adjustments back to the last time called save() was called
            this.canvas.restore();
        }
        this.canvas.save();
        Log.d(buildTag("updateCanvas"), String.format("canvas updated...thread=%s, %s", Thread.currentThread().getName(), word.toString()));
    }

    private Runnable updateViewTask(final List<Word> wordList) {
        return () -> {
            try {
                clearDrawing();
                Log.d(TAG, String.format("updateViewTask update canvas... thread: %s", Thread.currentThread().getName()));
                // simply place each word on the bitmap
                wordList.forEach(this::updateCanvasText);
                postInvalidate();
            } catch (Exception e) {
                e.printStackTrace();
                throw new ApplicationException(e.getMessage(), e);
            }
        };
    }

    // Listen to RxJava publish event
    private Observer<Object> getInputObserver() {
        return new Observer<Object>() {
            @Override
            public void onSubscribe(@NotNull Disposable d) {
                Log.d(buildTag("getInputObserver.onSubscribe"), "getInputObserver.onSubscribe");
            }

            @Override
            public void onNext(@NotNull Object obj) {
                //Log.d(buildTag("getInputObserver.onNext"), String.format("Received new data event of type %s", obj.getClass().getSimpleName()));
                if (obj instanceof WordCloudEvent) {
                    WordCloudEvent event = (WordCloudEvent) obj;
                    if (event.isUpdateEvent()) {
                        Log.d(buildTag(TAG + ".getInputObserver.onNext"), String.format("handle word cloud event: %s", event.toString()));
                        Executors.newSingleThreadExecutor().execute(updateViewTask(event.getWordList()));
                    }
                }
            }

            @Override
            public void onError(@NotNull Throwable e) {
                Log.e(buildTag(TAG + ".getInputObserver.onError"), String.format("%s", e.getMessage()));
            }

            @Override
            public void onComplete() {
                Log.d(buildTag("getInputObserver.onComplete"), "");
            }
        };
    }
}