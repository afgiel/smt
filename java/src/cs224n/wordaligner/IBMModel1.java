package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 *
 */
public class IBMModel1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  private static final int MAX_ITERS = 30;
  private static final double CONVERGE_THRESH = .001;

  private CounterMap<String, String> cMap;
  private CounterMap<String, String> tMap;

  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    if (!sourceWords.contains("NULL")) { 
      sourceWords.add("NULL");
    }
    
    for (int tgtIndex = 0; tgtIndex < targetWords.size(); tgtIndex++) {
      double maxProb = Double.NEGATIVE_INFINITY;
      int argMaxSrcIndex = 0;
      for (int srcIndex = 0; srcIndex < sourceWords.size(); srcIndex++) {
        double prob = tMap.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)); 
        if (prob > maxProb) {
          maxProb = prob;
          argMaxSrcIndex = srcIndex;
        }
      }
      alignment.addPredictedAlignment(tgtIndex, argMaxSrcIndex);
    } 
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    cMap = new CounterMap<String, String>();
    tMap = new CounterMap<String, String>();
    boolean converged = false;
    int iteration = 1; 
    while (!converged && iteration < MAX_ITERS) {
      cMap = new CounterMap<String, String>();
      expectation(trainingPairs, iteration);
      converged =  maximization();
      iteration++; 
    }
    
  }
 
  public CounterMap<String, String> getTMap() { 
    return tMap;
  }

  private void expectation(List<SentencePair> trainingPairs, int iteration) {
    for(SentencePair pair : trainingPairs){
      List<String> targetWords = pair.getTargetWords();
      List<String> sourceWords = pair.getSourceWords();
      if (!sourceWords.contains("NULL")) { 
        sourceWords.add("NULL");
      }
      addDelta(sourceWords, targetWords, iteration);
    }
  } 

  private boolean maximization() {
    CounterMap<String, String> newTMap = Counters.conditionalNormalize(cMap);
    boolean converged = checkConvergence(newTMap); 
    tMap = newTMap;
    return converged; 
  }

  private boolean checkConvergence(CounterMap<String, String> newTMap) { 
    int totalNumEntries = 0;
    double totalDifference = 0.0;
    for(String sourceKey : tMap.keySet()) {
      for(String targetKey : tMap.getCounter(sourceKey).keySet()) {
        double oldProb = tMap.getCount(sourceKey, targetKey);
        double newProb = newTMap.getCount(sourceKey, targetKey);
        totalDifference += Math.abs(oldProb - newProb);
        totalNumEntries++;
      }
    }
    double averageDiff = totalDifference/totalNumEntries;
    return averageDiff < CONVERGE_THRESH;
  }

  private void addDelta(List<String> sourceWords, List<String> targetWords, int iteration){
    for(String targetWord : targetWords) {     
      double deltaDenom = calculateDeltaDenom(targetWord, sourceWords);
      for(String sourceWord : sourceWords) {
        if(iteration == 1) {
          double alignmentProb = 1.0/sourceWords.size();
          cMap.incrementCount(sourceWord, targetWord, alignmentProb); 
        } else {
          double indivProb = tMap.getCount(sourceWord, targetWord);
          double alignmentProb = indivProb/deltaDenom; 
          cMap.incrementCount(sourceWord, targetWord, alignmentProb);
        }     
      }
    } 
  }

  private double calculateDeltaDenom(String targetWord, List<String> sourceWords) {
    double denom = 0.0;
    for(String sourceWord : sourceWords) { 
      denom += tMap.getCount(sourceWord, targetWord); 
    }
    return denom;
  }
}
