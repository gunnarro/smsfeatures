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
import com.gunnarro.android.ughme.utility.Utility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordCloudServiceImpl implements WordCloudService {

    protected final TreeWordPlacer wordPlacer;
    private final Random rnd = new Random();

    @Inject
    public WordCloudServiceImpl() {
        wordPlacer = new TreeWordPlacer();
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

    /**
     *
     * @param wordMap map sorted by highest occurrences of words
     * @param rectangleDimension holds dimension of the word cloud bitmap
     * @param settings holds word cloud settings
     * @return list of most used words
     */
    public List<Word> buildWordCloud(Map<String, Integer> wordMap, final Dimension rectangleDimension, Settings settings) {
        Log.i(Utility.buildTag(getClass(), "buildWordCloud"), String.format("number of words size=%s, rectangle: %s", wordMap.size(), rectangleDimension));
        Log.i(Utility.buildTag(getClass(),"buildWordCloud"), String.format("%s", settings));
        Log.d(Utility.buildTag(getClass(),"buildWordCloud"), String.format("word map: %s", wordMap.size()));

        if (rectangleDimension.getWidth() < 0 || rectangleDimension.getHeight() < 0) {
            throw new ApplicationException(String.format("width and height must both be greater than 0! rectangle: %s", rectangleDimension), null);
        }

        int highestWordCount = wordMap.values()
                .stream()
                .max(Comparator.comparing( Integer::valueOf ))
                .orElse(0);

        // reset previous build
        wordPlacer.reset();
        int numberOfCollisions = 0;
        float[] rotationAngles = {0, 90};
        Random rand = new Random();
        List<Word> wordList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            Log.i(Utility.buildTag(getClass(), ""), String.format("x2x word=%s, count=%s", entry.getKey(), entry.getValue()));
            int wordFontSize = determineWordFontSize(entry.getValue(), highestWordCount, settings.minWordFontSize, settings.maxWordFontSize);
            Paint wordPaint = createPaint(wordFontSize);
            Rect wordRect = new Rect();
            // Retrieve the text boundary box and store to bounds
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), wordRect);
            float textWidth = wordPaint.measureText(entry.getKey());

            if (wordRect.width() != textWidth) {
                Log.d(Utility.buildTag(getClass(),"buildWordCloud"), String.format("Text width not equal! word=%s, %s != %s", entry.getKey(), wordRect.width(), textWidth));
            }

            float textHeight = wordPaint.descent() - wordPaint.ascent();
            if (wordRect.height() != textHeight) {
                Log.d(Utility.buildTag(getClass(),"buildWordCloud"), String.format("Text height not equal! word=%s, %s != %s", entry.getKey(), wordRect.height(), textHeight));
            }

            Word newWord = Word.builder()
                    .text(entry.getKey())
                    .size(wordFontSize)
                    .rotationAngle(rotationAngles[rand.nextInt(1)])
                    .count(entry.getValue())
                    .paint(wordPaint)
                    .rect(wordRect)
                    .build();

            final Point startPoint = getStartingPoint(newWord, rectangleDimension);
            boolean placed = place(newWord, startPoint, settings.radiusStep, settings.offsetStep, rectangleDimension);

            if (placed) {
                newWord.setStatusPlaced();
                // Log.d(buildTag("buildWordCloud"), String.format("placed, word: %s, rect=%s,%s, placed-words=%s, count=%s, size=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfdWords, newWord.getCount(), newWord.getSize()));
            } else {
                newWord.setStatusNotPlaced();
                numberOfCollisions++;
                // Log.d(buildTag("buildWordCloud"), String.format("skipped, word: %s, rect=%s,%s collisions=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfCollisions));
            }
            wordList.add(newWord);
        }

        List<Word> sortedWordList = wordList.stream()
                .sorted((w1, w2) -> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        // sortedWordList.forEach(w -> Log.i(Utility.buildTag(getClass(),"buildWordCloud"), String.format("coordinates=%s, size=%s, word=%s, occurrences=%s", w.getRect().toShortString(), w.getSize(), w.getText(), w.getCount())));
        Log.i(Utility.buildTag(getClass(),"buildWordCloud"), String.format("finished! numberOfWords=%s, numberOfCollisions=%s, totalNumberOfWords=%s", wordList.size(), numberOfCollisions, wordMap.size()));
        return sortedWordList;
    }

    /**
     *
     * @param wordCount number of words for this query
     * @param highestWordCount highest number of occurrences for a word
     * @param minFontSize minimum allowed font size
     * @param maxFontSize maximum allowed font size
     * @return font size used for this word
     */
    private int determineWordFontSize(int wordCount, int highestWordCount, int minFontSize, int maxFontSize) {
        float wordFontSize = ((float) wordCount / (float) highestWordCount) * maxFontSize;
        Log.d(Utility.buildTag(getClass(),"determineWordSize"), String.format("wordFontSize=%s", wordFontSize));
        if (wordFontSize < minFontSize) {
            wordFontSize = minFontSize;
        } else if (wordFontSize > maxFontSize) {
            wordFontSize = maxFontSize;
        }
        Log.d(Utility.buildTag(getClass(),"determineWordSize"), String.format("wordCount=%s, highestWordCount=%s, wordFontSize=%s", wordCount, highestWordCount, wordFontSize));
        return (int) wordFontSize;
    }

    /**
     * Create paint object
     */
    private Paint createPaint(int fontSize) {
        Paint wordPaint = new Paint();
        // Eliminating sawtooth
        wordPaint.setAntiAlias(true);
        wordPaint.setStyle(Paint.Style.FILL);
        wordPaint.setTypeface(Typeface.DEFAULT);
        wordPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        wordPaint.setTextSize(fontSize * 10);//getResources().getDisplayMetrics().density);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        Log.d(Utility.buildTag(getClass(),"createPaint"), String.format("fontSize=%s", fontSize));
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
    public boolean place(Word word, final Point startPoint, final int radiusStep, final int offsetStep, final Dimension rectangleDimension) {
        final int maxRadius = computeRadius(rectangleDimension, startPoint);
        final Point position = new Point(word.getRect().left, word.getRect().top);
        // reset position
        position.x = 0;
        position.y = 0;
        Log.d(Utility.buildTag(getClass(),"place"), String.format("start, word=%s, word-position=%s, start-point=%s, max-radius=%s, rectDimension=(%s, %s)", word.getText(), position, startPoint, maxRadius, rectangleDimension.getWidth(), rectangleDimension.getHeight()));
        int radiusLoopCount = 0;
        int positionLoopCount = 0;
        long startTime = System.currentTimeMillis();
        for (int r = 0; r < maxRadius; r += radiusStep) {
            radiusLoopCount++;
            positionLoopCount = 0;
            for (int x = Math.max(-startPoint.x, -r); x <= Math.min(r, rectangleDimension.getWidth() - startPoint.x - 1); x += offsetStep) {
                positionLoopCount++;
                position.x = startPoint.x + x;
                final int offset = (int) Math.sqrt(r * r - x * x);
                // try positive root
                position.y = startPoint.y + offset;
                word.getRect().offsetTo(position.x, position.y);
                //Log.d(Utility.buildTag(getClass(),"place"), String.format("check-1, word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (position.y >= 0 && position.y < rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect(), rectangleDimension)) {
                    Log.d(Utility.buildTag(getClass(),"place"), String.format("finished, placed positive adjustment, word=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s, exeTime=%s", word.getText(), position, radiusLoopCount, positionLoopCount, (System.currentTimeMillis() - startTime)));
                    return true;
                }
                // try negative root (if offset != 0)
                position.y = startPoint.y - offset;
                word.getRect().offsetTo(position.x, position.y);
                //Log.d(Utility.buildTag(getClass(),"place"), String.format("check-2, word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (offset != 0 && position.y >= 0 && position.y < rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect(), rectangleDimension)) {
                    Log.d(Utility.buildTag(getClass(),"place"), String.format("finished, placed negative adjustment, word=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s, exeTime=%s", word.getText(), position, radiusLoopCount, positionLoopCount, (System.currentTimeMillis() - startTime)));
                    return true;
                }
            }
        }
        Log.d(Utility.buildTag(getClass(),"place"), String.format("finished, word not placed,  word=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s", word.getText(), position, radiusLoopCount, positionLoopCount));
        return false;
    }

    /**
     * x = left
     * y = top
     */
    private boolean canPlace(final String word, final Rect wordRect, final Dimension rectangleDimension) {
        // check if inside the background
        if (wordRect.top < 0 || wordRect.top + wordRect.height() > rectangleDimension.getHeight()) {
           // Log.d(Utility.buildTag(getClass(),"canPlace"), String.format("canPlace: NOT HEIGHT, word=%, rect-top=%s + rect-height=%s > height=%s", word, wordRect.top, wordRect.height(), rectangleDimension.getHeight()));
            return false;
        } else if (wordRect.left < 0 || wordRect.left + wordRect.width() > rectangleDimension.getWidth()) {
          //  Log.d(Utility.buildTag(getClass(),"canPlace"), String.format("canPlace: NOT WIDTH, word=%s, rect-left=%s + rect-width=%s > width=%s", word, wordRect.left, wordRect.width(), rectangleDimension.getWidth()));
            return false;
        }
        return wordPlacer.place(word, wordRect); // is there a collision with the existing words?
    }

    public Point getStartingPoint(final Word word, final Dimension rectangleDimension) {
        final int x = (rectangleDimension.getWidth() / 2) - (word.getRect().width() / 2);
        final int y = (rectangleDimension.getHeight() / 2) - (word.getRect().height() / 2);
        return new Point(x, y);
    }
}

