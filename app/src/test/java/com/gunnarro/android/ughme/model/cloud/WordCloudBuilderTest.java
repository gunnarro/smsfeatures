package com.gunnarro.android.ughme.model.cloud;

import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.gunnarro.android.ughme.model.analyze.TextAnalyzer;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.cloud.WordCloudBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RunWith(MockitoJUnitRunner.class)
public class WordCloudBuilderTest {

    String[] mobileNumbers = {"23545400", "23545411", "23545422", "23545433", "23545444", "23545455", "23545466", "23545466", "23545477", "235454588", "23545499"};
    List<String> words;

    @Before
    public void readWordFile() throws IOException {
        Path path = Paths.get("src/test/resources/norwegian-words.txt");
        words = Files.readAllLines(path);
/*
        Path path = Paths.get("src/test/resources/norwegian-verbs.txt");
        words = Files.readAllLines(path);
        for (int i=0; i<words.size();i++) {
            String[] split = words.get(i).trim().split(" ");
            System.out.println(split[0].trim());
            System.out.println(split[1].trim());
            System.out.println(split[2].trim());
            //System.out.println(split[3].trim());
            System.out.println(split[4].trim());
        }
 */
    }

    @Test
    public void buildWordCloud() {
        //doNothing().when(rectMock).offsetTo(Mockito.anyInt(), Mockito.anyInt());
        //doNothing().when(rectMock).offset(Mockito.anyInt(), Mockito.anyInt());

        StringBuilder smsPlainTxt = new StringBuilder();
        // List<Sms> smsList = generateSmsList(10);

        // smsList.forEach(s -> smsPlainTxt.append(s.getBody()));

        smsPlainTxt.append("Dette, dette, dette er kun en enhets test, og dette er ingenting å tulle med, spør du meg. antall enhets tester er kun 1");


        TextAnalyzer textAnalyzer = new TextAnalyzer();
        textAnalyzer.analyzeText(smsPlainTxt.toString(), null);

        WordCloudBuilder builder = new WordCloudBuilder(1440, 1944);
        List<Word> words = builder.buildWordCloud(textAnalyzer.getWordCountMap(3), textAnalyzer.getHighestWordCount());
        Assert.assertEquals("[Word{word=dette, size=200.0, count=4, rect=null width=0 height=0}]", words.toString());
        Assert.assertEquals("Word{word=dette, size=200.0, count=4, rect=null width=0 height=0}", words.get(0).toString());
        words.forEach(w -> Log.i("unit-test", String.format("x=%s, y=%s, size=%s, word=%s, occurrences=%s", w.getRect().left, w.getRect().top, w.getSize(), w.getText(), w.getCount())));
    }

    @Test
    public void intersectes() {
        //  Rect a = new Rect(803,1100,804,1101);
        //   Rect b = new Rect(803,1100,804,1101);
        //   Assert.assertTrue(intersects(a,b));
        int x = 803;
        int y = 803;
        double r = Math.sqrt(x - 3) + Math.sqrt(y - 3);
    }

    @Test
    public void circle() {
        double step = 2 * Math.PI / 20;
        int radius = 50;
        for (double theta = 0; theta < 2 * Math.PI; theta += step) {
            double x = 100 + (radius * Math.cos(theta));
            double y = 100 + (radius * Math.sin(theta));
            Log.d("", String.format("%.2f. (%.0f, %.0f)", theta, x, y));
        }
    }

    @Ignore
    @Test
    public void place() {
        // 1. create mock
        Rect rect = Mockito.mock(Rect.class);
        Mockito.when(rect.width()).thenReturn(75);
        Mockito.when(rect.height()).thenReturn(75);

        WordCloudBuilder builder = new WordCloudBuilder(1440, 1944);
        Word word = Word.builder()
                .setText("test")
                .setRect(rect)
                .setPaint(new Paint())
                .setCount(1)
                .setSize(10)
                .build();


        Assert.assertEquals(75, word.getRect().height());

        //  Assert.assertEquals(1920, word.getPosition().x);
        //  Assert.assertEquals(1920, word.getPosition().y);
        //  Assert.assertNotNull(builder.place(word, new Point(1920, 1920)));
        Assert.assertNotNull(builder.place(word, new Point(1920, 1920)));
    }


    public static boolean intersects(Rect a, Rect b) {
        Log.d("unit-test", a.toShortString());
        boolean v = a.left < b.right
                && b.left < a.right
                && a.top < b.bottom
                && b.top < a.bottom;
        return v;
    }

    private List<Sms> generateSmsList(int numberOfSms) {
        Random rand = new Random();
        List<Sms> list = new ArrayList<>();
        for (int i = 0; i < numberOfSms; i++) {
            list.add(Sms.builder().setAddress(mobileNumbers[rand.nextInt(mobileNumbers.length - 1)])
                    .setBody(generateSentence(rand.nextInt(100)))
                    .build());
        }
        return list;
    }

    /**
     * Setningsledd
     * verbal - verb
     * subjekt - subjektet
     * objekt - direkte og/eller indirekte, for eks: indirekte: gunnar, direkte: en ball
     * adverbial - adverbet
     * predikativ -  beskriver subjektet eller objektet i en setning
     */
    private String generateSentence(int numberOfWords) {
        Random random = new Random();
        StringBuilder sentence = new StringBuilder();
        for (int i = 0; i < numberOfWords; i++) {
            sentence.append(words.get(random.nextInt(words.size() - 1))).append(" ");
        }
        //Log.d("unit-test", sentence.toString());
        return sentence.toString();
    }
}
