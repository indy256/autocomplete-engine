suggest-engine
===================

[![Build Status](https://travis-ci.org/indy256/autocomplete-engine.svg?branch=master)](https://travis-ci.org/indy256/autocomplete-engine)

```java
String[] dictionary = {"ab", "tab", "abab", "azb", "abz"};
State[] automaton = Suggest.buildAutomaton(dictionary);
int[] occurrences1 = Suggest.bySubstring(automaton, "z", Integer.MAX_VALUE); // 3,4
int[] occurrences2 = Suggest.bySubstring(automaton, "ab", Integer.MAX_VALUE); // 0,1,2,4
```
