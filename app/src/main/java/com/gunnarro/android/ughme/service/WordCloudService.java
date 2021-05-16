package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.cloud.Dimension;
import com.gunnarro.android.ughme.model.cloud.Word;
import com.gunnarro.android.ughme.model.config.Settings;
import com.gunnarro.android.ughme.model.report.ReportItem;

import java.util.List;

public interface WordCloudService {

    List<Word> buildWordCloud(List<ReportItem> wordItems, Dimension rectangleDimension, Settings settings);

}
