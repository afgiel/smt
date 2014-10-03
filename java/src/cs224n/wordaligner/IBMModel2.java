package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 *
 */
public class IBMModel2 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  private static final int MAX_ITERS = 10000;
  private static final double CONVERGE_THRESH = .1;

  private CounterMap<String, String> cMap;
  private CounterMap<String, String> qMap;
  private CounterMap<String, String> tMap;
  private int numUniqueSourceWords;

  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    int m = sourceWords.size();
    int n = targetWords.size();
    if (!sourceWords.contains("NULL")) { 
      sourceWords.add("NULL");
    }
    for (int tgtIndex = 0; tgtIndex < targetWords.size(); tgtIndex++) {
      double maxProb = Double.NEGATIVE_INFINITY;
      int argMaxSrcIndex = 0;
      for (int srcIndex = 0; srcIndex < sourceWords.size(); srcIndex++) {
        double prob = qMap.getCount(tgtIndex + "," + n + "," + m, Integer.toString(srcIndex)) * tMap.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)); 
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
    IBMModel1 bootstrap = new IBMModel1();
    bootstrap.train(trainingPairs);
    cMap = new CounterMap<String, String>();
    qMap = new CounterMap<String, String>();
    tMap = bootstrap.getTMap();
    boolean converged = false;
    int iteration = 0; 
    while (!converged && iteration < MAX_ITERS) {
      cMap = new CounterMap<String, String>();
      expectation(trainingPairs, iteration);
      converged =  maximization();
      iteration++; 
    }
  }
  
  private void expectation(List<SentencePair> trainingPairs, int iteration) {
    if (iteration == 0) {
      // Initialize q params randomly
      for(SentencePair pair : trainingPairs){
        List<String> sourceWords = pair.getSourceWords();
        if (!sourceWords.contains("NULL")) { 
          sourceWords.add("NULL");
        }
        List<String> targetWords = pair.getTargetWords();
        int m = sourceWords.size();
        int n = targetWords.size();
        for(int i = 0; i < n; i++) {
          for(int j = 0; j < m; j++) {
            qMap.setCount(i + "," + n + "," + m, Integer.toString(j), Math.random());
          }
        }
      }
    } else {
      for(SentencePair pair : trainingPairs){
        List<String> targetWords = pair.getTargetWords();
        List<String> sourceWords = pair.getSourceWords();
        if (!sourceWords.contains("NULL")) { 
          sourceWords.add("NULL");
        }
        addDelta(sourceWords, targetWords, iteration);
      }
    }
  } 

  private boolean maximization() {
    CounterMap<String, String> newTMap = Counters.conditionalNormalize(cMap);
    CounterMap<String, String> newQMap = Counters.conditionalNormalize(qMap);
    boolean converged = checkConvergence(newTMap, newQMap); 
    tMap = newTMap;
    qMap = newQMap;
    return converged; 
  }

  private boolean checkConvergence(CounterMap<String, String> newTMap, CounterMap<String, String> newQMap) { 
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
    int n = targetWords.size();
    int m = sourceWords.size();
    for(int i = 0; i < n; i++) {
      double deltaDenom = calculateDeltaDenom(targetWords.get(i), i, n, sourceWords);
      for(int j = 0; j < m; j++) {
        double indivProb = qMap.getCount(i + "," + n + "," + m, Integer.toString(j)) * tMap.getCount(sourceWords.get(j), targetWords.get(i));
        double alignmentProb = indivProb/deltaDenom; 
        cMap.incrementCount(sourceWords.get(j), targetWords.get(i), alignmentProb);
      }
    } 
  }

  private double calculateDeltaDenom(String targetWord, int i, int n, List<String> sourceWords) {
    int m = sourceWords.size();
    double denom = 0.0;
    for(int j = 0; j < m; j++) { 
      denom += qMap.getCount(i + "," + n + "," + m, Integer.toString(j)) * tMap.getCount(sourceWords.get(j), targetWord); 
    }
    return denom;
  }
}
