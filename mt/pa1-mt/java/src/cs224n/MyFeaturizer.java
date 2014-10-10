package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;
import java.lang.Math;

import edu.stanford.nlp.mt.util.FeatureValue;
import edu.stanford.nlp.mt.util.Featurizable;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.mt.util.Sequence;

/**
 * A rule featurizer.
 */
public class MyFeaturizer implements RuleFeaturizer<IString, String> {
 
  public static final String FEAT_NAME = "worddiff";
  

  @Override
  public void initialize() {
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {
      
    List<FeatureValue<String>> features = Generics.newLinkedList();
    features.add(new FeatureValue<String>(String.format("%s:%d", FEAT_NAME, f.sourcePhrase.size() - f.targetPhrase.size()), 1.0));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
