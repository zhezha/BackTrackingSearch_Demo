import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

class Solver {

	private final int CHAR_a = 97;
	private final int NUM_Chars = 26;
	private String targetMD5Hash;
	private String anagram;
	private Set<String> wordList;
	
	private static Object lockWriteFile = new Object();

	
	// constructor reads file and initializes the set wordlist
	public Solver(TreeSet<String> wordList, String anagram, String targetMD5) {

		this.wordList = wordList;
		this.anagram = anagram;
		this.targetMD5Hash = targetMD5;
	}

	
	// entry solve method
	public void solve() {

		HashMap<Integer, Integer> startMap = getCharsMap(anagram);
		StringBuffer sb = new StringBuffer();

		solveRecursion(startMap, sb);

		System.out.println("No solution found ...");
	}

	
	// recursively search for solution
	private void solveRecursion(HashMap<Integer, Integer> charsMap, StringBuffer sb) {

		int checkCharsMapResult = checkCharsMap(charsMap);

		// candidate solution found
		if (checkCharsMapResult == 1) {
			// remove the last white space
			sb.setLength(sb.length() - 1);
			// print candidate solution
			System.out.println(sb.toString());

			// if candidate is solution, write into result file and
			// exit the execution
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

			// return to the for loop in (checkCharsMapResult == 0)
			return;
		}
		// branch fails and backtrack
		else if (checkCharsMapResult == -1) {

			// return to the for loop in (checkCharsMapResult == 0)
			return;
		}
		// branch needs further exploration
		else if (checkCharsMapResult == 0) {

			for (String word : wordList) {
				// the word "i" should appear at the start of the phase
				if (word.equalsIgnoreCase("i") && sb.length() != 0) {
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

		HashMap<Integer, Integer> resultMap = new HashMap<Integer, Integer>(charsMap);
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
			} 
			else if (charsMap.get(i) == 0) {
				count++;
			}
		}

		if (count == NUM_Chars) {
			return 1;
		}

		return 0;
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
}