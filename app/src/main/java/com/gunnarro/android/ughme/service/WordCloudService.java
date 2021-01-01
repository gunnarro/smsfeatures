package com.gunnarro.android.ughme.service;

import com.gunnarro.android.ughme.model.cloud.Word;

import java.util.List;
import java.util.Map;

public interface WordCloudService {

    List<Word> buildWordCloud(Map<String, Integer> wordMap, Integer mostFrequentWordCount);

}
