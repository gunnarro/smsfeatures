package com.gunnarro.android.ughme.model.config;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "settings_table")
public class Settings {

    /**
     * Auto generated id
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Integer id;

    @ColumnInfo(name = "word_match_regex")
    public String wordMatchRegex = "\\b\\w{3,}";
    /**
     * Maximum number of words displayed in the word cloud
     */
    @ColumnInfo(name = "number_of_words")
    public Integer numberOfWords = 100;
    /**
     * Spiral radius step size, used by the wold cloud algorithm
     */
    @ColumnInfo(name = "radius_step")
    public Integer radiusStep = 30;
    /**
     * Offset step size used by the word cloud algorithm
     */
    @ColumnInfo(name = "offset_step")
    public Integer offsetStep = 25;
    /**
     * Maximum number of chars in a word
     */
    @ColumnInfo(name = "word_max_chars")
    public Integer maxCharsInWord = 50;
    /**
     * Minimum number of chars in a word
     */
    @ColumnInfo(name = "word_min_chars")
    public Integer minCharsInWord = 3;

    @ColumnInfo(name = "word_min_font_size")
    public Integer minWordFontSize = 25;

    @ColumnInfo(name = "word_max_font_size")
    public Integer maxWordFontSize = 200;

    /**
     * Numbers of mobile numbers in the word cloud view drop down list.
     */
    @ColumnInfo(name = "mobile_max_numbers")
    public Integer numberOfMobileNumbers = 10;
    /**
     * Numbers of bas in the bar chart
     */
    @ColumnInfo(name = "chart_max_bars")
    public Integer numberOfBarsInChart = 12;


    @NotNull
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Settings{");
        sb.append("id=").append(id);
        sb.append(", wordMatchRegex='").append(wordMatchRegex).append('\'');
        sb.append(", numberOfWords=").append(numberOfWords);
        sb.append(", radiusStep=").append(radiusStep);
        sb.append(", offsetStep=").append(offsetStep);
        sb.append(", maxCharsInWord=").append(maxCharsInWord);
        sb.append(", minCharsInWord=").append(minCharsInWord);
        sb.append(", minWordFontSize=").append(minWordFontSize);
        sb.append(", maxWordFontSize=").append(maxWordFontSize);
        sb.append(", numberOfMobileNumbers=").append(numberOfMobileNumbers);
        sb.append(", numberOfBarsInChart=").append(numberOfBarsInChart);
        sb.append('}');
        return sb.toString();
    }
}
