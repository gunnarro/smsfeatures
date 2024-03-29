package com.gunnarro.android.ughme.service.impl;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import com.gunnarro.android.ughme.exception.ApplicationException;
import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.model.report.ReportItem;
import com.gunnarro.android.ughme.service.WordCloudService;
import com.gunnarro.android.ughme.utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class WordCloudServiceImpl implements WordCloudService {

    protected final TreeWordPlacer wordPlacer;
    private final Random rnd = new Random();
    private final static int[] angleDegrees = {90, 270};

    @Inject
    public WordCloudServiceImpl() {
        wordPlacer = new TreeWordPlacer();
        rnd.setSeed(2345667);
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
     * @param wordItems          list sorted by highest occurrences of words
     * @param wordCloudDimension holds dimension of the word cloud bitmap
     * @param settings           holds word cloud settings
     * @return list of most used words, sorted by word occurrences
     */
    public List<Word> buildWordCloud(List<ReportItem> wordItems, final Dimension wordCloudDimension, Settings settings) {
        Log.i(Utility.buildTag(getClass(), "buildWordCloud"), String.format("number of words size=%s, rectangle: %s", wordItems.size(), wordCloudDimension));
        Log.i(Utility.buildTag(getClass(), "buildWordCloud"), String.format("%s", settings));
        Log.d(Utility.buildTag(getClass(), "buildWordCloud"), String.format("word map: %s", wordItems.size()));

        if (wordItems == null || wordItems.isEmpty()) {
            return new ArrayList<>();
        }

        if (wordCloudDimension.getWidth() < 0 || wordCloudDimension.getHeight() < 0) {
            throw new ApplicationException(String.format("width and height must both be greater than 0! rectangle: %s", wordCloudDimension), null);
        }

        int highestWordCount = wordItems.get(0).getCount();

        // reset previous build
        wordPlacer.reset();
        int numberOfCollisions = 0;
        List<Word> wordList = new ArrayList<>();
        for (int i = 0; i < wordItems.size(); i++) {
            ReportItem reportItem = wordItems.get(i);
            Log.i(Utility.buildTag(getClass(), "buildWordCloud"), String.format("word=%s, count=%s", reportItem.getWord(), reportItem.getCount()));
            int wordFontSize = determineWordFontSize(reportItem.getCount(), highestWordCount, settings.minWordFontSize, settings.maxWordFontSize);
            Paint wordPaint = createPaint(wordFontSize, settings.colorSchema, settings.fontType, reportItem.getCategory());
            Rect wordRect = new Rect();
            // Retrieve the text boundary box and store to bounds
            wordPaint.getTextBounds(reportItem.getWord(), 0, reportItem.getWord().length(), wordRect);
            int textWidth = (int)wordPaint.measureText(reportItem.getWord());

            if (wordRect.width() != textWidth) {
                Log.d(Utility.buildTag(getClass(), "buildWordCloud"), String.format("word=%s, Word rect width not equal word width! %s != %s", reportItem.getWord(), (int)wordRect.width(), textWidth));
            }

            int textHeight = (int)wordPaint.descent() - (int)wordPaint.ascent();
            if (wordRect.height() != textHeight) {
                Log.d(Utility.buildTag(getClass(), "buildWordCloud"), String.format("word=%s, Word rect height not equal word height! %s != %s", reportItem.getWord(), wordRect.height(), textHeight));
            }

            // build word
            Word newWord = Word.builder()
                    .text(reportItem.getWord())
                    .size(wordFontSize)
                    .rotate(settings.wordRotation)
                    .count(reportItem.getCount())
                    .paint(wordPaint)
                    .rect(wordRect)
                    .build();

            // starting point is always at center of the word cloud
            final Point startPoint = getStartingPoint(newWord, wordCloudDimension);
            // try to place the word, will loop until placed or not
            boolean placed = place(newWord, startPoint, settings.radiusStep, settings.offsetStep, wordCloudDimension);

            if (placed) {
                newWord.setStatusPlaced();
            } else {
                newWord.setStatusNotPlaced();
                numberOfCollisions++;
            }
            wordList.add(newWord);
        }

        List<Word> sortedWordList = wordList.stream()
                .sorted((w1, w2) -> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        // sortedWordList.forEach(w -> Log.i(Utility.buildTag(getClass(),"buildWordCloud"), String.format("coordinates=%s, size=%s, word=%s, occurrences=%s", w.getRect().toShortString(), w.getSize(), w.getText(), w.getCount())));
        wordPlacer.printAttempts();
        Log.i(Utility.buildTag(getClass(), "buildWordCloud"), String.format("finished! numberOfWords=%s, numberOfCollisions=%s, totalNumberOfWords=%s", (wordList.size() - numberOfCollisions), numberOfCollisions, wordItems.size()));
        return sortedWordList;
    }

    /**
     * @param wordCount        number of words for this query
     * @param highestWordCount highest number of occurrences for a word
     * @param minFontSize      minimum allowed font size
     * @param maxFontSize      maximum allowed font size
     * @return font size used for this word
     */
    private int determineWordFontSize(int wordCount, int highestWordCount, int minFontSize, int maxFontSize) {
        float wordFontSize = ((float) wordCount / (float) highestWordCount) * maxFontSize;
        Log.d(Utility.buildTag(getClass(), "determineWordSize"), String.format("wordFontSize=%s", wordFontSize));
        if (wordFontSize < minFontSize) {
            wordFontSize = minFontSize;
        } else if (wordFontSize > maxFontSize) {
            wordFontSize = maxFontSize;
        }
        Log.d(Utility.buildTag(getClass(), "determineWordSize"), String.format("wordCount=%s, highestWordCount=%s, wordFontSize=%s", wordCount, highestWordCount, wordFontSize));
        return (int) wordFontSize;
    }

    /**
     * Create paint object
     */
    private Paint createPaint(int fontSize, String colorSchema, String fontType, int category) {
        Paint wordPaint = new Paint();
        // Eliminating sawtooth
        wordPaint.setAntiAlias(true);
        wordPaint.setStyle(Paint.Style.FILL);

        if ("SINGLE_COLOR".equals(colorSchema)) {
            // use a fixed seed in order to ensure he the same random sequence is generated every time
            rnd.setSeed(category);
        } else if ("SINGLE_COLOR_PER_FONT_SIZE".equals(colorSchema)) {
            rnd.setSeed(fontSize);
        } else if ("SINGLE_COLOR_INBOX_OUTBOX".equals(colorSchema)) {
            rnd.setSeed(category);
        } else if ("SINGLE_COLOR_PER_MOBILE_NUMBER".equals(colorSchema)) {
            rnd.setSeed(23232323); // FIXME
        } else if ("MULTI_COLOR".equals(colorSchema)) {
            // for multicolor, generate a different color every time
            rnd.setSeed(System.nanoTime());
        }
        wordPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        switch (fontType) {
            case "SANS_SERIF":
                wordPaint.setTypeface(Typeface.SANS_SERIF);
                break;
            case "MONOSPACE":
                wordPaint.setTypeface(Typeface.MONOSPACE);
                break;
            case "SERIF":
                wordPaint.setTypeface(Typeface.SERIF);
                break;
            case "DEFAULT_BOLD":
                wordPaint.setTypeface(Typeface.DEFAULT_BOLD);
                break;
            default:
                wordPaint.setTypeface(Typeface.DEFAULT);
        }

        wordPaint.setTextSize(fontSize * 10);//getResources().getDisplayMetrics().density);
        wordPaint.setTextAlign(Paint.Align.CENTER);
        Log.d(Utility.buildTag(getClass(), "createPaint"), String.format("fontSize=%s, fontType=%s, colorSchema=%s", fontSize, fontType, colorSchema));
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
        Log.d(Utility.buildTag(getClass(), "place"), String.format("word=%s, word-position=%s, start-point=%s, max-radius=%s, rectDimension=(%s, %s)", word.getText(), position, startPoint, maxRadius, rectangleDimension.getWidth(), rectangleDimension.getHeight()));
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
                // try positive root, place below current word(s)
                position.y = startPoint.y + offset;
                word.getRect().offsetTo(position.x, position.y);
                Log.d(Utility.buildTag(getClass(), "place"), String.format("word=%s, x=%s, offset=%s, start.x=%s, start.y=%s", word.getText(), x, offset, position.x, position.y));
                //Log.d(Utility.buildTag(getClass(),"place"), String.format("check-1, word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (position.y >= 0 && position.y < rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect(), rectangleDimension)) {
                    Log.d(Utility.buildTag(getClass(), "place"), String.format("finished, placed positive adjustment, word=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s, exeTime=%s", word.getText(), position, radiusLoopCount, positionLoopCount, (System.currentTimeMillis() - startTime)));
                    return true;
                }

                // try negative root (if offset != 0), place above current word(s)
                position.y = startPoint.y - offset;
                word.getRect().offsetTo(position.x, position.y);
                //Log.d(Utility.buildTag(getClass(),"place"), String.format("check-2, word=%s, x=%s, y=%s", word.getText(), word.getRect().left, word.getRect().left));
                if (offset != 0 && position.y >= 0 && position.y < rectangleDimension.getHeight() && canPlace(word.getText(), word.getRect(), rectangleDimension)) {
                    Log.d(Utility.buildTag(getClass(), "place"), String.format("finished, placed negative adjustment, word=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s, exeTime=%s", word.getText(), position, radiusLoopCount, positionLoopCount, (System.currentTimeMillis() - startTime)));
                    return true;
                }

                // try rotate
                if (word.isRotate()) {
                    Random random = new Random();
                    // rotate 90 or 270 random
                    int rotateDegrees = angleDegrees[random.nextInt(angleDegrees.length)];
                    Rect rotatedRect = rotateRect(rotateDegrees, word.getRect(), word.getText());
                    word.getRect().set(rotatedRect.left, rotatedRect.top, rotatedRect.right, rotatedRect.bottom);
                    if (canPlace(word.getText(), word.getRect(), rectangleDimension)) {
                        word.setRotationAngle(rotateDegrees);
                        Log.d(Utility.buildTag(getClass(), "place"), String.format("word=%s, finished, placed rotated, degrees=%s, final-position=%s, radius-loop-count=%s, position-loop-count=%s, exeTime=%s", word.getText(), rotateDegrees, position, radiusLoopCount, positionLoopCount, (System.currentTimeMillis() - startTime)));
                        return true;
                    }
                }
            }
        }
        Log.d(Utility.buildTag(getClass(), "place"), String.format("word=%s, finished, word not placed, final-position=%s, radius-loop-count=%s, position-loop-count=%s", word.getText(), position, radiusLoopCount, positionLoopCount));
        return false;
    }

    /**
     * x = left
     * y = top
     */
    private boolean canPlace(final String word, final Rect wordRect, final Dimension rectangleDimension) {
        Log.d(Utility.buildTag(getClass(),"canPlace"), String.format("word=%s, rect=%s", word, wordRect.toShortString()));
        // check if inside the background
        if (wordRect.top < 0 || (wordRect.top + wordRect.height()) > rectangleDimension.getHeight()) {
            Log.d(Utility.buildTag(getClass(),"canPlace"), String.format("word=%s, NOT PLACED HEIGHT, rect-top=%s + rect-height=%s > height=%s", word, wordRect.top, wordRect.height(), rectangleDimension.getHeight()));
            return false;
        } else if (wordRect.left < 0 || (wordRect.left + wordRect.width()) > rectangleDimension.getWidth()) {
            Log.d(Utility.buildTag(getClass(),"canPlace"), String.format("word=%s, NOT PLACED WIDTH, rect-left=%s + rect-width=%s > width=%s", word, wordRect.left, wordRect.width(), rectangleDimension.getWidth()));
            return false;
        }
        return wordPlacer.place(word, wordRect); // is there a collision with the existing words?
    }

    /**
     * A words starting point is always at center of the word cloud
     *
     * @param word the word
     * @param wordCloudDimension the dimension of the word cloud
     * @return the x,y coordinates to start placing the word
     */
    public Point getStartingPoint(final Word word, final Dimension wordCloudDimension) {
        final int x = (wordCloudDimension.getWidth() / 2) - (word.getRect().width() / 2);
        final int y = (wordCloudDimension.getHeight() / 2) - (word.getRect().height() / 2);
        Log.d(Utility.buildTag(WordCloudServiceImpl.class, "getStartingPoint"), String.format("word=%s, x=%s, y=%s", word, x, y));
        return new Point(x, y);
    }

    /**
     * Convert rectangle coordinates to rotated rectangle coordinates where degree of rotation is specified by degrees parameter
     *
     * Use formula:
     * R(0,0),90∘​(x,y)=(−y,x)
     */
    public static Rect rotateRect(int degrees, final Rect wordRect, String word) {

        float[] rectangleCorners = new float[]{};
                /*{
                wordRect.left, wordRect.top, //left, top
                wordRect.right, wordRect.top, //right, top
                wordRect.right, wordRect.bottom, //right, bottom
                wordRect.left, wordRect.bottom //left, bottom
        };
                 */
        final RectF rectF = new RectF(wordRect);
        final Matrix matrix = new Matrix();
        matrix.setRotate(degrees, wordRect.centerX(), wordRect.centerY());
        matrix.mapRect(rectF);
        matrix.setTranslate(rectF.centerX(), rectF.centerY());
        // matrix.mapPoints(rectangleCorners);
//        matrix.getValues(rectangleCorners);
  //      Log.d(Utility.buildTag(WordCloudServiceImpl.class, "rotateRect"), String.format("word=%s, %s", rectangleCorners.length));
        Log.d(Utility.buildTag(WordCloudServiceImpl.class, "rotateRect"), String.format("word=%s, degrees=%s, left=%s, top=%s, right=%s, bottom=%s, centerX=%s, centerY=%s", word, degrees, wordRect.left, wordRect.top, wordRect.right, wordRect.bottom, wordRect.centerX(), wordRect.centerY()));
        Log.d(Utility.buildTag(WordCloudServiceImpl.class, "rotateRect"), String.format("word=%s, degrees=%s, left=%s, top=%s, right=%s, bottom=%s, centerX=%s, centerY=%s", word, degrees, rectF.left, rectF.top, rectF.right, rectF.bottom, rectF.centerX(), rectF.centerY()));
        return new Rect((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
    }

    public void saveWordCloudAsImage() {
        wordPlacer.saveTreeAsImage();
    }
}

