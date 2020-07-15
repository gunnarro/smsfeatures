package com.gunnarro.android.ughme.ui.view;

import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.mordred.wordcloud.WordCloud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class WordCloudBuilder {

    private static final String TAG = WordCloud.class.getSimpleName();
    private final Random rnd = new Random();
    private static final int MAX_WORD_SIZE = 150;
    private static final int MIN_WORD_SIZE = 50;

    private final int width;
    private final int height;
    private final int centerX;
    private final int centerY;

    private List<String> xyCoordinateList = new ArrayList<>();

    /**
     *
     * @param width - length of x.axis, none negative values only
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
        Log.i(TAG, String.format("init, width=%s, height=%s", this.width, this.height));
    }

    public List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount) {
        Log.d(TAG, String.format("word map size=%s", wordMap.size()));
        Log.d(TAG, String.format("word map: %s", wordMap));

        int numberOfCollisions = 0;
        List<Word> wordList = new ArrayList<>();
        Random rand = new Random();
        List<Double> radians = getNumberList(20);
        int i = 0;
        Double rad = 0d;
        int radius = 0;
        int previousWordSize = 0;
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            Log.d(TAG, String.format("buildWordCloud, start build, key=%s, value=%s", entry.getKey(), entry.getValue()));
            int wordSize = determineWordSize(entry.getValue(), mostFrequentWordCount);
            // skip first element, which is the most used word
            if (i>0) {
                rad = radians.get(rand.nextInt(radians.size()));
            }
            // if rotated all round the circle, time increase radius and start a new circle
            // skip first element, which is the most used word
            if (i==1 || i % 5 == 0) {
                // increase radius with wordSize of the biggest word in the inner circle
                radius += previousWordSize;
            }
            previousWordSize = wordSize;
            Rect wordRect = calculateCoordinates(radius, rad, entry.getKey());
            Log.d(TAG, String.format("buildWordCloud, rect word=%s, x=%s, y=%s", entry.getKey(), wordRect.left, wordRect.top));
            Paint.Align align = Paint.Align.LEFT;
            // align most used word with center
            if (i==0) {
                align = Paint.Align.CENTER;
            }
            Paint wordPaint = createPaint(wordSize, align);
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), new Rect());
            Log.d(TAG, String.format("buildWordCloud, rect word=%s, x=%s, y=%s", entry.getKey(), wordRect.left, wordRect.top));
            Word newWord = Word.builder()
                    .setText(entry.getKey())
                    .setSize(wordSize)
                    .setCount(entry.getValue())
                    .setPaint(wordPaint)
                    .setRect(wordRect)
                    .setYOffset(Math.abs(wordRect.top))
                    .build();

            Log.d(TAG, String.format("buildWordCloud, radius=%d, angle=%s, wordSize=%s, degrees=%s, loop=%s, word=%s, x0=%s, y0=%s, w=%s, h=%s", radius, rad, wordSize, radians.size(), i, entry.getKey(), centerX, centerY, width, height));
            Log.d(TAG, String.format("buildWordCloud, rect word=%s, w=%s, h=%s, x=%s, y=%s", newWord.getText(), newWord.getRect().width(), newWord.getRect().height(), newWord.getRect().left, newWord.getRect().top));

           //  Word word = checkWordCollision(newWord, wordList, radius);
           // Log.d(TAG, String.format("is equal: %s", newWord.toString().equals(word.toString())));
           // if (word != null) {
                wordList.add(newWord);
            //} else {
            //    numberOfCollisions++;
            //}
            i++;
        }
        List<Word> sortedWordList = wordList.stream()
                .sorted((w1,w2)-> w2.getCount().compareTo(w1.getCount()))
                .collect(Collectors.toList());

        // for debug only
        sortedWordList.forEach(w -> Log.i(TAG, String.format("buildWordCloud, x=%s, y=%s, size=%s, word=%s, occurrences=%s", w.getRect().left, w.getRect().top, w.getSize(), w.getText(), w.getCount())));
        Log.d(TAG, String.format("build word cloud finished! numberOfWords=%s, numberOfCollisions=%s", wordList.size(), numberOfCollisions));
        return sortedWordList;
    }

    private Word checkWordCollision(Word word, List<Word> wordList, int radius) {
        Random rand = new Random();
        List<Double> radianers = getNumberList(20);
        Rect colRect = checkCollision(word, wordList);
        int newRadius = radius;
        int attempts = 0;
        while (colRect != null && attempts < 5) {
            attempts++;
            // increase radius
            newRadius += colRect.height();
            double newRad = radianers.get(rand.nextInt(radianers.size()));
            Rect newRect = calculateCoordinates(newRadius, newRad, word.getText());
            while (xyCoordinateList.contains(colRect.toShortString())) {
                newRect = calculateCoordinates(newRadius, newRad, word.getText());
                Log.d(TAG, String.format("crash regenerate coordinates...", word.getText()));
            }
            word.getRect().set(newRect);
            // try to rotate
            // w.setRotationDegree(90f);
            colRect = checkCollision(word, wordList);
            Log.d(TAG, String.format("collision attempt: word=%s, attempts=%s", word.toString(), attempts));
        }
        if (colRect == null) {
            Log.d(TAG, String.format("collision fixed, word=%s, attempts=%s", word.toString(), attempts));
            return word;
        }
        Log.d(TAG, String.format("collision, skipped word! word: %s, attempts=%s", word.toString(), attempts));
        return null;
    }

    /**
     *
     * @param newWord
     * @param list
     * @return returns the rectangle the new word collided with
     */
    private Rect checkCollision(Word newWord, List<Word> list) {
        Log.d(TAG, String.format("checkCollision! newWord=%s, numberOfWords=%s", newWord, list.size()));
        for (Word w : list) {
            if (!newWord.getText().equals(w.getText()) && Rect.intersects(newWord.getRect(), w.getRect())) {
                // return coordinates of the word that the new word collision with
                Log.d(TAG, String.format("checkCollision, collision!, newWord=%s, newRect=%s, existingRect=%s, word=%s", newWord.getText(), newWord.getRect().toShortString(), w.getRect().toShortString(), w.getText()));
                return w.getRect();
            } else {
                Log.d(TAG, String.format("checkCollision No collision, newWord=%s, newRect=%s, existingRect=%s, word=%s", newWord.getText(), newWord.getRect().toShortString(), w.getRect().toShortString(), w.getText()));
            }
        }
        return null;
    }

    private Rect calculateCoordinates(int radius, Double theta, String word) {
        int eclipseFactor = 5;
        Double x = centerX + (radius * Math.cos(theta));
        Double y = centerY + (radius * Math.sin(theta));
        Log.d(TAG, String.format("calculated coordinates x,y! x=%s, y=%s, radius=%s, angle=%s, word=%s center=%s,%s", x.intValue(), y.intValue(), radius, theta, word, centerX, centerY));
        // Offset the rectangle to a specific (x, y) position, keeping its width and height the same
        Rect rect = new Rect();
        rect.offsetTo(x.intValue(), y.intValue());
        Log.d(TAG, String.format("calculated coordinates! left=%s, top=%s, right=%s, bottom=%s, radius=%s, angle=%s, word=%s", rect.left, rect.top, rect.right, rect.bottom, radius, theta, word));
        xyCoordinateList.add(rect.toShortString());
        return rect;
    }

    private List<Double> getNumberList(int n) {
        List<Double> randomNumbers = new ArrayList<>();
        double step = 2*Math.PI/n;
        int radius = 50;
        for (double theta = 0; theta < 2*Math.PI; theta += step) {
            randomNumbers.add(theta);
        }
        //Collections.shuffle(randomNumbers);
        Log.d(TAG, String.format("degrees=%s", randomNumbers.size()));
        return randomNumbers;
    }

    /**
     * Determine the font size.
     */
    private int determineWordSize(int wordCount, int highestWordCount) {
        int wordSize = (int) (((float) wordCount / (float) highestWordCount) * MAX_WORD_SIZE);
        Log.d(TAG, String.format("wordSize=%s", wordSize));
        if (wordSize < MIN_WORD_SIZE) {
            wordSize = MIN_WORD_SIZE;
        } else if (wordSize > MAX_WORD_SIZE) {
            wordSize = MAX_WORD_SIZE;
        }
        Log.d(TAG, String.format("wordCount=%s, highestWordCount=%s, wordSize=%s", wordCount, highestWordCount, wordSize));
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

        Log.d(TAG, String.format("createPaint wordSize=%s", wordSize));
        return wordPaint;
    }
}
