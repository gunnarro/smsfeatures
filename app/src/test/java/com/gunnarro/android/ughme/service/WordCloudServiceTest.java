package com.gunnarro.android.ughme.service;

import android.util.Log;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.model.report.AnalyzeReport;
import com.gunnarro.android.ughme.model.sms.Sms;
import com.gunnarro.android.ughme.service.impl.TextAnalyzerServiceImpl;
import com.gunnarro.android.ughme.service.impl.WordCloudServiceImpl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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
        AnalyzeReport report = textAnalyzer.analyzeText(smsPlainTxt.toString(), Sms.INBOX,null, 3, 1);

        WordCloudServiceImpl builder = new WordCloudServiceImpl();
        List<Word> words = builder.buildWordCloud(report.getReportItems(), Dimension.builder().width(1440).height(1944).build(), new Settings());

        Assert.assertEquals(3, words.size());
        // check placed
        Assert.assertEquals(1, (int) words.stream().filter(Word::isPlaced).count());
        // check not placed
        Assert.assertEquals(2, (int) words.stream().filter(Word::isNotPlaced).count());

        // filter out and check placed
        List<Word> placedWords = words.stream().filter(Word::isPlaced).collect(Collectors.toList());
        Assert.assertEquals("[Word(text=dette, rect=null, count=4, size=200.0, rotationAngle=0.0)]", placedWords.toString());
        Assert.assertEquals("Word(text=dette, rect=null, count=4, size=200.0, rotationAngle=0.0)", placedWords.get(0).toString());

        // filter out and check not placed
        List<Word> notPlacedWords = words.stream().filter(Word::isNotPlaced).collect(Collectors.toList());
        Assert.assertEquals("[Word(text=kun, rect=null, count=2, size=100.0, rotationAngle=0.0), Word(text=enhets, rect=null, count=2, size=100.0, rotationAngle=0.0)]", notPlacedWords.toString());

        words.forEach(w -> Log.i("unit-test", String.format("x=%s, y=%s, size=%s, word=%s, occurrences=%s", w.getRect().left, w.getRect().top, w.getSize(), w.getText(), w.getCount())));
        builder.saveWordCloudAsImage();
    }
}
