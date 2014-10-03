package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 *
 */
public class IBMModel1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  private static final int MAX_ITERS = 10000;

  private CounterMap<String, String> cMap;
  private CounterMap<String, String> tMap;
  private int numUniqueSourceWords;

  public Alignment align(SentencePair sentencePair) {
    Alignment alignment = new Alignment();
    List<String> targetWords = sentencePair.getTargetWords();
    List<String> sourceWords = sentencePair.getSourceWords();
    if (!sourceWords.contains("NULL")) { 
      sourceWords.add("NULL");
    }
     
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    cMap = new CounterMap<String, String>();
    tMap = new CounterMap<String, String>();
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
      HashSet<String> uniqueSourceWords = new HashSet<String>();
      for(SentencePair pair : trainingPairs){
        List<String> sourceWords = pair.getSourceWords();
        for(String sourceWord : sourceWords) {
          uniqueSourceWords.add(sourceWord);  
        }
      }
      numUniqueSourceWords = 1 + uniqueSourceWords.size();
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
    boolean converged = false; 
    // check convergence 
    tMap = newTMap;
    return converged; 
  }

  private void addDelta(List<String> sourceWords, List<String> targetWords, int iteration){
    for(String targetWord : targetWords) {     
      double deltaDenom = calculateDeltaDenom(targetWord, sourceWords);
      for(String sourceWord : sourceWords) {
        if(iteration == 1) {
          double uniformProb = 1.0/numUniqueSourceWords;    
          double alignmentProb = uniformProb*sourceWords.size(); 
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
