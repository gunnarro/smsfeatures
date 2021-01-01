package com.gunnarro.android.ughme.service;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.service.impl.WordCloudServiceImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class WordCloudServiceTest {

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


        TextAnalyzerServiceImpl textAnalyzer = new TextAnalyzerServiceImpl();
        textAnalyzer.analyzeText(smsPlainTxt.toString(), null);

        WordCloudService builder = new WordCloudServiceImpl(1440, 1944);
        List<Word> words = builder.buildWordCloud(textAnalyzer.getWordCountMap(3), textAnalyzer.getHighestWordCount());
        Assert.assertEquals("[Word{word=dette, size=200.0, count=4, rect=null width=0 height=0}]", words.toString());
        Assert.assertEquals("Word{word=dette, size=200.0, count=4, rect=null width=0 height=0}", words.get(0).toString());
        words.forEach(w -> Log.i("unit-test", String.format("x=%s, y=%s, size=%s, word=%s, occurrences=%s", w.getRect().left, w.getRect().top, w.getSize(), w.getText(), w.getCount())));
    }

    @Ignore
    @Test
    public void place() {
        // 1. create mock
        Rect rect = Mockito.mock(Rect.class);
        Mockito.when(rect.width()).thenReturn(75);
        Mockito.when(rect.height()).thenReturn(75);

        WordCloudService builder = new WordCloudServiceImpl(1440, 1944);
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
        // Assert.assertNotNull(builder.place(word, new Point(1920, 1920)));
    }
}
