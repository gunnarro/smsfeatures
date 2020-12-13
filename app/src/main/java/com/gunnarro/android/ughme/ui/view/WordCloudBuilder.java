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
    private Dimension rectangleDimension;


    protected TreeWordPlacer wordPlacer;

    private enum WordAlignEnum {
        BELOW, RIGHT, ABOVE, LEFT;
    }

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
        rectangleDimension = new Dimension(width, height);
        this.centerX = this.width / 2;
        this.centerY = this.height / 2;
        wordPlacer = new TreeWordPlacer();
        wordPlacer.reset();
        Log.i(buildTag("WordCloudBuilder"), String.format("init, width=%s, height=%s", this.width, this.height));
    }

    public List<Word> testAlgorithm(int numberOfWords) {
        List<Word> wordList = new ArrayList<>();
        Double radStep = 2 * Math.PI / 60;
        int radiusStep = 100;
        int radius = radiusStep;
        for (int i=0; i<numberOfWords; i++) {
            if (i % 30 == 0) {
                radius += radiusStep;
            }
            //Point startPoint = calculateXYCoordinatesCircle(radius, i*radStep, Integer.toString(i));
            Point startPoint = calculateXYCoordinatesSpiral(radius, i*radStep, Integer.toString(i));
            Rect wordRect = new Rect();
            //wordRect.left = this.width/2;
            //wordRect.top = this.height/2;
            wordRect.offsetTo(startPoint.x, startPoint.y);
            Word newWord = Word.builder()
                    .setText(Integer.toString(i))
                    .setRect(wordRect)
                    .setPaint(new Paint())
                    .setCount(numberOfWords)
                    .setSize(numberOfWords)
                    .build();
            wordList.add(newWord);
        }
        return wordList;
    }

    public List<Word> testAlgorithmRectangle(int numberOfWords) {
        List<Word> wordList = new ArrayList<>();
        Word previousWord = createFistWord("first-word");
        wordList.add(previousWord);
        for (int i=0; i<numberOfWords; i++) {
            previousWord = wordList.get(i/4);
            String word = "test-word-number-" + Integer.toString(i);
            Rect wordRect = new Rect();
            WordAlignEnum align = WordAlignEnum.values()[i%4];
            Paint wordPaint = createPaint(getRandomNumberInRange(MIN_WORD_SIZE, MAX_WORD_SIZE-10), Paint.Align.CENTER);
            Point offsetPoint = calculateOffset(previousWord.getRect(), align, word);
            wordRect.set(previousWord.getRect().left + offsetPoint.x, previousWord.getRect().top + offsetPoint.y, previousWord.getRect().right + offsetPoint.x, previousWord.getRect().bottom + offsetPoint.y);
            int rotationAngle = 0;
            switch (align) {
                case LEFT:
                    wordPaint.setTextAlign(Paint.Align.CENTER);
                    rotationAngle = 270;
                    break;
                case RIGHT:
                    wordPaint.setTextAlign(Paint.Align.CENTER);
                    rotationAngle = 270;
                    break;
                default:
                    // do nothing, keep default text alignment
            }

            Word newWord = Word.builder()
                    .setText(word)
                    .setRect(wordRect)
                    .setPaint(wordPaint)
                    .setCount(numberOfWords)
                    .setSize(numberOfWords)
                    .build();

            newWord.setRotationAngle(rotationAngle);
            wordList.add(newWord);
        }
        return wordList;
    }

    /**
     * First word is always placed at center of the screen.
     *
     * @param word
     * @return
     */
    private Word createFistWord(String word) {
        Rect wordRect = new Rect();
        Paint wordPaint = createPaint(MAX_WORD_SIZE, Paint.Align.CENTER);
        wordPaint.getTextBounds(word, 0, word.length(), wordRect);
        wordRect.offsetTo(centerX, centerY);
        return Word.builder()
                .setText(word)
                .setRect(wordRect)
                .setPaint(wordPaint)
                .setCount(0)
                .setSize(0)
                .build();
    }

    public List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount) {
        Log.d(buildTag("buildWordCloud"), String.format("word map size=%s", wordMap.size()));
        Log.d(buildTag("buildWordCloud"), String.format("word map: %s", wordMap));
        AngleGenerator angleGenerator = new AngleGenerator();
        int numberOfCollisions = 0;
        int numberOfdWords = 1;
        List<Word> wordList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            int wordSize = determineWordSize(entry.getValue(), mostFrequentWordCount);
            Paint wordPaint = createPaint(wordSize, Paint.Align.CENTER);
            Rect wordRect = new Rect();
            wordPaint.getTextBounds(entry.getKey(), 0, entry.getKey().length(), wordRect);
            Word newWord = Word.builder()
                    .setText(entry.getKey())
                    .setSize(wordSize)
                    .setCount(entry.getValue())
                    .setPaint(wordPaint)
                    .setRect(wordRect)
                    .build();

            final Point startPoint = getStartingPoint(rectangleDimension, newWord);
            boolean placed = place(newWord, startPoint);

            if (placed) {
                wordList.add(newWord);
                //Log.d(buildTag("buildWordCloud"), String.format("placed, word: %s, rect=%s,%s, placed-words=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfdWords));
            } else {
                numberOfCollisions++;
                //Log.d(buildTag("buildWordCloud"), String.format("skipped, word: %s, rect=%s,%s collisions=%s", newWord.getText(), newWord.getRect().left, newWord.getRect().top, numberOfCollisions));
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


        public List<Word> buildWordCloudOld(Map<String, Integer> wordMap, Integer mostFrequentWordCount) {
        Log.d(buildTag("buildWordCloud"), String.format("word map size=%s", wordMap.size()));
        Log.d(buildTag("buildWordCloud"), String.format("word map: %s", wordMap));
        AngleGenerator angleGenerator = new AngleGenerator();
        int numberOfCollisions = 0;
        List<Word> wordList = new ArrayList<>();
        Random rand = new Random();
        int i = 0;
        int radius = 0;
        Word previousWord = null;
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            int wordSize = determineWordSize(entry.getValue(), mostFrequentWordCount);
            // if rotated all round the circle, time increase radius and start a new circle
            // skip first element, which is the most used word and should always be at center
            if (previousWord != null || i / 25 > 1) {
                // increase radius with wordSize of the biggest word in the inner circle
                radius += 15;
                Log.d(buildTag("buildWordCloud"), String.format("new radius=%s, loop=%s", radius, i));
            }
            Log.d(buildTag("buildWordCloud"), String.format("start build, i=%s, key=%s, value=%s, wordSize=%s, radius=%s", i, entry.getKey(), entry.getValue(), wordSize, radius));
            Paint.Align align = Paint.Align.CENTER;
            // align most used word with center
            //if (i == 0) {
            //    align = Paint.Align.CENTER;
            //}
            Point startPoint = calculateXYCoordinatesCircle(radius, angleGenerator.randomNext(), entry.getKey());
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

//            Log.d(buildTag("buildWordCloud"), String.format("rect radius=%d, wordSize=%s, degrees=%s, loop=%s, word=%s, x0=%s, y0=%s, w=%s, h=%s", radius, wordSize, i, entry.getKey(), centerX, centerY, width, height));
            Log.d(buildTag("buildWordCloud"), String.format("rect word=%s, w=%s, h=%s, coordinates=%s", newWord.getText(), newWord.getRect().width(), newWord.getRect().height(), newWord.getRect().toShortString()));

            // turn on/off collision check
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

    private Word checkWordCollision(Word newWord, final List<Word> wordList, int radius) {
        Log.d(buildTag(""), String.format("start check, word=%s", newWord.getText()));
        Random rand = new Random();
        Rect colRect = checkCollision(newWord, wordList);
        int newRadius = radius;
        WordAlignEnum align = null;
        int attempts = 0;
        while (colRect != null && attempts < 6) {
            Log.d(buildTag("checkWordCollision"), String.format("rect=%s, colRect=%s", newWord.getRect().toShortString(), colRect.toShortString()));
            // increase radius
            //if (attempts % 3 == 0) {
            //    newRadius += colRect.height();
            //}
            //double newRad = radianers.get(rand.nextInt(radianers.size()));
            //Point newPoint = calculateXYCoordinates(newRadius, newRad, word.getText());
            align = WordAlignEnum.values()[getRandomNumberInRange(0,3)];
            Point offsetPoint = calculateOffset(newWord.getRect(), align, newWord.getText() );
            //word.getRect().offset(offsetPoint.x, offsetPoint.y);
            newWord.getRect().set(colRect.left + offsetPoint.x, colRect.top + offsetPoint.y, colRect.right + offsetPoint.x, colRect.bottom + offsetPoint.y);
            int i = 1;
            while (!isInsideCanvasBounds(newWord.getRect(), newWord.getText())) {
                // revert previous offset
                newWord.getRect().offset(-offsetPoint.x, -offsetPoint.y);
                align = WordAlignEnum.values()[getRandomNumberInRange(0,3)];
                offsetPoint = calculateOffset(colRect, align, newWord.getText());
                newWord.getRect().offset(offsetPoint.x, offsetPoint.y);
                Log.d(buildTag("checkWordCollision"), String.format("exceeded canvas, try rotate one more time! i=%s, word=%s, coordinates=%s",i, newWord.getText(), newWord.getRect().toShortString()));
                i++;
                if (i > 3) {
                   // word.setRotationDegree(90 * getRandomNumberInRange(-1,1));
                    break;
                }
            }
            switch (align) {
                case LEFT:
                    newWord.getPaint().setTextAlign(Paint.Align.LEFT);
                    break;
                case RIGHT:
                    newWord.getPaint().setTextAlign(Paint.Align.RIGHT);
                    break;
                default:
                    // do nothing, keep default text alignment
            }
            attempts++;
            colRect = checkCollision(newWord, wordList);
            Log.d(buildTag("checkWordCollision"), String.format("collision attempt: word=%s, attempts=%s", newWord.toString(), attempts));
        }

        if (colRect == null) {
            Log.d(buildTag("checkWordCollision"), String.format("collision fixed, word=%s, attempts=%s", newWord.toString(), attempts));
            return newWord;
        }
        Log.d(buildTag("checkWordCollision"), String.format("collision skipped word! word: %s, attempts=%s", newWord.toString(), attempts));
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

    private Point calculateXYCoordinatesCircle(int radius, Double theta, String word) {
        int eclipseFactor = 5;
        // convert from polar to cartesian coordinates when calculating
        double x = centerX + (radius * Math.cos(theta));
        double y = centerY + (radius * Math.sin(theta));
        Rect rect = new Rect((int) x, (int) y, 0, 0);
        Log.d(buildTag("calculateXYCoordinatesCircle"), String.format("coordinates, word=%s, coordinates=%s, radius=%s, theta=%s", word, rect.toShortString(), radius, theta));
        return new Point((int) x, (int) y);
    }

    private Point calculateXYCoordinatesSpiral(int radius, Double theta, String word) {
        // for spiral
        int rinitial = 0; // spiral initial radius
        int rfinal = 100; // sprial final radius
        int theta0 = 0; // spiral start angle
        int n = 100; // number of turns;
        double theatf = 2*Math.PI*n;
        int gap = 20;
        // sprial growth rate
        double b = (rfinal - rinitial)/2*Math.PI*n;

        double x =  centerX + theta*Math.cos(theta)*gap;
        double y = centerY + theta*Math.sin(theta)*gap;

        Rect rect = new Rect((int) x, (int) y, 0, 0);
        Log.d(buildTag("calculateXYCoordinatesSpiral"), String.format("coordinates, word=%s, coordinates=%s, radius=%s, theta=%s", word, rect.toShortString(), radius, theta));
        return new Point((int) x, (int) y);
    }

    /**
     * Function to determine where to move a rectangle after a collision.
     *
     * We use following options:
     *
     * below: y - height + random x between x.left and x.right
     * left: x + 10 + random y between y.top and y.bottom
     * above: y + 10 + random x between x.left and x.right
     * right: x - width + random y between y.top and y.bottom
     *
     * Where we use the coordinates of the rectangle that the new rectangle collided with in order to determine a new location.
     *
     * The direction, below, left, above and right is randomly picked in order to get a uniform distribution.
     *
     * @param rect - the rect we have collided with
     * @param align - where to align the new word related to the wod we collided with
     * @param word - name of the new word, only for debug purpose
     * @return
     */
    private Point calculateOffset(Rect rect, WordAlignEnum align, String word) {
        if (rect == null) {
            return new Point(0, 0);
        }
        int offsetLeft = 0;
        int offsetTop = 0;
        switch (align) {
            case BELOW:
                offsetLeft = getRandomNumberInRange(0, 3);
                offsetTop = rect.height();
                break;
            case RIGHT:
                offsetLeft = rect.width();// + rect.right + 10;
                offsetTop = getRandomNumberInRange(0, 3);
                break;
            case ABOVE:
                offsetLeft = getRandomNumberInRange(0, 3);
                offsetTop = -rect.height(); //rect.top - rect.height() -10;
                break;
            case LEFT:
                offsetLeft = -rect.width(); //rect.left - rect.width() + 10;
                offsetTop = getRandomNumberInRange(0, 3);
                break;
        }
        Log.d(buildTag("calculateOffset"), String.format("generated coordinates, word=%s, moveTo=%s, from (%s) to (%s, %s)", word, align.name(), rect.toShortString(), offsetLeft, offsetTop));
        return new Point( offsetLeft, offsetTop);
    }

    Random r = new Random();
    private int getRandomNumberInRange(int min, int max) {
        Log.d(buildTag("getRandomNumberInRange"), String.format("min=%s, max=%s", min, max));
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
    public boolean isInsideCanvasBounds(Rect rect, String word) {
        // check for negative values
        if (rect.left < 0 || rect.top < 0 || rect.right < 0 || rect.bottom < 0) {
            return false;
        }
        // check is inside canvas bounds
        if (rect.left > this.width || rect.right > this.width ) {
            Log.d(buildTag("isInsideCanvasBounds"), String.format("width exceeded canvas bounds! word=%s, rect=%s, canvas w=%s, h=%s", word, rect.toShortString(), this.width, this.height));
            return false;
        }

        if ( rect.top > this.height || rect.bottom > this.height ) {
            Log.d(buildTag("isInsideCanvasBounds"), String.format("height exceeded canvas bounds! word=%s, rect=%s, canvas w=%s, h=%s", word, rect.toShortString(), this.width, this.height));
            return false;
        }
        Log.d(buildTag("isInsideCanvasBounds"), String.format("OK inside! word=%s, rect=%s, canvas w=%s, h=%s", word, rect.toShortString(), this.width, this.height));
        return true;

        /*
        // are we inside the background?
        if (position.y < 0 || position.y + dimensionOfWord.height > dimension.height) {
            return false;
        } else if (position.x < 0 || position.x + dimensionOfWord.width > dimension.width) {
            return false;
        }

         */
    }

    // *********************************************************************************************
    // NEW
    //**********************************************************************************************

    /**
     * try to place in center, build out in a spiral trying to place words for N steps
     * @param word the word being placed
     * @param startPoint the place to start trying to place the word
     */
    public boolean place(Word word, final Point startPoint) {
        final int maxRadius = computeRadius(this.rectangleDimension, startPoint);
        final Point position = new Point((int)word.getX(), (int)word.getY());
        // reset position
        position.x = 0;
        position.y = 0;
        Log.d("place-start", String.format("word=%s, position=%s, max-radius: %s, rect=%s,%s", word.getText(), position, maxRadius, rectangleDimension.getWidth(), rectangleDimension.getHeight()));
        for (int r = 0; r < maxRadius; r += 20) {
            for (int x = Math.max(-startPoint.x, -r); x <= Math.min(r, this.rectangleDimension.getWidth() - startPoint.x - 1); x+=25) {
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

    private boolean canPlace(final String word, final Rect wordRect) {
        // are we inside the background?
        /*
        if (word.getPosition().y < 0 || word.getPosition().y + word.getRect().height() > rectangleDimension.getHeight()) {
            return false;
        } else if (word.getPosition().x < 0 || word.getPosition().x + word.getRect().width() > rectangleDimension.getWidth()) {
            return false;
        }
         */
        return wordPlacer.place(word, wordRect); // is there a collision with the existing words?
    }

    /**
     * compute the maximum radius for the placing spiral
     *
     * @param rectangle the size of the backgound
     * @param start the center of the spiral
     * @return the maximum usefull radius
     */
    static int computeRadius(final Dimension rectangle, final Point start) {
        final int maxDistanceX = Math.max(start.x, rectangle.getWidth() - start.x) + 1;
        final int maxDistanceY = Math.max(start.y, rectangle.getHeight() - start.y) + 1;
        // we use the pythagorean theorem to determinate the maximum radius
        return (int) Math.ceil(Math.sqrt(maxDistanceX * maxDistanceX + maxDistanceY * maxDistanceY));
    }

    public Point getStartingPoint(final Dimension dimension, final Word word) {
        final int x = (rectangleDimension.getWidth() / 2) - (word.getRect().width() / 2);
        final int y = (rectangleDimension.getHeight() / 2) - (word.getRect().height() / 2);
        return new Point(x, y);
    }
}
