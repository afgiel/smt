package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;
import java.util.HashSet;

/**
 *
 */
public class IBMModel1 implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
 
  private CounterMap<String, String> countMap;
  private CounterMap<String, String> tMap;

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
    countMap = new CounterMap<String, String>();
    tMap = new CounterMap<String, String>();
    boolean converged = false;
    boolean first = true; 
    while (!converged) {
      expectation(trainingPairs, first);
      maximization();
      first = first && false; 
    }
    
  }
  private void expectation(List<SentencePair> trainingPairs, boolean first) {
    for(SentencePair pair : trainingPairs){
         List<String> targetWords = pair.getTargetWords();
         List<String> sourceWords = pair.getSourceWords();
         if (!sourceWords.contains("NULL")) { 
            sourceWords.add("NULL");
         }
         countWords("source", sourceWords);
         countJointOccurrences(sourceWords, targetWords);
         computeProbs(first);
    }   

  } 

  private void maximization() {
  
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

  private void computeProbs(boolean first){ 
    
  } 
}
