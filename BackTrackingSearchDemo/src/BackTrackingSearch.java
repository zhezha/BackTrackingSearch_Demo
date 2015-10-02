import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Zhao Zhengyang
 *
 */
class Solver {

	private final int CHAR_a = 97;
	private final int NUM_Chars = 26;
	private String targetMD5Hash = "b411f74102bfc6155ad322251a49e7a6";
	private String anagram = "no city dust here";
	private Set<String> wordlist;
	
	private static Object lockWriteFile = new Object();

	// constructor reads file and initializes the set wordlist
	public Solver(TreeSet<String> set) {

		wordlist = set;

		try (BufferedReader br = new BufferedReader(new FileReader("wordlist"))) {
			String line;

			while ((line = br.readLine()) != null) {
				wordlist.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		anagram = removeSpace(anagram);
		filterWordlist();
	}

	// entry solve method
	public void solve() {

		HashMap<Integer, Integer> startMap = getCharsMap(anagram);
		StringBuffer sb = new StringBuffer();

		solveRecursion(startMap, sb);

		System.out.println("No solution found ...");
	}

	// recursively seeking solution
	private void solveRecursion(HashMap<Integer, Integer> charsMap, StringBuffer sb) {

		int checkCharsMapResult = checkCharsMap(charsMap);

		// candidate solution found
		if (checkCharsMapResult == 1) {
			// remove the last white space
			sb.setLength(sb.length() - 1);
			// print candidate solution
			System.out.println(sb.toString());

			if (checkMD5Hash(sb.toString())) {
				System.out.println("Solution found!");
				System.out.println("The secret phase is: \"" + sb.toString() + "\"");
				// write result into file
				synchronized (lockWriteFile) {
					try (BufferedWriter bw = new BufferedWriter(new FileWriter("result.txt", true))) {

						bw.write("The solution is: \"" + sb.toString() + "\"");
						bw.newLine();
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				System.exit(0);
			}

			return;
		}
		// branch fails and backtrack
		else if (checkCharsMapResult == -1) {

			return;
		}
		// branch needs further exploration
		else if (checkCharsMapResult == 0) {

			for (String word : wordlist) {
				
				// the word "i" should appear at the start of the phase
				if ( (word.equals("i") 
						|| word.equals("he") 
						|| word.equals("she") 
						|| word.equals("we") 
						|| word.equals("they")) 
						&& sb.length() != 0) {
					continue;
				}

				HashMap<Integer, Integer> newCharsMap = new HashMap<Integer, Integer>();
				newCharsMap = updateCharsMap(charsMap, word);
				StringBuffer newSB = new StringBuffer();
				newSB.append(sb.toString());
				newSB.append(word);
				newSB.append(" ");

				solveRecursion(newCharsMap, newSB);
			}
		}
	}

	// update current charsMap with word
	private HashMap<Integer, Integer> updateCharsMap(HashMap<Integer, Integer> charsMap, String word) {

		HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>(
				charsMap);
		char[] chars = word.toCharArray();

		for (char c : chars) {
			int key = Integer.valueOf((int) c);
			int value = resultMap.get(key);
			resultMap.put(key, value - 1);
		}

		return resultMap;
	}

	// check if current charsMap is candidate solution
	// return 0 continue recurion, 1 check MD5 hash, -1 fail point and backtrack
	private int checkCharsMap(HashMap<Integer, Integer> charsMap) {

		int count = 0;
		for (int i = CHAR_a; i < CHAR_a + NUM_Chars; i++) {
			if (charsMap.get(i) < 0) {
				return -1;
			} else if (charsMap.get(i) == 0) {
				count++;
			}
		}

		if (count == NUM_Chars) {
			return 1;
		}

		return 0;
	}

	// remove invalid (meaningless) words from wordlist
	private void filterWordlist() {

		Iterator<String> it = wordlist.iterator();

		while (it.hasNext()) {
			String element = it.next();

			if (!isEnglishWord(element)) {
				it.remove();
			} 
			else if (isNotSingleAOrI(element)) {
				it.remove();
			}
			else if (isInvalidTwoLettersWord(element)) {
				it.remove();
			}
			else if (isCustomizedInvalidWord(element)) {
				it.remove();
			}
			else if (notContainVowel(element)) {
				it.remove();
			} 
			else {
				if (!stringContains(anagram, element)) {
					it.remove();
				}
			}
		}
	}
	
	// check if string s is a customized invalid word
	private boolean isCustomizedInvalidWord(String s) {
		
		String[] setStrings = {
				"id", "ir", "ho", "ne", "ni", "nu", "oh", "od", "os", "si", "se", "uh", "ur",
				"che", "chi", "cid", "cos", "cre", "cus",
				"duh", "edy", "ers", "ids", "inc", "ins"
				};
		
		return Arrays.asList(setStrings).contains(s);
	}
	
	// check if string s is an invalid word consists of 2 letters
	private boolean isInvalidTwoLettersWord(String s) {
		char[] chars = s.toCharArray();
		return (chars.length == 2) && (
				(chars[0] == 'c') 
				|| (chars[0] == 'd') 
				|| (chars[0] == 'e')
				|| (chars[0] == 'f')
				|| (chars[0] == 'g')
				|| (chars[0] == 'j')
				|| (chars[0] == 'k')
				|| (chars[0] == 'l')
				|| (chars[0] == 'p')
				|| (chars[0] == 'q')
				|| (chars[0] == 'r')
				|| (chars[0] == 't')
				|| (chars[0] == 'v')
				|| (chars[0] == 'x')
				|| (chars[0] == 'y')
				|| (chars[0] == 'z')
				);
	}
	
	// check if single letter is neither a nor i
	private boolean isNotSingleAOrI(String s) {

		char[] chars = s.toCharArray();
		return (chars.length == 1) && (chars[0] != 97) && (chars[0] != 105);
	}

	// check if string s contains any vowels or not
	private boolean notContainVowel(String s) {

		HashMap<Integer, Integer> charsMap = getCharsMap(s);
		char[] vowel = { 'a', 'e', 'i', 'o', 'u', 'y', 'w' };

		int count = 0;
		for (char c : vowel) {
			int key = (int) c;
			if (charsMap.get(key) == 0)
				count++;
		}

		if (count == vowel.length)
			return true;

		return false;
	}

	// check if string s is an English word
	private boolean isEnglishWord(String s) {

		char[] chars = s.toCharArray();

		for (char c : chars) {
			if (c < CHAR_a || c > CHAR_a + NUM_Chars - 1)
				return false;
		}

		return true;
	}

	// If s1 contains s2, return true. Otherwise return false.
	private boolean stringContains(String s1, String s2) {

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
	private HashMap<Integer, Integer> getCharsMap(String s) {

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
	private String removeSpace(String s) {

		return s.replaceAll("\\s", "");
	}

	// check if string s has the same hash value with target hash value
	private boolean checkMD5Hash(String s) {

		String hash = null;

		try {
			hash = getMD5Hash(s);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		if (hash.equalsIgnoreCase(targetMD5Hash)) {
			return true;
		}

		return false;
	}

	// get MD5 hash of string s
	private String getMD5Hash(String s) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(s.getBytes());

		byte[] bytesDigest = md.digest();

		StringBuffer hexString = new StringBuffer();

		for (int i = 0; i < bytesDigest.length; i++) {
			String hex = Integer.toHexString(bytesDigest[i] & 0xff);

			if (hex.length() == 1) {
				hexString.append(0);
			}
			hexString.append(hex);
		}

		return hexString.toString();
	}

	// for debugging
	public void test() {

		System.out.println("wordlist size: " + wordlist.size());
		for (String s : wordlist) {
			System.out.println(s);
		}

//		 String string = new String("the country side");
//		 try {
//		 System.out.println(getMD5Hash(string));
//		 } catch (NoSuchAlgorithmException e) {
//		 e.printStackTrace();
//		 }
	}
}

public class BackTrackingSearch {

	public static void main(String[] args) {

		// order wordlist by word length descending
		final Solver solver1 = new Solver(new TreeSet<String>(
				new Comparator<String>() {

					public int compare(String s1, String s2) {

						if (s1.length() < s2.length()) {
							return 1;
						} else if (s1.length() > s2.length()) {
							return -1;
						} else {
							return s1.compareTo(s2);
						}
					}
				}));

		// order wordlist by word length ascending
		final Solver solver2 = new Solver(new TreeSet<String>(
				new Comparator<String>() {

					public int compare(String s1, String s2) {

						if (s1.length() < s2.length()) {
							return -1;
						} else if (s1.length() > s2.length()) {
							return 1;
						} else {
							return s1.compareTo(s2);
						}
					}
				}));

		// my computer has two cores
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
		
		// solver2.test();

	}

}
