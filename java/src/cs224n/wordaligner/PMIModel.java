package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 * Implements the pointwise mutual exclusion model.
 */
public class PMIModel implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
 
  private int totalNumTrainingExamples;
  private int totalNumSourceWords;
  private int totalNumTargetWords;
  private CounterMap<String, String> countMap;

  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    if (!sourceWords.contains("NULL")) { 
      sourceWords.add("NULL");
    }
    int numTargetWords = sentencePair.targetWords.size();
    int numSourceWords = sentencePair.sourceWords.size();
    for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      double maxLogPMI = Double.NEGATIVE_INFINITY;
      int argMaxSrcIndex = 0;
      for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        double logPMI = calculateLogPMI(sourceWords.get(srcIndex), targetWords.get(tgtIndex));
        if (logPMI > maxLogPMI) {
          maxLogPMI = logPMI;
          argMaxSrcIndex = srcIndex;
        }
      }
      alignment.addPredictedAlignment(tgtIndex, argMaxSrcIndex);
    }
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    totalNumTrainingExamples = trainingPairs.size();
    totalNumSourceWords = 0;
    totalNumTargetWords = 0;
    countMap = new CounterMap<String, String>();

    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      if (!sourceWords.contains("NULL")) { 
        sourceWords.add("NULL");
      }
      countWords("source", sourceWords);
      totalNumSourceWords += sourceWords.size();
      countWords("target", targetWords);
      totalNumTargetWords += targetWords.size();
      countJointOccurrences(sourceWords, targetWords);
    }
  }

  private void countWords(String key, List<String> words) {
    for(String word : words){
      countMap.incrementCount(key, word, 1.0);
    }
  }

  private void countJointOccurrences(List<String> sourceWords, List<String> targetWords) {
    HashSet<String> sourceWordsNoDup = new HashSet<String>(sourceWords);
    HashSet<String> targetWordsNoDup = new HashSet<String>(targetWords);
    for(String source : sourceWordsNoDup) {
      for(String target : targetWordsNoDup){
        countMap.incrementCount("joint", source + "," + target, 1.0);
      }
    }
  }

  private double calculateLogPMI(String source, String target) {
    double logJointProb = Math.log10(countMap.getCount("joint", source + "," + target)/totalNumTrainingExamples);
    double logSourceWordProb = Math.log10(countMap.getCount("source", source)/totalNumSourceWords);
    double logTargetWordProb = Math.log10(countMap.getCount("target", target)/totalNumTargetWords);
    double logPMI =  logJointProb - logSourceWordProb - logTargetWordProb;
    return logPMI;
  }
}
