package net.coderodde.mining.associationrules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Demo {

    public static void main(String[] args) {
        demo();
    }
    
    private static void demo() {
        AprioriFrequentItemsetGenerator<String> generator =
                new AprioriFrequentItemsetGenerator<>();

        List<Set<String>> itemsetList = new ArrayList<>();

        itemsetList.add(new HashSet<>(Arrays.asList("a", "b")));
        itemsetList.add(new HashSet<>(Arrays.asList("b", "c", "d")));
        itemsetList.add(new HashSet<>(Arrays.asList("a", "c", "d", "e")));
        itemsetList.add(new HashSet<>(Arrays.asList("a", "d", "e")));
        itemsetList.add(new HashSet<>(Arrays.asList("a", "b", "c")));

        itemsetList.add(new HashSet<>(Arrays.asList("a", "b", "c", "d")));
        itemsetList.add(new HashSet<>(Arrays.asList("a")));
        itemsetList.add(new HashSet<>(Arrays.asList("a", "b", "c")));
        itemsetList.add(new HashSet<>(Arrays.asList("a", "b", "d")));
        itemsetList.add(new HashSet<>(Arrays.asList("b", "c", "e")));

        long startTime = System.nanoTime();
        FrequentItemsetData<String> data = generator.generate(itemsetList, 0.1);
        long endTime = System.nanoTime();
        
        int i = 1;

        System.out.println("--- Frequent itemsets ---");
        
        for (Set<String> itemset : data.getFrequentItemsetList()) {
            System.out.printf("%2d: %9s, support: %1.1f\n",
                              i++, 
                              itemset,
                              data.getSupport(itemset));
        }
        
        System.out.printf("Mined frequent itemset in %d milliseconds.\n", 
                          (endTime - startTime) / 1_000_000);
        
        startTime = System.nanoTime();
        List<AssociationRule<String>> associationRuleList = 
                new AssociationRuleGenerator<String>()
                        .mineAssociationRules(data, 0.3);
        endTime = System.nanoTime();
        
        i = 1;
        
        System.out.println();
        System.out.println("--- Association rules ---");
        
        for (AssociationRule<String> rule : associationRuleList) {
            System.out.printf("%2d: %s\n", i++, rule);
        }
        
        System.out.printf("Mined association rules in %d milliseconds.\n",
                          (endTime - startTime) / 1_000_000);
    }
}
