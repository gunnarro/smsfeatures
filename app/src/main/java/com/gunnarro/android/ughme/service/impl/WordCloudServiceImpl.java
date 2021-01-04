package com.gunnarro.android.ughme.service.impl;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.TreeWordPlacer;
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

    private final static String TAG = WordCloudServiceImpl.class.getSimpleName();
    private final Random rnd = new Random();
    private Dimension rectangleDimension;
    protected final TreeWordPlacer wordPlacer;

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    @Inject
    public WordCloudServiceImpl() {
        wordPlacer = new TreeWordPlacer();
    }

    public List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount, Dimension rectangleDimension, Settings settings) {
        Log.i(buildTag("buildWordCloud"), String.format("number of words size=%s, rectangle: %s", wordMap.size(), rectangleDimension));
        Log.i(buildTag("buildWordCloud"), String.format("%s", settings));

        //Log.d(buildTag("buildWordCloud"), String.format("word map: %s", wordMap));

        if (rectangleDimension.getWidth() < 0 || rectangleDimension.getHeight() < 0) {
            throw new ApplicationException(String.format("width and height must both be greater than 0! rectangle: %s", rectangleDimension), null);
        }
        // reset previous build
        wordPlacer.reset();
        this.rectangleDimension = rectangleDimension;
        int numberOfCollisions = 0;
        int numberOfdWords = 1;
        List<Word> wordList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            int wordFontSize = determineWordFontSize(entry.getValue(), mostFrequentWordCount, settings.minWordFontSize, settings.maxWordFontSize);
            Paint wordPaint = createPaint(wordFontSize, Paint.Align.CENTER);
            Rect wordRect = new Rect();
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), wordRect);
            Word newWord = Word.builder()
                    .setText(entry.getKey())
                    .setSize(wordFontSize)
                    .setCount(entry.getValue())
                    .setPaint(wordPaint)
                    .setRect(wordRect)
                    .build();

            final Point startPoint = getStartingPoint(newWord);
            boolean placed = place(newWord, startPoint, settings.radiusStep, settings.offsetStep);

            if (placed) {
                wordList.add(newWord);
                Log.d(buildTag("buildWordCloud"), String.format("placed, word: %s, rect=%s,%s, placed-words=%s, count=%s, size=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfdWords, newWord.getCount(), newWord.getSize()));
            } else {
                numberOfCollisions++;
                Log.d(buildTag("buildWordCloud"), String.format("skipped, word: %s, rect=%s,%s collisions=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfCollisions));
            }
            numberOfdWords++;
        }

        List<Word> sortedWordList = wordList.stream()
                .sorted((w1, w2) -> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        sortedWordList.forEach(w -> Log.i(buildTag("buildWordCloud"), String.format("coordinates=%s, size=%s, word=%s, occurrences=%s", w.getRect().toShortString(), w.getSize(), w.getText(), w.getCount())));
        Log.d(buildTag("buildWordCloud"), String.format("finished! numberOfWords=%s, numberOfCollisions=%s, totalNumberOfWords=%s", wordList.size(), numberOfCollisions, wordMap.size()));
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
    private Paint createPaint(int wordTextSize, Paint.Align align) {
        Paint wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setStyle(Paint.Style.FILL);
        wordPaint.setTypeface(Typeface.DEFAULT);
        wordPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        wordPaint.setTextSize(wordTextSize);
        if (align != null) {
            wordPaint.setTextAlign(align);
        } else {
            wordPaint.setTextAlign(Paint.Align.LEFT);
        }
        Log.d(buildTag("createPaint"), String.format("wordTextSize=%s", wordTextSize));
        return wordPaint;
    }


    /**
     * try to place in center, build out in a spiral trying to place words for N steps
     *
     * @param word       the word being placed
     * @param startPoint the place to start trying to place the word
     */
    public boolean place(Word word, final Point startPoint, int radiusStep, int offsetStep) {
        final int maxRadius = computeRadius(this.rectangleDimension, startPoint);
        final Point position = new Point((int) word.getX(), (int) word.getY());
        // reset position
        position.x = 0;
        position.y = 0;
        Log.d("place-start", String.format("word=%s, position=%s, max-radius: %s, rect=%s,%s", word.getText(), position, maxRadius, rectangleDimension.getWidth(), rectangleDimension.getHeight()));
        for (int r = 0; r < maxRadius; r += radiusStep) {
            for (int x = Math.max(-startPoint.x, -r); x <= Math.min(r, this.rectangleDimension.getWidth() - startPoint.x - 1); x += offsetStep) {
                position.x = startPoint.x + x;
                final int offset = (int) Math.sqrt(r * r - x * x);
                // try positive root
                position.y = startPoint.y + offset;
                //Log.d("place-check-1", String.format("word=%s, x=%s, y=%s, check=%s", word.getText(), position.x, position.y, (position.y < this.rectangleDimension.getHeight())));
                word.getRect().offsetTo(position.x, position.y);
                if (position.y >= 0 && position.y < this.rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect())) {
                    return true;
                }

                // try negative root (if offset != 0)
                position.y = startPoint.y - offset;
                //  Log.d("place-check-2", String.format("word=%s, x=%s, y=%s, check=%s", word.getText(), position.x, position.y, (position.y < this.rectangleDimension.getHeight())));
                word.getRect().offsetTo(position.x, position.y);
                if (offset != 0 && position.y >= 0 && position.y < this.rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect())) {
                    return true;
                }
                // Log.d(buildTag("place"), String.format("try to place: %s, position: %s, r=%s", word.getText(), position, r));
            }
        }
        return false;
    }

    /**
     * x = left
     * y = top
     *
     * @param word
     * @param wordRect
     * @return
     */
    private boolean canPlace(final String word, final Rect wordRect) {
        // are we inside the background?
        if (wordRect.top < 0 || wordRect.top + wordRect.height() > rectangleDimension.getHeight()) {
            return false;
        } else if (wordRect.left < 0 || wordRect.left + wordRect.width() > rectangleDimension.getWidth()) {
            return false;
        }
        return wordPlacer.place(word, wordRect); // is there a collision with the existing words?
    }

    /**
     * compute the maximum radius for the placing spiral
     *
     * @param rectangle the size of the backgound
     * @param start     the center of the spiral
     * @return the maximum usefull radius
     */
    static int computeRadius(final Dimension rectangle, final Point start) {
        final int maxDistanceX = Math.max(start.x, rectangle.getWidth() - start.x) + 1;
        final int maxDistanceY = Math.max(start.y, rectangle.getHeight() - start.y) + 1;
        // we use the pythagorean theorem to determinate the maximum radius
        return (int) Math.ceil(Math.sqrt(maxDistanceX * maxDistanceX + maxDistanceY * maxDistanceY));
    }

    public Point getStartingPoint(final Word word) {
        final int x = (rectangleDimension.getWidth() / 2) - (word.getRect().width() / 2);
        final int y = (rectangleDimension.getHeight() / 2) - (word.getRect().height() / 2);
        return new Point(x, y);
    }
}
