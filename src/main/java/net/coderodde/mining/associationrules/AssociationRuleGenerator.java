package net.coderodde.mining.associationrules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This class implements the algorithm for mining association rules out of 
 * frequent itemsets.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Apr 10, 2016)
 * @param <I> the actual item type.
 */
public class AssociationRuleGenerator<I> {
    
    public List<AssociationRule<I>> 
        mineAssociationRules(FrequentItemsetData<I> data,
                             double minimumConfidence) {
        Objects.requireNonNull(data, "The frequent itemset data is null.");
        checkMinimumConfidence(minimumConfidence);
        
        Set<AssociationRule<I>> resultSet = new HashSet<>();
        
        for (Set<I> itemset : data.getFrequentItemsetList()) {
            if (itemset.size() < 2) {
                // Any association rule requires at least one item in the 
                // antecedent, and at least one item in the consequent. An
                // itemset containing less than two items cannot satisfy this
                // requirement; skip it.
                continue;
            }
            
            // Generate the basic association rules out of current itemset.
            // An association rule is basic iff its consequent contains only one
            // item.
            Set<AssociationRule<I>> basicAssociationRuleSet = 
                    generateAllBasicAssociationRules(itemset, data);
            
            generateAssociationRules(itemset,
                                     basicAssociationRuleSet,
                                     data,
                                     minimumConfidence,
                                     resultSet);
        }
        
        List<AssociationRule<I>> ret = new ArrayList<>(resultSet);
        
        Collections.sort(ret, (a1, a2) -> { 
                               return Double.compare(a2.getConfidence(),
                                                     a1.getConfidence()); 
        });
        
        return ret;
    }
        
    private void generateAssociationRules(Set<I> itemset,
                                          Set<AssociationRule<I>> ruleSet,
                                          FrequentItemsetData<I> data,
                                          double minimumConfidence,
                                          Set<AssociationRule<I>> collector) {
        if (ruleSet.isEmpty()) {
            System.out.println("Rule set is empty in generateAssociationRules.");
            return;
        }
        
        // The size of the itemset.
        int k = itemset.size(); 
        // The size of the consequent of the input rules.
        int m = ruleSet.iterator().next().getConsequent().size();
        
        // Test whether we can pull one more item from the antecedent to 
        // consequent.
        if (k > m + 1) {
            Set<AssociationRule<I>> nextRules =
                    moveOneItemToConsequents(itemset, ruleSet, data);
            
            Iterator<AssociationRule<I>> iterator = nextRules.iterator();
            
            while (iterator.hasNext()) {
                AssociationRule<I> rule = iterator.next();
                
                if (rule.getConfidence() >= minimumConfidence) {
                    collector.add(rule);
                } else {
                    iterator.remove();
                }
            }
            
            generateAssociationRules(itemset,
                                     nextRules,
                                     data,
                                     minimumConfidence,
                                     collector);
        }
    }
    
    private Set<AssociationRule<I>> 
        moveOneItemToConsequents(Set<I> itemset, 
                                 Set<AssociationRule<I>> ruleSet,
                                 FrequentItemsetData<I> data) {
        Set<AssociationRule<I>> output = new HashSet<>();
        Set<I> antecedent = new HashSet<>();
        Set<I> consequent = new HashSet<>();
        double itemsetSupportCount = data.getSupportCountMap().get(itemset);
        
        // For each rule ...
        for (AssociationRule<I> rule : ruleSet) {
            // ... move one item from its antecedent to its consequnt.
            for (I item : rule.getAntecedent()) {
                antecedent.clear();
                antecedent.addAll(rule.getAntecedent());
                antecedent.remove(item);
                
                consequent.clear();
                consequent.addAll(rule.getConsequent());
                consequent.add(item);
                
                int antecedentSupportCount = data.getSupportCountMap()
                                                 .get(antecedent);
                AssociationRule<I> newRule = 
                        new AssociationRule<>(
                                antecedent,
                                consequent,
                                itemsetSupportCount / antecedentSupportCount);
                
                output.add(newRule);
            }
        }
        
        return output;
    }
        
   /**
    * Given a frequent itemset of size {@code n}, generates and returns all 
    * {@code n} possible association rules with consequent of size one.
    * 
    * @param itemset the itemset.
    * @return a set of association rules with consequents of size one.
    */
    private Set<AssociationRule<I>> 
        generateAllBasicAssociationRules(Set<I> itemset,
                                         FrequentItemsetData<I> data) {
        Set<AssociationRule<I>> basicAssociationRuleSet =
                new HashSet<>(itemset.size());
        
        Set<I> antecedent = new HashSet<>(itemset.size());
        Set<I> consequent = new HashSet<>(1);
        
        for (I item : itemset) {
            antecedent.clear();
            antecedent.addAll(itemset);
            antecedent.remove(item);
            consequent.clear();
            consequent.add(item);
            
            int itemsetSupportCount = data.getSupportCountMap().get(itemset);
            int antecedentSupportCount = data.getSupportCountMap()
                                             .get(antecedent);
            
            double confidence = 1.0 * itemsetSupportCount 
                                    / antecedentSupportCount;
            
            basicAssociationRuleSet.add(new AssociationRule(antecedent, 
                                                            consequent,
                                                            confidence));
        }
        
        return basicAssociationRuleSet;
    }
        
    private void checkMinimumConfidence(double minimumConfidence) {
        if (Double.isNaN(minimumConfidence)) {
            throw new IllegalArgumentException(
                    "The input minimum confidence is NaN.");
        }
        
        if (minimumConfidence < 0.0) {
            throw new IllegalArgumentException(
                    "The input minimum confidence is negative: " + 
                    minimumConfidence + ". " +
                    "Must be at least zero.");
        }
        
        if (minimumConfidence > 1.0) {
            throw new IllegalArgumentException(
                    "The input minimum confidence is too large: " +
                    minimumConfidence + ". " +
                    "Must be at most 1.");
        }
    }
        
//    private List<AssociationRule<I>> 
//        generateAssociationRules(Set<I> itemset,
//                                 List<AssociationRule<I>> rules,
//                                 FrequentItemsetData<I> data,
//                                 double minimumConfidence) {
//        if (rules.isEmpty()) {
//            return new ArrayList<>(0);
//        }
//        
//        Set<AssociationRule<I>> set = new HashSet<>();
//        int itemsetSize = itemset.size();
//        int consequentSize = rules.get(0).getConsequent().size();
//        Set<I> workSet = new HashSet<>();
//        
//        if (itemsetSize > consequentSize + 1) {
//            // There is room for moving one item from the antecedent to 
//            // consequent.
//            List<AssociationRule<I>> nextRuleList = generateNextRules(rules);
//            Iterator<AssociationRule<I>> ruleIterator = nextRuleList.iterator();
//            
//            while (ruleIterator.hasNext()) {
//                AssociationRule<I> rule = ruleIterator.next();
//                
//                workSet.clear();
//                workSet.addAll(itemset);
//                workSet.removeAll(rule.getConsequent());
//                
//                int itemsetSupportCount = data.getSupportCountMap()
//                                              .get(itemset);
//                int antecedentSupportCount = data.getSupportCountMap()
//                                                 .get(workSet);
//                
//                double confidence = 1.0 * itemsetSupportCount 
//                                        / antecedentSupportCount;
//                
//                if (confidence >= minimumConfidence) {
//                    rule.setConfidence(confidence);
//                    set.add(rule);
//                } else {
//                    ruleIterator.remove(); // Get rid of the current rule.
//                }
//            }
//            
//            List<AssociationRule<I>> rulesToAdd = 
//                    generateAssociationRules(itemset, 
//                                             nextRuleList,
//                                             data,
//                                             minimumConfidence);
//            
//            set.addAll(rulesToAdd);
//        }
//        
//        return new ArrayList<>(set);
//    }
//        
//    private List<AssociationRule<I>> 
//    generateNextRules(List<AssociationRule<I>> ruleList) {
//        // We need a set in order to get rid of repetitions.
//        Set<AssociationRule<I>> nextRuleSet = new HashSet<>();
//        
//        for (AssociationRule<I> rule : ruleList) {
//            if (rule.getAntecedent().size() < 2) {
//                // Moving one item from the antecedent to consequent will make 
//                // the former empty. Ignore the rule.
//                continue;
//            }
//            
//            for (I antecedentItem : rule.getAntecedent()) {
//                Set<I> newAntecedent = new HashSet<>(rule.getAntecedent());
//                Set<I> newConsequent = new HashSet<>(rule.getConsequent()
//                                                         .size() + 1);
//                
//                newAntecedent.remove(antecedentItem);
//                newConsequent.addAll(rule.getConsequent());
//                newConsequent.add(antecedentItem);
//                
//                AssociationRule<I> newRule = 
//                        new AssociationRule<>(newAntecedent, newConsequent);
//                
//                nextRuleSet.add(newRule);
//            }
//        }
//        
//        return new ArrayList<>(nextRuleSet);
//    }
}
