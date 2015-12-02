import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class TrustpilotChallenge {
	
	public final static int CHAR_a = 97;
	public final static int NUM_Chars = 26;
	

	public static void main(String[] args) {
		TreeSet<String> wordList = getWordList("wordlist");
		String anagram = "poultry outwits ants";
		String targetMD5 = "4624d200580677270a54ccff86b9610e";
		
		anagram = removeSpace(anagram);
		wordList = filterWordList(wordList, anagram);
		
		// descending order
		TreeSet<String> set1 = new TreeSet<String>(new Comparator<String>() {
			public int compare(String s1, String s2) {
				if (s1.length() < s2.length()) {
					return 1;
				} else if (s1.length() > s2.length()) {
					return -1;
				} else {
					return s1.compareTo(s2);
				}
			}
		});
		// ascending order
		TreeSet<String> set2 = new TreeSet<String>(new Comparator<String>() {

			public int compare(String s1, String s2) {
				if (s1.length() < s2.length()) {
					return -1;
				} else if (s1.length() > s2.length()) {
					return 1;
				} else {
					return s1.compareTo(s2);
				}
			}
		});
		
		for (String word : wordList) {
			set1.add(word);
			set2.add(word);
		}
		
		
		final Solver solver1 = new Solver(set1, anagram, targetMD5);
		final Solver solver2 = new Solver(set2, anagram, targetMD5);
		
		ExecutorService executor = Executors.newFixedThreadPool(2);

		executor.submit(new Runnable() {

			public void run() {
				solver1.solve();
			}
		});

		executor.submit(new Runnable() {

			public void run() {
				solver2.solve();
			}
		});

		executor.shutdown();

		try {
			executor.awaitTermination(2, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
	
	// filter wordList
	public static TreeSet<String> filterWordList(TreeSet<String> wordList, String anagram) {
		
		TreeSet<String> result = wordList;
		Iterator<String> it = result.iterator();

		while (it.hasNext()) {
			String s = it.next();
			
			// remove non-English words
			if (s.matches(".*\\W.*"))
				it.remove();
			// remove words not contained in anagram
			else if (!stringContains(anagram, s))
				it.remove();
			// remove words without vowels
			else if (s.matches("[^aeiouy]+"))
				it.remove();
			// remove sigle character words other than a or i
			else if (s.matches("[^ai]"))
				it.remove();
			// remove invalid two-character words
			else if (s.matches("[cdefgjklpqrtvxyz]."))
				it.remove();
			// remove customized invalid words
			else if (s.matches("a[lruy]") || 
					s.matches("i[lr]") ||
					s.matches("n[aiu]") ||
					s.matches("ow") ||
					s.matches("si") || 
					s.matches("ur") ||
					s.matches("w[iou]"))
				it.remove();
			
		}
		
		return result;
	}
	
	
	
	// If s1 contains s2, return true. Otherwise return false.
	public static boolean stringContains(String s1, String s2) {

		HashMap<Integer, Integer> charsMap1 = getCharsMap(s1);
		HashMap<Integer, Integer> charsMap2 = getCharsMap(s2);

		for (int i = CHAR_a; i < CHAR_a + NUM_Chars; i++) {
			int value1 = charsMap1.get(i);
			int value2 = charsMap2.get(i);

			if (value2 > value1) {
				return false;
			}
		}

		return true;
	}
	
	
	// get the characters map of string s
	public static HashMap<Integer, Integer> getCharsMap(String s) {
		
		HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>();

		for (int i = CHAR_a; i < CHAR_a + NUM_Chars; i++) {
			resultMap.put(i, 0);
		}

		char[] chars = s.toCharArray();

		for (char c : chars) {
			int key = Integer.valueOf((int) c);
			int value = resultMap.get(key);
			resultMap.put(key, value + 1);
		}

		return resultMap;
	}
	
	
	// remove the white spaces in string s
	public static String removeSpace(String s) {

		return s.replaceAll("\\s", "");
	}
	
	
	public static TreeSet<String> getWordList(String filename) {
		
		TreeSet<String> wordList = new TreeSet<>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;

			while ((line = br.readLine()) != null) {
				wordList.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return wordList;
	}
	
	
	// for debugging
	public static void print(TreeSet<String> wordList) {

		for (String s : wordList) {
			System.out.println(s);
		}
		System.out.println("wordlist size: " + wordList.size());
	}

}

