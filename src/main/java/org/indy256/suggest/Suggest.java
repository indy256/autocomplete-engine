package org.indy256.suggest;

import java.util.*;
import java.util.stream.*;

public class Suggest {

	public static int[] bySubstring(State[] automaton, String substring, int maxResults) {
		int node = 0;
		for (char c : substring.toCharArray()) {
			int next = automaton[node].next[c];
			if (next == -1) {
				return new int[0];
			}
			node = next;
		}
		Set<Integer> occurrences = new HashSet<>();
		collectOccurrences(automaton, node, occurrences, maxResults);
		return occurrences.stream().mapToInt(Integer::intValue).sorted().toArray();
	}

	public static int[] byPrefix(State[] automaton, String prefix, int maxResults) {
		return bySubstring(automaton, '\0' + prefix, maxResults);
	}

	private static void collectOccurrences(State[] automaton, int curNode, Set<Integer> occurrences, int maxResults) {
		if (occurrences.size() == maxResults) {
			return;
		}
		if (automaton[curNode].firstWord != -1) {
			occurrences.add(automaton[curNode].firstWord);
		}
		for (int nextNode : automaton[curNode].invSuffLinks) {
			collectOccurrences(automaton, nextNode, occurrences, maxResults);
		}
	}

	public static State[] buildAutomaton(String... dictionaryWords) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dictionaryWords.length; i++) {
			sb.append('\0');
			sb.append(dictionaryWords[i]);
		}
		return buildSuffixAutomaton(sb);
	}

	public static class State {
		private int length;
		private int suffLink;
		private List<Integer> invSuffLinks = new ArrayList<>(0);
		private int firstWord = -1;
		private int[] next = new int[128];

		{
			Arrays.fill(next, -1);
		}
	}

	private static State[] buildSuffixAutomaton(CharSequence s) {
		int n = s.length();
		State[] st = new State[Math.max(2, 2 * n - 1)];
		st[0] = new State();
		st[0].suffLink = -1;
		int last = 0;
		int size = 1;
		for (int i = 0, curWord = -1; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '\0') {
				++curWord;
			}
			int cur = size++;
			st[cur] = new State();
			st[cur].length = i + 1;
			st[cur].firstWord = curWord;
			int p = last;
			for (; p != -1 && st[p].next[c] == -1; p = st[p].suffLink) {
				st[p].next[c] = cur;
			}
			if (p == -1) {
				st[cur].suffLink = 0;
			} else {
				int q = st[p].next[c];
				if (st[p].length + 1 == st[q].length)
					st[cur].suffLink = q;
				else {
					int clone = size++;
					st[clone] = new State();
					st[clone].length = st[p].length + 1;
					System.arraycopy(st[q].next, 0, st[clone].next, 0, st[q].next.length);
					st[clone].suffLink = st[q].suffLink;
					for (; p != -1 && st[p].next[c] == q; p = st[p].suffLink) {
						st[p].next[c] = clone;
					}
					st[q].suffLink = clone;
					st[cur].suffLink = clone;
				}
			}
			last = cur;
		}
		for (int i = 1; i < size; i++) {
			st[st[i].suffLink].invSuffLinks.add(i);
		}
		return Arrays.copyOf(st, size);
	}

	// random test
	public static void main(String[] args) {
		String[] dictionary = {"ab", "tab", "abab", "azb", "abz"};
		State[] automaton = Suggest.buildAutomaton(dictionary);
		int[] occurrences1 = Suggest.bySubstring(automaton, "z", Integer.MAX_VALUE);
		System.out.println(Arrays.toString(occurrences1));
		int[] occurrences2 = Suggest.bySubstring(automaton, "ab", Integer.MAX_VALUE);
		System.out.println(Arrays.toString(occurrences2));

		Random rnd = new Random(1);
		for (int step = 0; step < 10_000; step++) {
			int n = rnd.nextInt(10) + 1;
			String[] dictionaryWords = new String[n];
			for (int i = 0; i < n; i++) {
				int len = rnd.nextInt(20);
				dictionaryWords[i] = getRandomString(len, rnd);
			}
			int len = rnd.nextInt(5) + 1;
			String needle = getRandomString(len, rnd);

			int[] res1 = IntStream.range(0, dictionaryWords.length).filter(i -> dictionaryWords[i].contains(needle)).toArray();
			int[] res2 = bySubstring(buildAutomaton(dictionaryWords), needle, Integer.MAX_VALUE);

			if (!Arrays.equals(res1, res2)) {
				throw new RuntimeException();
			}
		}
	}

	static String getRandomString(int n, Random rnd) {
		return Stream.generate(() -> String.valueOf((char) ('a' + rnd.nextInt(3)))).limit(n).reduce("", (a, b) -> a + b);
	}
}
