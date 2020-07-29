package com.gunnarro.android.ughme.ui.view;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;

public class WordCloudBuilder {

    private final static String TAG = WordCloudBuilder.class.getSimpleName();
    private final Random rnd = new Random();
    private static final int MAX_WORD_SIZE = 150;
    private static final int MIN_WORD_SIZE = 50;
    private final int width;
    private final int height;
    private final int centerX;
    private final int centerY;

    private static String buildTag(String tagName) {
        return new StringBuilder(TAG).append(".").append(tagName).toString();
    }

    /**
     * @param width  - length of x.axis, none negative values only
     * @param height - Length of y-axis, none negative values only
     */
    public WordCloudBuilder(int width, int height) {
        if (width < 0 || height < 0) {
            throw new RuntimeException("width and height must both be greater than 0!");
        }
        this.width = width;
        this.height = height;
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;
        Log.i(buildTag("WordCloudBuilder"), String.format("init, width=%s, height=%s", this.width, this.height));
    }

    public List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount) {
        Log.d(buildTag("buildWordCloud"), String.format("word map size=%s", wordMap.size()));
        Log.d(buildTag("buildWordCloud"), String.format("word map: %s", wordMap));

        int numberOfCollisions = 0;
        List<Word> wordList = new ArrayList<>();
        Random rand = new Random();
        List<Double> radians = getNumberList(25);
        int i = 0;
        Double rad = 0d;
        int radius = 0;
        Word previousWord = null;
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            int wordSize = determineWordSize(entry.getValue(), mostFrequentWordCount);
            // if rotated all round the circle, time increase radius and start a new circle
            // skip first element, which is the most used word and should always be at center
            if (previousWord != null || i / 25 > 1) {
                // increase radius with wordSize of the biggest word in the inner circle
                radius += 10;
                Log.d(buildTag("buildWordCloud"), String.format("new radius=%s, loop=%s", radius, i));
            }
            Log.d(buildTag("buildWordCloud"), String.format("start build, i=%s, key=%s, value=%s, wordSize=%s, radius=%s", i, entry.getKey(), entry.getValue(), wordSize, radius));
            Paint.Align align = Paint.Align.CENTER;
            // align most used word with center
            //if (i == 0) {
            //    align = Paint.Align.CENTER;
            //}
            Point startPoint = calculateXYCoordinates(radius, rad, entry.getKey());
            Paint wordPaint = createPaint(wordSize, align);
            // Need this only to determine the size of the text rectangle
            Rect wordRect = new Rect();
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), wordRect);
            Log.d(buildTag("buildWordCloud"), String.format("text bounds, word=%s ,rect=%s, width=%s(%s), height=%s(%s)", entry.getKey(), wordRect.toShortString(), wordRect.width(), wordRect.right - wordRect.left, wordRect.bottom - wordRect.top, wordRect.height()));
            // Offset the rectangle to the determined (left, top) position
            // The distance from the baseline to the center
            int baseLine = (int)((wordPaint.descent() + wordPaint.ascent())/2);
            wordRect.offsetTo(startPoint.x, startPoint.y);
            Log.d(buildTag("buildWordCloud"), String.format("word-rect i=%s, coordinates=%s, w=%s, h=%s, word=%s", i, wordRect.toShortString(), wordRect.width(), wordRect.height(), entry.getKey()));
            Word newWord = Word.builder()
                    .setText(entry.getKey())
                    .setSize(wordSize)
                    .setCount(entry.getValue())
                    .setPaint(wordPaint)
                    .setRect(wordRect)
                    .build();

            Log.d(buildTag("buildWordCloud"), String.format("rect radius=%d, angle=%s, wordSize=%s, degrees=%s, loop=%s, word=%s, x0=%s, y0=%s, w=%s, h=%s", radius, rad, wordSize, radians.size(), i, entry.getKey(), centerX, centerY, width, height));
            Log.d(buildTag("buildWordCloud"), String.format("rect word=%s, w=%s, h=%s, coordinates=%s", newWord.getText(), newWord.getRect().width(), newWord.getRect().height(), newWord.getRect().toShortString()));

            Word word = checkWordCollision(newWord, wordList, radius);
            if (word != null) {
                wordList.add(word);
            } else {
                numberOfCollisions++;
                Log.d(buildTag("buildWordCloud"), String.format("skipped word due to collision! word=%s, count=%s", newWord.getText(), newWord.getCount()));
            }
            Log.d(buildTag("buildWordCloud"), String.format("added new word! i=%s,  %s", i, newWord));
            previousWord = wordList.size() > 0 ? wordList.get(wordList.size() - 1) : null;
            i++;
        }
        List<Word> sortedWordList = wordList.stream()
                .sorted((w1, w2) -> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        sortedWordList.forEach(w -> Log.i(buildTag("buildWordCloud"), String.format("coordinates=%s, size=%s, word=%s, occurrences=%s", w.getRect().toShortString(), w.getSize(), w.getText(), w.getCount())));
        Log.d(buildTag("buildWordCloud"), String.format("finished! numberOfWords=%s, numberOfCollisions=%s, totalNumberOfWords=%s", wordList.size(), numberOfCollisions, wordMap.size()));
        return sortedWordList;
    }

    private Word checkWordCollision(Word word, final List<Word> wordList, int radius) {
        Random rand = new Random();
        List<Double> radianers = getNumberList(20);
        Rect colRect = checkCollision(word, wordList);
        int newRadius = radius;
        int attempts = 0;
        while (colRect != null && attempts < 6) {
            Log.d(buildTag("checkWordCollision"), String.format("rect=%s, colRect=%s", word.getRect().toShortString(), colRect.toShortString()));
            // increase radius
            //if (attempts % 3 == 0) {
            //    newRadius += colRect.height();
            //}
            //double newRad = radianers.get(rand.nextInt(radianers.size()));
            //Point newPoint = calculateXYCoordinates(newRadius, newRad, word.getText());
            Point offsetPoint = calculateOffset(word.getRect(), getRandomNumberInRange(0,3), word.getText() );
            //word.getRect().offset(offsetPoint.x, offsetPoint.y);
            word.getRect().set(colRect.left + offsetPoint.x, colRect.top + offsetPoint.y, colRect.right + offsetPoint.x, colRect.bottom + offsetPoint.y);
            int i = 1;
            while (!isInsideCanvasBounds(word.getRect())) {
                // revert previous offset
                word.getRect().offset(-offsetPoint.x, -offsetPoint.y);
                offsetPoint = calculateOffset(colRect, getRandomNumberInRange(0,3), word.getText() );
                word.getRect().offset(offsetPoint.x, offsetPoint.y);
                Log.d(buildTag("checkWordCollision"), String.format("exceeded canvas, try rotate one more time! i=%s, word=%s, coordinates=%s",i, word.getText(), word.getRect().toShortString()));
                i++;
                if (i > 3) {
                   // word.setRotationDegree(90 * getRandomNumberInRange(-1,1));
                    break;
                }
            }
            attempts++;
            colRect = checkCollision(word, wordList);
            Log.d(buildTag("checkWordCollision"), String.format("collision attempt: word=%s, attempts=%s", word.toString(), attempts));
        }

        if (colRect == null) {
            Log.d(buildTag("checkWordCollision"), String.format("collision fixed, word=%s, attempts=%s", word.toString(), attempts));
            return word;
        }
        Log.d(buildTag("checkWordCollision"), String.format("collision skipped word! word: %s, attempts=%s", word.toString(), attempts));
        return null;
    }

    /**
     * @return returns the rectangle the new word collided with
     */
    private Rect checkCollision(final Word newWord, final List<Word> list) {
        Log.d(buildTag("checkCollision"), String.format("newWord=%s, numberOfWordsInList=%s", newWord, list.size()));
        for (Word w : list) {
            if (!newWord.getText().equals(w.getText()) && Rect.intersects(newWord.getRect(), w.getRect())) {
                // return coordinates of the word that the new word collision with
                Log.d(buildTag("checkCollision"), String.format("collision! wordListSize=%s, newWord=%s, newRect=%s, existingWord=%s, existingRect=%s", list.size(), newWord.getText(), newWord.getRect().toShortString(), w.getText(), w.getRect().toShortString()));
                // return the coordinate to the word that the new word collided with
                return w.getRect();
            }
        }
        return null;
    }

    private Point calculateXYCoordinates(int radius, Double theta, String word) {
        int eclipseFactor = 5;
        // convert from polar to cartesian coordinates when calculating
        double x = centerX + (radius * Math.cos(theta));
        double y = centerY + (radius * Math.sin(theta));
        Rect rect = new Rect((int) x, (int) y, 0, 0);
        Log.d(buildTag("calculateCoordinates"), String.format("coordinates, word=%s, coordinates=%s, radius=%s, theta=%s", word, rect.toShortString(), radius, theta));
        return new Point((int) x, (int) y);
    }

    /**
     * 0 = move below, y - height + random x between x.left and x.right
     * 1 = move to left, x + 10 + random y between y.top and y.bottom
     * 2 = move above, y + 10 + random x between x.left and x.right
     * 3 = move to right, x - width + random y between y.top and y.bottom
     */
    private Point calculateOffset(Rect rect, int direction, String word) {
        if (rect == null) {
            return new Point(0, 0);
        }
        String moveTo= null;
        int offsetLeft = 0;
        int offsetTop = 0;
        if (direction == 0) { // below
            offsetLeft = getRandomNumberInRange(0, 3);
            offsetTop = rect.height();
            moveTo = "below";
        } else if (direction == 1) { // right
            offsetLeft = rect.width();// + rect.right + 10;
            offsetTop = getRandomNumberInRange(0, 3);
            moveTo = "right";
        } else if (direction == 2) { // above
            offsetLeft = getRandomNumberInRange(0, 3);
            offsetTop = - rect.height(); //rect.top - rect.height() -10;
            moveTo = "above";
        } else if (direction == 3) { // left
            offsetLeft = - rect.width(); //rect.left - rect.width() + 10;
            offsetTop = getRandomNumberInRange(0, 3);
            moveTo = "left";
        }
        Log.d(buildTag("calculateXYCoordinatesNew"), String.format("generate coordinates, word=%s, moveTo=%s, from (%s) to (%s, %s)", word, moveTo, rect.toShortString(), offsetLeft, offsetTop));
        return new Point( offsetLeft, offsetTop);
    }

    Random r = new Random();
    private int getRandomNumberInRange(int min, int max) {
        Log.d(TAG, String.format("min=%s, max=%s", min, max));
        OptionalInt i = r.ints(min, (max + 1)).findFirst();
        if (i.isPresent()) {
            return i.getAsInt();
        }
        return -1;
    }

    private List<Double> getNumberList(int n) {
        List<Double> randomNumbers = new ArrayList<>();
        double step = 2 * Math.PI / n;
        for (double theta = 0; theta < 2 * Math.PI; theta += step) {
            randomNumbers.add(theta);
        }
        Log.d(buildTag("getNumberList"), String.format("random radians=%s", randomNumbers.size()));
        return randomNumbers;
    }

    /**
     * Determine the font size.
     */
    private int determineWordSize(int wordCount, int highestWordCount) {
        int wordSize = (int) (((float) wordCount / (float) highestWordCount) * MAX_WORD_SIZE);
        Log.d(buildTag("determineWordSize"), String.format("wordSize=%s", wordSize));
        if (wordSize < MIN_WORD_SIZE) {
            wordSize = MIN_WORD_SIZE;
        } else if (wordSize > MAX_WORD_SIZE) {
            wordSize = MAX_WORD_SIZE;
        }
        Log.d(buildTag("determineWordSize"), String.format("wordCount=%s, highestWordCount=%s, wordSize=%s", wordCount, highestWordCount, wordSize));
        return wordSize;
    }

    /**
     * Create paint object
     */
    private Paint createPaint(int wordSize, Paint.Align align) {
        Paint wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setStyle(Paint.Style.FILL);
        wordPaint.setTypeface(Typeface.DEFAULT);
        wordPaint.setARGB(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        wordPaint.setTextSize(wordSize);
        if (align != null) {
            wordPaint.setTextAlign(align);
        } else {
            wordPaint.setTextAlign(Paint.Align.LEFT);
        }

        Log.d(buildTag("createPaint"), String.format("wordSize=%s", wordSize));
        return wordPaint;
    }

    /**
     * check if rect exceeded canvas width and height
     *
     * @return true if exceed, false if not
     */
    public boolean isInsideCanvasBounds(Rect rect) {
        // check for negative values
        if (rect.left < 0 || rect.top < 0 || rect.right < 0 || rect.bottom < 0) {
            return false;
        }
        // check is inside canvas bounds
        if (rect.left > this.width || rect.right > this.width ) {
            Log.d(buildTag("isInsideCanvasBounds"), String.format("width exceeded canvas bounds! rect=%s, canvas w=%s, h=%s", rect.toShortString(), this.width, this.height));
            return false;
        }

        if ( rect.top > this.height || rect.bottom > this.height ) {
            Log.d(buildTag("isInsideCanvasBounds"), String.format("height exceeded canvas bounds! rect=%s, canvas w=%s, h=%s", rect.toShortString(), this.width, this.height));
            return false;
        }
        return true;
    }
}
