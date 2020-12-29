package com.gunnarro.android.ughme.model.analyze;

import com.gunnarro.android.ughme.model.analyze.TextAnalyzer;

import org.junit.Assert;
import org.junit.Test;

public class TextAnalyzerTest {

    @Test
    public void analyzeText() {
        TextAnalyzer w = new TextAnalyzer();
        w.analyzeText("Dette, dette, dette er kun en enhets test, og dette er ingen ting å tule med, spør du meg. antall enhets tester er kun 1", null);
        Assert.assertEquals(12, w.getNumberOfWords().intValue());
        Assert.assertEquals("{dette=4, enhets=2, ingen=1, antall=1, test=1, ting=1, tester=1, tule=1}", w.getWordCountMap(10).toString());
        Assert.assertEquals("{dette=4, enhets=2, ingen=1}", w.getWordCountMap(3).toString());
        Assert.assertEquals(4, w.getHighestWordCount());
        Assert.assertEquals(0.48f, w.getHighestWordCountPercent(), 0);
    }

    /*
    @Test
    public void spiral() {
       // int n = 5; // number of spiral turns;
        int radiusInitial = 0; // spiral initial radius
        int radiusFinal = 100; // sprial final radius
        double thetaStart = 0; // spiral start angle
        double theatFinal = 2*Math.PI*n;// the final angle
        double b = 0;// spiral growth rate
        double xspiral;
        double yspiral;
        for (int n = 0; n<6; n++) {
            b = (radiusFinal - radiusInitial)/2*Math.PI*n;
            xspiral = (radius + b * theta) * Math.cos(theta);
            yspiral = (radius + b * theta) * Math.sin(theta);
            Log.d("unit-test", String.format("x=%s, y=%s", xspiral,yspiral));
        }
    }

     */
}

