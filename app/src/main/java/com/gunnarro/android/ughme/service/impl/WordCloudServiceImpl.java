package com.gunnarro.android.ughme.service.impl;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.service.WordCloudService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordCloudServiceImpl implements WordCloudService {

    public static final int CLOUD_MARGIN = 20;
    private final static String TAG = WordCloudServiceImpl.class.getSimpleName();
    protected final TreeWordPlacer wordPlacer;
    private final Random rnd = new Random();
    private Dimension rectangleDimension;

    @Inject
    public WordCloudServiceImpl() {
        wordPlacer = new TreeWordPlacer();
    }

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    /**
     * compute the maximum radius for the placing spiral
     *
     * @param rectangle the size of the background
     * @param start     the center of the spiral
     * @return the maximum useful radius
     */
    static int computeRadius(final Dimension rectangle, final Point start) {
        final int maxDistanceX = Math.max(start.x, rectangle.getWidth() - start.x) + 1;
        final int maxDistanceY = Math.max(start.y, rectangle.getHeight() - start.y) + 1;
        // we use the pythagorean theorem to determinate the maximum radius
        return (int) Math.ceil(Math.sqrt(maxDistanceX * maxDistanceX + maxDistanceY * maxDistanceY));
    }

    public List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount, Dimension rectangleDimension, Settings settings) {
        Log.i(buildTag("buildWordCloud"), String.format("number of words size=%s, rectangle: %s", wordMap.size(), rectangleDimension));
        Log.i(buildTag("buildWordCloud"), String.format("%s", settings));
        Log.d(buildTag("buildWordCloud"), String.format("word map: %s", wordMap.size()));

        if (rectangleDimension.getWidth() < 0 || rectangleDimension.getHeight() < 0) {
            throw new ApplicationException(String.format("width and height must both be greater than 0! rectangle: %s", rectangleDimension), null);
        }
        // reset previous build
        wordPlacer.reset();
        this.rectangleDimension = rectangleDimension;
        int numberOfCollisions = 0;
        float[] rotationAngles = {0, 90};
        Random rand = new Random();
        List<Word> wordList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            int wordFontSize = determineWordFontSize(entry.getValue(), mostFrequentWordCount, settings.minWordFontSize, settings.maxWordFontSize);
            Paint wordPaint = createPaint(wordFontSize, Paint.Align.CENTER);
            Rect wordRect = new Rect();
            // Retrieve the text boundary box and store to bounds
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), wordRect);
            float textWidth = wordPaint.measureText(entry.getKey());

            if (wordRect.width() != textWidth) {
                Log.d(TAG, String.format("Text width not equal! word=%s, %s != %s", entry.getKey(), wordRect.width(), textWidth));
            }

            float textHeight = wordPaint.descent() - wordPaint.ascent();
            if (wordRect.height() != textHeight) {
                Log.d(TAG, String.format("Text height not equal! word=%s, %s != %s", entry.getKey(), wordRect.height(), textHeight));
            }

            Word newWord = Word.builder()
                    .text(entry.getKey())
                    .size(wordFontSize)
                    .rotationAngle(rotationAngles[rand.nextInt(1)])
                    .count(entry.getValue())
                    .paint(wordPaint)
                    .rect(wordRect)
                    .build();

            final Point startPoint = getStartingPoint(newWord);
            boolean placed = place(newWord, startPoint, settings.radiusStep, settings.offsetStep);

            if (placed) {
                wordList.add(newWord);
                // Log.d(buildTag("buildWordCloud"), String.format("placed, word: %s, rect=%s,%s, placed-words=%s, count=%s, size=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfdWords, newWord.getCount(), newWord.getSize()));
            } else {
                numberOfCollisions++;
                // Log.d(buildTag("buildWordCloud"), String.format("skipped, word: %s, rect=%s,%s collisions=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfCollisions));
            }
        }

        List<Word> sortedWordList = wordList.stream()
                .sorted((w1, w2) -> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        sortedWordList.forEach(w -> Log.i(buildTag("buildWordCloud"), String.format("coordinates=%s, size=%s, word=%s, occurrences=%s", w.getRect().toShortString(), w.getSize(), w.getText(), w.getCount())));
        Log.i(buildTag("buildWordCloud"), String.format("finished! numberOfWords=%s, numberOfCollisions=%s, totalNumberOfWords=%s", wordList.size(), numberOfCollisions, wordMap.size()));
        return sortedWordList;
    }

    /**
     * Determine the font size.
     */
    private int determineWordFontSize(int wordCount, int highestWordCount, int minFontSize, int maxFontSize) {
        float wordFontSize = ((float) wordCount / (float) highestWordCount) * maxFontSize;
        Log.d(buildTag("determineWordSize"), String.format("wordSize=%s", wordFontSize));
        if (wordFontSize < minFontSize) {
            wordFontSize = minFontSize;
        } else if (wordFontSize > maxFontSize) {
            wordFontSize = maxFontSize;
        }
        Log.d(buildTag("determineWordSize"), String.format("wordCount=%s, highestWordCount=%s, wordSize=%s", wordCount, highestWordCount, wordFontSize));
        return (int) wordFontSize;
    }

    private int percent(int part, int total) {
        return (part / total) * 100;
    }

    private int percentageOf(int total, int percent) {
        return (total * percent) / 100;
    }

    /**
     * Create paint object
     */
    private Paint createPaint(int fontSize, Paint.Align align) {
        Paint wordPaint = new Paint();
        // Eliminating sawtooth
        wordPaint.setAntiAlias(true);
        wordPaint.setStyle(Paint.Style.FILL);
        wordPaint.setTypeface(Typeface.DEFAULT);
        wordPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        wordPaint.setTextSize(fontSize * 10);//getResources().getDisplayMetrics().density);
        if (align != null) {
            wordPaint.setTextAlign(align);
        } else {
            wordPaint.setTextAlign(Paint.Align.LEFT);
        }
        Log.d(buildTag("createPaint"), String.format("fontSize=%s", fontSize));
        return wordPaint;
    }

    /**
     * word=sveen, x=430, y=789, check=true
     * word=det, x=448, y=903, check=true
     * <p>
     * try to place in center, build out in a spiral trying to place words for N steps
     *
     * @param word       the word being placed
     * @param startPoint the place to start trying to place the word
     */
    public boolean place(Word word, final Point startPoint, int radiusStep, int offsetStep) {
        final int maxRadius = computeRadius(this.rectangleDimension, startPoint);
        final Point position = new Point(word.getRect().left, word.getRect().top);
        // reset position
        position.x = 0;
        position.y = 0;
        Log.d(TAG + ".place start", String.format("word=%s, position=%s, max-radius: %s, rect=%s,%s", word.getText(), position, maxRadius, rectangleDimension.getWidth(), rectangleDimension.getHeight()));
        for (int r = 0; r < maxRadius; r += radiusStep) {
            for (int x = Math.max(-startPoint.x, -r); x <= Math.min(r, this.rectangleDimension.getWidth() - startPoint.x - 1); x += offsetStep) {
                position.x = startPoint.x + x;
                final int offset = (int) Math.sqrt(r * r - x * x);
                // try positive root
                position.y = startPoint.y + offset;
                word.getRect().offsetTo(position.x, position.y);
                Log.d(TAG + ".place check-1", String.format("word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (position.y >= 0 && position.y < this.rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect())) {
                    return true;
                }
                // try negative root (if offset != 0)
                position.y = startPoint.y - offset;
                word.getRect().offsetTo(position.x, position.y);
                Log.d(TAG + ".place check-2", String.format("word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (offset != 0 && position.y >= 0 && position.y < this.rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect())) {
                    return true;
                }
                Log.d(buildTag("place"), String.format(" not placed, keep looping: %s, position: %s, r=%s", word.getText(), position, r));
            }
        }
        return false;
    }

    /**
     * x = left
     * y = top
     */
    private boolean canPlace(final String word, final Rect wordRect) {
        // check if inside the background
        if (wordRect.top < 0 || wordRect.top + wordRect.height() > rectangleDimension.getHeight()) {
            Log.d(TAG, String.format("canPlace: NOT, top=%s, %s, %s", wordRect.top, wordRect.height(), rectangleDimension.getHeight()));
            return false;
        } else if (wordRect.left < 0 || wordRect.left + wordRect.width() > rectangleDimension.getWidth()) {
            Log.d(TAG, String.format("canPlace: NOT left=%s, %s, %s", wordRect.left, wordRect.width(), rectangleDimension.getWidth()));
            return false;
        }
        return wordPlacer.place(word, wordRect); // is there a collision with the existing words?
    }

    public Point getStartingPoint(final Word word) {
        final int x = (rectangleDimension.getWidth() / 2) - (word.getRect().width() / 2);
        final int y = (rectangleDimension.getHeight() / 2) - (word.getRect().height() / 2);
        return new Point(x, y);
    }
}
