package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 *
 */
public class IBMModel2 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  private static final int MAX_ITERS = 30;
  private static final double CONVERGE_THRESH = .000001;

  // The CounterMaps that stores the expected alignment counts in terms of indices and length.  Reset to 0 upon each iteration.
  private CounterMap<String, String> cQMap;
  // The CounterMaps that stores the expected alignment counts in terms of French word and English word.  
  // French word is used as the key, English word as value. Reset to 0 upon each iteration.
  private CounterMap<String, String> cTMap;
  // The CounterMap that stores the q parameters. String representations are used as keys and values.
  private CounterMap<String, String> qMap;
  private CounterMap<String, String> tMap;

  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> sourceWords = sentencePair.getSourceWords();
    List<String> targetWords = sentencePair.getTargetWords();
    if (!sourceWords.contains("NULL")) { 
      sourceWords.add("NULL");
    }
    int numSourceWords = sourceWords.size();
    int numTargetWords = targetWords.size();
    for (int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      double maxProb = Double.NEGATIVE_INFINITY;
      int argMaxSrcIndex = 0;
      for (int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        double prob = qMap.getCount(tgtIndex + "," + numTargetWords + "," + numSourceWords, Integer.toString(srcIndex)) * tMap.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex)); 
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
    tMap = bootstrap.getTMap();
    boolean converged = false;
    int iteration = 0; 
    while (!converged && iteration < MAX_ITERS) {
      cQMap = new CounterMap<String, String>();
      cTMap = new CounterMap<String, String>();
      expectation(trainingPairs, iteration);
      converged =  maximization(iteration);
      iteration++; 
    }
    System.out.println("Number of iterations until converged: " + iteration);
  }
  
  private void expectation(List<SentencePair> trainingPairs, int iteration) {
    if (iteration == 0) {
      initializeQParamsRandomly(trainingPairs);
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

  private boolean maximization(int iteration) {
    if (iteration == 0) {
      qMap = Counters.conditionalNormalize(cQMap);
      return false;
    } else {
      CounterMap<String, String> newTMap = Counters.conditionalNormalize(cTMap);
      CounterMap<String, String> newQMap = Counters.conditionalNormalize(cQMap);
      boolean converged = checkConvergence(newTMap, newQMap); 
      tMap = newTMap;
      qMap = newQMap;
      return converged; 
    }
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

  private void initializeQParamsRandomly(List<SentencePair> trainingPairs) {
    for(SentencePair pair : trainingPairs){
      List<String> sourceWords = pair.getSourceWords();
      List<String> targetWords = pair.getTargetWords();
      if (!sourceWords.contains("NULL")) { 
        sourceWords.add("NULL");
      }
      int numSourceWords = sourceWords.size();
      int numTargetWords = targetWords.size();
      for(int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
        for(int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
          cQMap.setCount(tgtIndex + "," + numTargetWords + "," + numSourceWords, Integer.toString(srcIndex), Math.random());
        }
      }
    }
  }

  private void addDelta(List<String> sourceWords, List<String> targetWords, int iteration){
    int numTargetWords = targetWords.size();
    int numSourceWords = sourceWords.size();
    for(int tgtIndex = 0; tgtIndex < numTargetWords; tgtIndex++) {
      double deltaDenom = calculateDeltaDenom(targetWords.get(tgtIndex), tgtIndex, numTargetWords, sourceWords);
      for(int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) {
        double indivProb = qMap.getCount(tgtIndex + "," + numTargetWords + "," + numSourceWords, Integer.toString(srcIndex)) * tMap.getCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex));
        double alignmentProb = indivProb/deltaDenom; 
        cTMap.incrementCount(sourceWords.get(srcIndex), targetWords.get(tgtIndex), alignmentProb);
        cQMap.incrementCount(tgtIndex + "," + numTargetWords + "," + numSourceWords, Integer.toString(srcIndex), alignmentProb);
      }
    } 
  }

  private double calculateDeltaDenom(String targetWord, int tgtIndex, int numTargetWords, List<String> sourceWords) {
    int numSourceWords = sourceWords.size();
    double denom = 0.0;
    for(int srcIndex = 0; srcIndex < numSourceWords; srcIndex++) { 
      denom += qMap.getCount(tgtIndex + "," + numTargetWords + "," + numSourceWords, Integer.toString(srcIndex)) * tMap.getCount(sourceWords.get(srcIndex), targetWord); 
    }
    return denom;
  }
}
