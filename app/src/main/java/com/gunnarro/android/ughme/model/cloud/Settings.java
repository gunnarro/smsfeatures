package com.gunnarro.android.ughme.model.cloud;

public class Settings {
    /**
     * Maximum number of words displayed in the word cloud
     */
    private Integer numberOfWords = 100;
    /**
     * Spiral radius step size, used by the wold cloud algorithm
     */
    private Integer radiusStep = 20;
    /**
     * Offset step size used by the word cloud algorithm
     */
    private Integer offsetStep = 25;
    /**
     * Maximum number of chars in a word
     */
    private Integer maxCharsInWord = 50;
    /**
     * Minimum number of chars in a word
     */
    private Integer minCharsInWord = 3;
    /**
     * Numbers of mobile numbers in the word cloud view drop down list.
     */
    private Integer numberOfMobileNumbers = 10;
    /**
     * Numbers of bas in the bar chart
     */
    private Integer numberOfBarsInChart = 12;


    public Settings() {
        super();
    }

    public Integer getNumberOfWords() {
        return numberOfWords;
    }

    public Integer getRadiusStep() {
        return radiusStep;
    }

    public Integer getOffsetStep() {
        return offsetStep;
    }

    public Integer getMaxCharsInWord() {
        return maxCharsInWord;
    }

    public Integer getMinCharsInWord() {
        return minCharsInWord;
    }

    public Integer getNumberOfMobileNumbers() {
        return numberOfMobileNumbers;
    }

    public Integer getNumberOfBarsInChart() {
        return numberOfBarsInChart;
    }

    public void setNumberOfWords(Integer numberOfWords) {

        this.numberOfWords = numberOfWords;
    }

    public void setRadiusStep(Integer radiusStep) {
        this.radiusStep = radiusStep;
    }

    public void setOffsetStep(Integer offsetStep) {
        this.offsetStep = offsetStep;
    }

    public void setMaxCharsInWord(Integer maxCharsInWord) {
        this.maxCharsInWord = maxCharsInWord;
    }

    public void setMinCharsInWord(Integer minCharsInWord) {
        this.minCharsInWord = minCharsInWord;
    }

    public void setNumberOfMobileNumbers(Integer numberOfMobileNumbers) {
        this.numberOfMobileNumbers = numberOfMobileNumbers;
    }

    public void setNumberOfBarsInChart(Integer numberOfBarsInChart) {
        this.numberOfBarsInChart = numberOfBarsInChart;
    }

}
