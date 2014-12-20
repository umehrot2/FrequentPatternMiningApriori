/* ---------- Apriori Algorithm------------*/
import java.io.*;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.*;
import java.io.InputStreamReader;

public class Apriori {

    public static double min_sup = 0.002; //Minimum Support
    public static int[][] D_matrix = new int[][]{ //D(t,t') Matrix for purity
            {10047, 17326, 17988, 17999, 17820},
            {17326, 9674, 17446, 17902, 17486},
            {17988, 17446, 9959, 18077, 17492},
            {17999, 17902, 18077, 10161, 17912},
            {17820, 17486, 17492, 17912, 9845}
    };


    //This function receives candidate item sets, with their support count and returns frequent item sets
    public static HashMap<ArrayList<Integer>,Integer> get_frequent_item_sets(HashMap<ArrayList<Integer>,Integer> candidates,int k)
    {

      HashMap<ArrayList<Integer>,Integer> frequent = new HashMap<ArrayList<Integer>, Integer>();
      for(Object keys : candidates.keySet())
      {
          ArrayList<Integer> key = (ArrayList<Integer>) keys;
          int support = candidates.get(key);
          if(support >= min_sup) {

              //Add the item-set if the support count >= minimum support
              frequent.put(key,support);

          }
      }

      if(frequent.size() != 0)
            System.out.println("Number of Frequent " + k + " item-sets found : " + frequent.size());

      return frequent;
    }

    //This function generate candidate k - item sets, from frequent (k-1) item sets
    public static HashMap<ArrayList<Integer>,Integer> generate_candidates(HashMap<ArrayList<Integer>,Integer> frequent_items_previous,int size)
    {
        int len = frequent_items_previous.keySet().size();
        HashMap<ArrayList<Integer>,Integer> candidates = new HashMap<ArrayList<Integer>, Integer>();
        for(int i = 0; i < len-1; i++)
        {
            ArrayList<Integer> item_set1 = (ArrayList<Integer>) frequent_items_previous.keySet().toArray()[i];
            for(int j = i+1; j < len; j++)
            {
                ArrayList<Integer> new_candidate = new ArrayList<Integer>();
                ArrayList<Integer> item_set2 = (ArrayList<Integer>) frequent_items_previous.keySet().toArray()[j];
                int flag = 0;

                for(int z = 0; z <= size-2; z++)
                {
                    int f = item_set1.get(z);
                    int g = item_set2.get(z);
                    if(f != g)
                    {
                        flag = 1;
                        break;
                    }
                    else
                    {
                        new_candidate.add(item_set1.get(z));
                    }
                }

                if(flag == 0) {
                    int x = item_set2.get(item_set2.size() - 1);
                    new_candidate.add(item_set1.get(item_set1.size() - 1));
                    new_candidate.add(x);
                    Collections.sort(new_candidate);

                    if(!has_infrequent_subsets(frequent_items_previous,new_candidate))
                        candidates.put(new_candidate, 0);
                }
            }
        }
        System.out.println();
        return candidates;
    }

    //This function obtains the support counts of all the candidate item sets, by scanning file data
    public static HashMap<ArrayList<Integer>,Integer> get_candidates_count(HashMap<ArrayList<Integer>,Integer> candidates, ArrayList<ArrayList<Integer>> file_data)
    {
        for(Object keys : candidates.keySet())
        {
            ArrayList<Integer> candidate = (ArrayList<Integer>) keys;
            for(int i = 0; i <  file_data.size(); i++)
            {
                ArrayList<Integer> temp = file_data.get(i);
                int count = 0;
                for(int x = 0; x < candidate.size(); x++)
                {
                    int f = candidate.get(x);
                    for(int y = 0; y < temp.size(); y++)
                    {
                        int g = temp.get(y);
                        if(f == g) {
                            count++;
                            break;
                        }
                    }
                }

                if(count == candidate.size()) {
                    int c = candidates.get(candidate);
                    candidates.put(candidate,++c);
                }
            }
        }

        return candidates;
    }

    //This function checks if any candidate item set generated, has an infrequent subset, so it can be pruned
    public static boolean has_infrequent_subsets(HashMap<ArrayList<Integer>,Integer> frequent_items_previous, ArrayList<Integer> candidate_set)
    {

        for(int i=0; i < candidate_set.size(); i++)
        {
            ArrayList<Integer> subset = new ArrayList<Integer>();
            for(int j=0; j < candidate_set.size(); j++)
            {
              if(i != j)
              {
                  subset.add(candidate_set.get(j));
              }
            }
            Collections.sort(subset);

            if(!frequent_items_previous.containsKey(subset))
                return true;
        }
        return false;
    }

    //This function reads the data from files topic-i.txt and returns the data
    public static ArrayList<ArrayList<Integer>> get_file_data(String file_path)
    {
        String file = file_path;
        ArrayList<ArrayList<Integer>> data_from_file = new ArrayList<ArrayList<Integer>>();
        try {
            //Read the file line by line and populate the array
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                List<String> l = Arrays.asList(line.split(" "));
                ArrayList<Integer> t_list = new ArrayList<Integer>();
                for (int i = 0; i < l.size(); i++) {
                   t_list.add(Integer.parseInt(l.get(i)));
                }
                data_from_file.add(t_list);
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
        return data_from_file;
    }

    //This function reads the data from vocabulary file vocab.txt
    public static HashMap<Integer,String> read_vocabulary()
    {
        String file = "vocab.txt";
        HashMap<Integer,String> vocab = new HashMap<Integer, String>();
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null)
            {
              String[] temp = line.split("\t");
              vocab.put(Integer.parseInt(temp[0]),temp[1]);
            }
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }

        return vocab;
    }

    //This function runs the actual Apriori algorithm and generate frequent patterns
    public static HashMap<ArrayList<Integer>, Integer> apriori(HashMap<ArrayList<Integer>, Integer> frequent_item_sets, ArrayList<ArrayList<Integer>> file_data)
    {
        HashMap<ArrayList<Integer>, Integer> all_frequent_item_sets = frequent_item_sets;
        HashMap<ArrayList<Integer>, Integer> candidates = new HashMap<ArrayList<Integer>, Integer>();
        for (int k = 2; frequent_item_sets.size() != 0; k++) {
            candidates = generate_candidates(frequent_item_sets, k - 1);
            candidates = get_candidates_count(candidates, file_data);
            frequent_item_sets = get_frequent_item_sets(candidates, k);
            if (frequent_item_sets.size() != 0) {
                for (ArrayList<Integer> key : frequent_item_sets.keySet()) {
                    all_frequent_item_sets.put(key, frequent_item_sets.get(key));
                }
            }
        }

        return all_frequent_item_sets;
    }

    //This function maps the frequent patterns containing numbers to corresponding words from the vocabulary
    public static HashMap<String, Integer> map_numbers_to_words(HashMap<ArrayList<Integer>, Integer> all_frequent_item_sets, HashMap<Integer, String> vocab)
    {
        HashMap<String, Integer> frequent_word_patterns = new HashMap<String, Integer>();
        for (ArrayList<Integer> key : all_frequent_item_sets.keySet()) {
            String words = "";
            for (Integer num : vocab.keySet()) {
                if (key.contains(num)) {
                    words = words + " " + vocab.get(num);
                }
            }
            frequent_word_patterns.put(words, all_frequent_item_sets.get(key));
        }

        return frequent_word_patterns;
    }

    //This function sorts the frequent word patterns in descending order of support count
    public static List<Entry<String, Integer>> sort_by_support_count(HashMap<String, Integer> frequent_word_patterns)
    {
        Map<String, Integer> map = frequent_word_patterns;
        Set<Entry<String, Integer>> set = map.entrySet();
        List<Entry<String, Integer>> sorted_frequent_patterns = new ArrayList<Entry<String, Integer>>(set);
        Collections.sort(sorted_frequent_patterns, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        return sorted_frequent_patterns;
    }

    //This function writes the frequent patterns to file pattern-i.txt
    public static void print_frequent_patterns(List<Entry<String, Integer>> sorted_frequent_patterns, int file_id)
    {
        //Write output in file pattern-i.txt
        try {
            File pattern_dir = new File("pattern");
            if (!pattern_dir.exists()) {
                try {
                    pattern_dir.mkdir();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }

            PrintWriter writer = new PrintWriter(pattern_dir + "/pattern-" + file_id + ".txt", "UTF-8");
            for (int i = 0; i < sorted_frequent_patterns.size(); i++) {
                writer.println(sorted_frequent_patterns.get(i).getValue() + " "  + sorted_frequent_patterns.get(i).getKey().trim());
            }

            writer.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }

    //This function generate max frequent patterns and writes them to max-i.txt file
    public static void find_max_patterns(List<Entry<String, Integer>> sorted_frequent_patterns, int file_id)
    {
        try {
            //Mining max patterns and writing them in file max-i.txt
            File max_pattern_dir = new File("max");
            if (!max_pattern_dir.exists()) {
                try {
                    max_pattern_dir.mkdir();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            PrintWriter writer = new PrintWriter(max_pattern_dir + "/max-" + file_id + ".txt", "UTF-8");
            for (int i = 0; i < sorted_frequent_patterns.size(); i++) {
                List<String> pattern_to_check = Arrays.asList(sorted_frequent_patterns.get(i).getKey().split(" "));
                boolean is_max_pattern = true;
                for (int j = 0; j < sorted_frequent_patterns.size(); j++) {
                    if (i != j) {
                        List<String> pattern_against = Arrays.asList(sorted_frequent_patterns.get(j).getKey().split(" "));
                        if (pattern_against.size() > pattern_to_check.size()) {
                            if (pattern_against.containsAll(pattern_to_check)) {
                                is_max_pattern = false;
                                break;
                            }
                        }
                    }
                }

                if (is_max_pattern)
                    writer.println(sorted_frequent_patterns.get(i).getValue() + " "  + sorted_frequent_patterns.get(i).getKey().trim());
            }
            writer.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }

    //This function generates closed frequent patterns and writes them to file
    public static void find_closed_patterns(List<Entry<String, Integer>> sorted_frequent_patterns, int file_id)
    {
        //Mining closed patterns and writing them in file closed-i.txt
        try {
            File closed_pattern_dir = new File("closed");
            if (!closed_pattern_dir.exists()) {
                try {
                    closed_pattern_dir.mkdir();
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            PrintWriter writer = new PrintWriter(closed_pattern_dir + "/closed-" + file_id + ".txt", "UTF-8");
            for (int i = 0; i < sorted_frequent_patterns.size(); i++) {
                List<String> pattern_to_check = Arrays.asList(sorted_frequent_patterns.get(i).getKey().split(" "));
                int value_to_check = sorted_frequent_patterns.get(i).getValue();
                boolean is_closed_pattern = true;
                for (int j = 0; j < sorted_frequent_patterns.size(); j++) {
                    if (i != j) {
                        List<String> pattern_against = Arrays.asList(sorted_frequent_patterns.get(j).getKey().split(" "));
                        if (pattern_against.size() > pattern_to_check.size()) {
                            int value_against = sorted_frequent_patterns.get(j).getValue();
                            if (pattern_against.containsAll(pattern_to_check) && value_to_check == value_against) {
                                is_closed_pattern = false;
                                break;
                            }
                        }
                    }
                }

                if (is_closed_pattern)
                    writer.println(sorted_frequent_patterns.get(i).getValue() + " "  + sorted_frequent_patterns.get(i).getKey().trim());
            }
            writer.close();
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }

    }

    //This function generates purity for frequent patterns, and writes them to purity-i.txt
    public static void find_pure_patterns(ArrayList<HashMap<ArrayList<Integer>, Integer>> frequent_patterns_across_files, ArrayList<ArrayList<ArrayList<Integer>>> data_across_files)
    {
        HashMap<ArrayList<Integer>, ArrayList<Integer>> patterns_purity = new HashMap<ArrayList<Integer>, ArrayList<Integer>>();
        HashMap<Integer, String> vocab = read_vocabulary();

        //Compute the purity for frequent patterns from all files
        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            for(ArrayList<Integer> pattern : frequent_patterns_across_files.get(i).keySet())
            {
                if(!patterns_purity.containsKey(pattern)) {
                    ArrayList<Integer> counts = new ArrayList<Integer>();
                    for (int k = 0; k < data_across_files.size(); k++) {
                        if (i != k) {
                            /*
                            int count = 0;
                            for (int l = 0; l < data_across_files.get(k).size(); l++) {
                                ArrayList<Integer> seq = data_across_files.get(k).get(l);
                                if (seq.containsAll(pattern)) {
                                    count++;
                                }
                            }
                            counts.add(count);*/
                            if(frequent_patterns_across_files.get(k).containsKey(pattern))
                            {
                                counts.add(frequent_patterns_across_files.get(k).get(pattern));
                            }
                            else
                                counts.add(0);
                        }
                        else
                            counts.add(frequent_patterns_across_files.get(i).get(pattern));
                    }
                    patterns_purity.put(pattern,counts);
                }
            }
        }

        ArrayList<HashMap<String,Double>> pure_patterns = new ArrayList<HashMap<String, Double>>();
        ArrayList<HashMap<String,Double>> rank_patterns = new ArrayList<HashMap<String, Double>>();
        final ArrayList<HashMap<String,Integer>> frequent_patterns = new ArrayList<HashMap<String, Integer>>();
        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            HashMap<ArrayList<Integer>,Double> file_pure_patterns = new HashMap<ArrayList<Integer>, Double>();
            //new
            HashMap<ArrayList<Integer>,Double> patterns_ranks = new HashMap<ArrayList<Integer>, Double>();
            for(ArrayList<Integer> pattern : frequent_patterns_across_files.get(i).keySet())
            {
                ArrayList<Integer> fp_values = patterns_purity.get(pattern);
                Double max_fpval = 0.0;
                for(int j = 0; j < fp_values.size(); j++)
                {
                    if(i != j)
                    {
                        double val = fp_values.get(i) + fp_values.get(j);
                        val = val / D_matrix[i][j];
                        if(max_fpval < val)
                            max_fpval = val;
                    }
                }

                double purity = Math.log(fp_values.get(i)/(D_matrix[i][i] * 1.0)) - Math.log(max_fpval);
                DecimalFormat twoDForm = new DecimalFormat("#.####");
                purity = Double.valueOf(twoDForm.format(purity));
                double support = frequent_patterns_across_files.get(i).get(pattern) / (D_matrix[i][i] * 1.0);
                double rank = purity * support;
                file_pure_patterns.put(pattern,purity);
                patterns_ranks.put(pattern,rank);
            }

            HashMap<String, Double> frequent_word_patterns = new HashMap<String, Double>();
            HashMap<String, Integer> frequent_word_patterns_2 = new HashMap<String, Integer>();
            HashMap<String, Double> word_patterns_by_rank = new HashMap<String, Double>();

            for (ArrayList<Integer> key : file_pure_patterns.keySet()) {
                String words = "";
                for (Integer num : vocab.keySet()) {
                    if (key.contains(num)) {
                        words = words + " " + vocab.get(num);
                    }
                }
                frequent_word_patterns.put(words, file_pure_patterns.get(key));
                frequent_word_patterns_2.put(words, frequent_patterns_across_files.get(i).get(key));
                word_patterns_by_rank.put(words,patterns_ranks.get(key));
            }

            pure_patterns.add(frequent_word_patterns);
            frequent_patterns.add(frequent_word_patterns_2);
            rank_patterns.add(word_patterns_by_rank);
        }

        //Re rank patterns in descending order of Purity followed by Support count, and write them to file
        File pure_pattern_dir = new File("purity");
        if(!pure_pattern_dir.exists())
        {
            try
            {
                pure_pattern_dir.mkdir();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }

        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            try {
                PrintWriter writer = new PrintWriter(pure_pattern_dir + "/purity-" + i + ".txt", "UTF-8");
                /*
                Map<String, Double> map = pure_patterns.get(i);
                Set<Entry<String, Double>> set = map.entrySet();
                List<Entry<String, Double>> sorted_frequent_patterns = new ArrayList<Entry<String, Double>>(set);
                final int f = i;
                Collections.sort(sorted_frequent_patterns, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        if(o2.getValue().compareTo(o1.getValue()) == 0)
                        {
                            Integer sup1 = frequent_patterns.get(f).get(o2.getKey());
                            Integer sup2 = frequent_patterns.get(f).get(o1.getKey());
                            return sup1.compareTo(sup2);
                        }
                        else
                            return (o2.getValue()).compareTo(o1.getValue());
                    }
                });*/
                Map<String, Double> map = rank_patterns.get(i);
                Set<Entry<String, Double>> set = map.entrySet();
                List<Entry<String, Double>> sorted_frequent_patterns = new ArrayList<Entry<String, Double>>(set);
                Collections.sort(sorted_frequent_patterns, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                            return (o2.getValue()).compareTo(o1.getValue());
                    }
                });

                for (int j = 0; j < sorted_frequent_patterns.size(); j++) {

                    writer.println(pure_patterns.get(i).get(sorted_frequent_patterns.get(j).getKey()) + " "  + sorted_frequent_patterns.get(j).getKey().trim());
                }

                writer.close();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }

    }

    //This function generates Coverage for frequent patterns, and re ranks them by Coverage value
    public static void find_coverage_patterns(ArrayList<HashMap<ArrayList<Integer>, Integer>> frequent_patterns_across_files)
    {
        HashMap<Integer, String> vocab = read_vocabulary();

        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            //Generate coverage for patterns
            HashMap<ArrayList<Integer>, Double> patterns_coverage = new HashMap<ArrayList<Integer>, Double>();
            for(ArrayList<Integer> key : frequent_patterns_across_files.get(i).keySet())
            {
                double completeness = frequent_patterns_across_files.get(i).get(key) / (D_matrix[i][i] * 1.0);
                DecimalFormat twoDForm = new DecimalFormat("#.####");
                completeness = Double.valueOf(twoDForm.format(completeness));
                patterns_coverage.put(key, completeness);
            }

            //Map numbers to words from vocab
            HashMap<String,Double> words_patterns_coverage = new HashMap<String, Double>();
            for(ArrayList<Integer> key : patterns_coverage.keySet())
            {
                String words = "";
                for(Integer num : vocab.keySet())
                {
                    if(key.contains(num))
                    {
                        words = words + " " + vocab.get(num);
                    }
                }
                words_patterns_coverage.put(words,patterns_coverage.get(key));
            }

            //Sort the patterns in descending order of coverage value and write them to file
            File coverage_pattern_dir = new File("coverage");
            if(!coverage_pattern_dir.exists())
            {
                try
                {
                    coverage_pattern_dir.mkdir();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }

            try {
                PrintWriter writer = new PrintWriter(coverage_pattern_dir  + "/coverage-" + i + ".txt", "UTF-8");

                Map<String, Double> map = words_patterns_coverage;
                Set<Entry<String, Double>> set = map.entrySet();
                List<Entry<String, Double>> patterns_sorted_by_coverage = new ArrayList<Entry<String, Double>>(set);
                final int f = i;
                Collections.sort(patterns_sorted_by_coverage, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                            return (o2.getValue()).compareTo(o1.getValue());
                    }
                });


                for (int j = 0; j < patterns_sorted_by_coverage.size(); j++) {

                    writer.println(patterns_sorted_by_coverage.get(j).getValue() + " "  + patterns_sorted_by_coverage.get(j).getKey().trim());
                }

                writer.close();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }

        }

    }

    //This function generates Phraseness for frequent patterns, and re ranks them by Phraseness value
    public static void find_phraseness_patterns(ArrayList<HashMap<ArrayList<Integer>, Integer>> frequent_patterns_across_files)
    {
        HashMap<Integer, String> vocab = read_vocabulary();

        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            //Generate Phraseness for patterns
            HashMap<ArrayList<Integer>, Double> patterns_phraseness = new HashMap<ArrayList<Integer>, Double>();
            for(ArrayList<Integer> key : frequent_patterns_across_files.get(i).keySet())
            {
                double phraseness = Math.log(frequent_patterns_across_files.get(i).get(key) / (D_matrix[i][i] * 1.0));

                double sum = 0.0;
                if(key.size() > 1) {
                    for (Integer num : key) {
                        ArrayList<Integer> num1 = new ArrayList<Integer>();
                        num1.add(num);
                        sum = sum + Math.log(frequent_patterns_across_files.get(i).get(num1) / (D_matrix[i][i] * 1.0));
                    }
                }
                phraseness = phraseness - sum;
                DecimalFormat twoDForm = new DecimalFormat("#.####");
                phraseness = Double.valueOf(twoDForm.format(phraseness));
                patterns_phraseness.put(key, phraseness);
            }

            //Map numbers to words from vocab
            HashMap<String,Double> words_patterns_phraseness = new HashMap<String, Double>();
            for(ArrayList<Integer> key : patterns_phraseness.keySet())
            {
                String words = "";
                for(Integer num : vocab.keySet())
                {
                    if(key.contains(num))
                    {
                        words = words + " " + vocab.get(num);
                    }
                }
                words_patterns_phraseness.put(words,patterns_phraseness.get(key));
            }

            //Sort the patterns in descending order of Phraseness value, and write them to file
            File phraseness_pattern_dir = new File("phraseness");
            if(!phraseness_pattern_dir.exists())
            {
                try
                {
                    phraseness_pattern_dir.mkdir();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }

            try {
                PrintWriter writer = new PrintWriter(phraseness_pattern_dir  + "/phraseness-" + i + ".txt", "UTF-8");

                Map<String, Double> map = words_patterns_phraseness;
                Set<Entry<String, Double>> set = map.entrySet();
                List<Entry<String, Double>> patterns_sorted_by_phraseness = new ArrayList<Entry<String, Double>>(set);
                final int f = i;
                Collections.sort(patterns_sorted_by_phraseness, new Comparator<Map.Entry<String, Double>>() {
                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });


                for (int j = 0; j < patterns_sorted_by_phraseness.size(); j++) {

                    writer.println(patterns_sorted_by_phraseness.get(j).getValue() + " "  + patterns_sorted_by_phraseness.get(j).getKey().trim());
                }

                writer.close();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }

        }
    }

    //This function generates Completeness for frequent patterns, and re ranks them by Completeness and Support Count value
    public static void find_completeness_patterns(ArrayList<HashMap<ArrayList<Integer>, Integer>> frequent_patterns_across_files)
    {
        HashMap<Integer, String> vocab = read_vocabulary();

        for(int i = 0; i < frequent_patterns_across_files.size(); i++)
        {
            HashMap<ArrayList<Integer>, Double> patterns_completeness = new HashMap<ArrayList<Integer>, Double>();

            //Generate Phraseness value for patterns
            for(ArrayList<Integer> key : frequent_patterns_across_files.get(i).keySet())
            {
                int max = 0;
                for(ArrayList<Integer> key2 : frequent_patterns_across_files.get(i).keySet())
                {
                    ArrayList<Integer> temp = new ArrayList<Integer>();
                    temp.addAll(key);
                    if(key2.size() == 1 && !key.contains(key2))
                    {
                        temp.addAll(key2);
                        Collections.sort(temp);
                        int support = 0;
                        if(frequent_patterns_across_files.get(i).containsKey(temp))
                          support = frequent_patterns_across_files.get(i).get(temp);

                        if(max < support)
                            max = support;
                    }
                }


                double val = max / (frequent_patterns_across_files.get(i).get(key) * 1.0);
                double completeness = 1.0 - val;
                DecimalFormat twoDForm = new DecimalFormat("#.####");
                completeness = Double.valueOf(twoDForm.format(completeness));
                patterns_completeness.put(key, completeness);
            }

            //Map numbers to words from vocab
            HashMap<String,Double> words_patterns_completeness = new HashMap<String, Double>();
            final HashMap<String,Integer> frequent_patterns = new HashMap<String, Integer>();
            for(ArrayList<Integer> key : patterns_completeness.keySet())
            {
                String words = "";
                for(Integer num : vocab.keySet())
                {
                    if(key.contains(num))
                    {
                        words = words + " " + vocab.get(num);
                    }
                }
                words_patterns_completeness.put(words,patterns_completeness.get(key));
                frequent_patterns.put(words,frequent_patterns_across_files.get(i).get(key));
            }

            //Re rank patterns by Completeness followed by Minimum support in descending order and write to file
            File completeness_pattern_dir = new File("completeness");
            if(!completeness_pattern_dir.exists())
            {
                try
                {
                    completeness_pattern_dir.mkdir();
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
            }

            try {
                PrintWriter writer = new PrintWriter(completeness_pattern_dir  + "/completeness-" + i + ".txt", "UTF-8");

                Map<String, Double> map = words_patterns_completeness;
                Set<Entry<String, Double>> set = map.entrySet();
                List<Entry<String, Double>> patterns_sorted_by_completeness = new ArrayList<Entry<String, Double>>(set);
                final int f = i;
                Collections.sort(patterns_sorted_by_completeness, new Comparator<Map.Entry<String, Double>>() {

                    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                        if(o2.getValue().compareTo(o1.getValue()) == 0)
                        {
                            Integer sup1 = frequent_patterns.get(o2.getKey());
                            Integer sup2 = frequent_patterns.get(o1.getKey());
                            return sup1.compareTo(sup2);
                        }
                        else
                            return (o2.getValue()).compareTo(o1.getValue());

                    }
                });


                for (int j = 0; j < patterns_sorted_by_completeness.size(); j++) {

                    writer.println(patterns_sorted_by_completeness.get(j).getValue() + " "  + patterns_sorted_by_completeness.get(j).getKey().trim());
                }

                writer.close();
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }

        }
    }

    public static void main(String[] args)
    {
        ArrayList<HashMap<ArrayList<Integer>, Integer>> frequent_patterns_across_files = new ArrayList<HashMap<ArrayList<Integer>, Integer>>();
        ArrayList<ArrayList<ArrayList<Integer>>> data_across_files = new ArrayList<ArrayList<ArrayList<Integer>>>();
        long t1 = System.currentTimeMillis();

        //Fetch the vocab.txt data into a HashMap data structure
        HashMap<Integer, String> vocab = read_vocabulary();
        
        //Check if minimum support has been provided through command line
        if(args.length != 0)
            min_sup = Double.parseDouble(args[0]);

        System.out.println("--------------Running Apriori Algorithm---------------\n");
        System.out.println("Minimum Support : " + min_sup + "\n");
        System.out.println("------------------------------------------------------");

        for(int loop = 0; loop <= 4; loop++) {

           System.out.println("Mining Frequent Patterns for : topic-" + loop + ".txt\n");

           String file = "topic-" + loop + ".txt";
            try {

                //Read the file topic-i.txt line by line and get all candidate 1 - item sets
                BufferedReader br = new BufferedReader(new FileReader(file));

                HashMap<ArrayList<Integer>, Integer> c1 = new HashMap<ArrayList<Integer>, Integer>();
                String line;
                int num_lines = 0;
                while ((line = br.readLine()) != null) {
                    List<String> l = Arrays.asList(line.split(" "));
                    for (int i = 0; i < l.size(); i++) {
                        ArrayList<Integer> key = new ArrayList<Integer>();
                        key.add(Integer.parseInt(l.get(i)));

                        if (!c1.containsKey(key)) {
                            c1.put(key, 1);
                        } else {
                            int a = c1.get(key);
                            c1.put(key, ++a);
                        }
                    }
                    num_lines++;
                }

                //Obtain minimum support in terms of number of records
                min_sup = min_sup * num_lines;

                //Fetch the data from topic-i.txt file into memory
                ArrayList<ArrayList<Integer>> topic_file_data = get_file_data(file);

                //Store the data from all the files, together in one data structure
                data_across_files.add(topic_file_data);

                //Get frequent 1 - item sets
                HashMap<ArrayList<Integer>, Integer> l1 = get_frequent_item_sets(c1, 1);

                //Call the Apriori algorithm with frequent 1 - item sets, and the data read from file topic-i.txt
                HashMap<ArrayList<Integer>, Integer> all_frequent_item_sets = apriori(l1,topic_file_data);

                //Store the frequent patterns from all files in one data structure
                frequent_patterns_across_files.add(all_frequent_item_sets);

                //Map the numbers in frequent patterns to words from the vocabulary
                HashMap<String, Integer> frequent_word_patterns = map_numbers_to_words(all_frequent_item_sets, vocab);

                //Sort the frequent item sets in descending order of their support count
                List<Entry<String, Integer>> sorted_frequent_patterns = sort_by_support_count(frequent_word_patterns);

                //Print all the frequent patterns obtained from topic-i.txt
                print_frequent_patterns(sorted_frequent_patterns,loop);

                //Generate Max frequent patterns from file topic-i.txt and write it to file
                find_max_patterns(sorted_frequent_patterns,loop);

                //Generate Closed frequent patterns from file topic-i.txt and write it to file
                find_closed_patterns(sorted_frequent_patterns,loop);

                //Obtain back the minimum support in fraction, to be used for next run
                min_sup = min_sup / num_lines;

                br.close();
            } catch (Exception ex) {
                System.out.println(ex);
            }
            System.out.println("------------------------------------------------------");
        }

        System.out.println("\nRe Ranking frequent patterns by Purity...");
        //Rank frequent patterns by Purity for all the files topic-i.txt and write them to file
        find_pure_patterns(frequent_patterns_across_files, data_across_files);

        System.out.println("Re Ranking frequent patterns by Coverage...");
        //Rank frequent patterns by Coverage for all the files topic-i.txt and write them to file
        find_coverage_patterns(frequent_patterns_across_files);

        System.out.println("Re Ranking frequent patterns by Phraseness...");
        //Rank frequent patterns by Phraseness for all the files topic-i.txt and write them to file
        find_phraseness_patterns(frequent_patterns_across_files);

        System.out.println("Re Ranking frequent patterns by Completeness...");
        //Rank frequent patterns by Completeness for all the files topic-i.txt and write them to file
        find_completeness_patterns(frequent_patterns_across_files);

        System.out.println("Time Taken : " + (System.currentTimeMillis() - t1));

    }
}
