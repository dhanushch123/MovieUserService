package com.movieapp.userservice.configurations;
import java.util.*;


class TrieNode {
    char data;
    TrieNode[] children;
    boolean isTerminating;
    ArrayList<String> suffixes;
    int wordCount;


    public TrieNode(char ch) {
        // for root node \u0000 -> empty char
        this.data = ch;
        children = new TrieNode[26];
        suffixes = new ArrayList<>();
        wordCount = 0;
    }


    public void add(String word) {
        insertNode(word,this);
    }


    public void insertNode(String word,TrieNode cur) {
        if(word.isEmpty()) {
            cur.isTerminating = true;
            cur.wordCount = cur.wordCount+1;
            return;
        }
        char ch = word.charAt(0);
        int index = ch - 'a';
        cur.suffixes.add(word);
        if(cur.children[index] == null) {
            cur.children[index] = new TrieNode(ch);
        }
        TrieNode next = cur.children[index];
        insertNode(word.substring(1),next);
    }


    public int getWordCount(String s) {
        return getWordCount(s,this);
    }


    public int getWordCount(String word,TrieNode cur) {
        if(word.isEmpty()) return cur.wordCount;
        char ch = word.charAt(0);
        int index = ch - 'a';
        cur.suffixes.add(word);
        TrieNode next = cur.children[index];
        if(next == null) return 0;
        return getWordCount(word.substring(1),next);
    }


    public boolean contains(String word) {
        if(word.length() <= 0) return false;
        return containsWord(word,this);
    }


    public boolean containsWord(String word,TrieNode current) {
        if(word.isEmpty()) return current.isTerminating;
        char ch = word.charAt(0);
        int index = ch - 'a';
        TrieNode next = current.children[index];
        if(next == null) return false;
        return containsWord(word.substring(1),next);
    }


    public boolean startsWith(String prefix) {
        if(prefix.isEmpty()) return false;
        return hasPrefix(prefix,this);
    }


    public boolean hasPrefix(String prefix,TrieNode current) {
        if(prefix.isEmpty()) return true;
        char ch = prefix.charAt(0);
        int index = ch - 'a';
        TrieNode next = current.children[index];
        if(next == null) return false;
        return hasPrefix(prefix.substring(1),next);
    }


    public List<String> wordsStartsWith(String prefix) {
        if(prefix.isEmpty()) return new ArrayList<>();
        boolean[] validPrefix = new boolean[1];
        List<String> words = collectWords(prefix,this,validPrefix);
        List<String> res = new ArrayList<>(words);
        if(validPrefix[0])  res.add(prefix);
        return res = res.stream().map(s -> {
            if(!s.equals(prefix)) return prefix + s;
            return s;
        }).toList();
    }


    public int countWordsStartingWith(String prefix) {
        if(prefix.isEmpty()) return 0;
        boolean[] validPrefix = new boolean[1];
        List<String> result = collectWords(prefix,this,validPrefix);
        return validPrefix[0] ? result.size() + 1 : result.size();
    }


    public List<String> collectWords(String prefix,TrieNode current,boolean[] validPrefix) {
        if(prefix.isEmpty()) {
            // System.out.println(current.data);
            validPrefix[0] = current.isTerminating;
            return current.suffixes;
        }


        char ch = prefix.charAt(0);
        int index = ch - 'a';
        TrieNode next = current.children[index];
        if(next == null) return new ArrayList<>();
        return collectWords(prefix.substring(1),next,validPrefix);
    }


    public void eraseWord(String word) {
        boolean[] wordDeleted = new boolean[1];
        deleteNode(word,this,wordDeleted);
    }


    public boolean deleteNode(String word,TrieNode current,boolean[] wordDeleted) {
        if(word.isEmpty()) {
            if(current.isTerminating) {
                wordDeleted[0] = true;
                current.wordCount--;
                if(current.wordCount == 0) current.isTerminating = false;
                return current.suffixes.isEmpty() && current.wordCount == 0; // if suffixes exist keep char else delete char
            }
            return false; // word doesnt exist
        }
        char ch = word.charAt(0);
        int index = ch - 'a';
        TrieNode next = current.children[index];
        if(next == null) return false;
        String nextStr = word.substring(1);
        boolean canDelete = deleteNode(nextStr,next,wordDeleted);
        if(canDelete) current.children[index] = null;
        if(wordDeleted[0]) current.suffixes.remove(word);
        return !current.isTerminating && canDelete && current.suffixes.isEmpty();
    }

    public String longestWordWithAllPrefixes(String[] words) {
        for(String word : words) this.add(word);
        // Now check a word that has all prefixes
        int maxLen = 0;
        String result = "";
        for(String word : words) {
            if(word.isEmpty()) continue;
            boolean containsAll = containsAllPrefixes(word,this);
            if(containsAll && ((word.length() > maxLen) || (word.length() == maxLen && word.compareTo(result) < 0))) {
                maxLen = word.length();
                result = word;
            }
        }
        return result;
    }

    public boolean containsAllPrefixes(String word,TrieNode current) {
        if(word.isEmpty()) return true;
        char ch = word.charAt(0);
        int index = ch - 'a';
        TrieNode next = current.children[index];
        if(next == null || !next.isTerminating) return false;
        return containsAllPrefixes(word.substring(1),next);
    }




    public static void main(String[] args) {
        TrieNode root = new TrieNode('\u0000');

        root.add("app");
        root.add("app");
        root.add("application");
        root.add("appliance");
        root.add("apply");
        root.eraseWord("application");

        System.out.println(root.contains("app"));
        System.out.println(root.getWordCount("app"));
        System.out.println(root.countWordsStartingWith("app"));
        System.out.println(root.wordsStartsWith("app"));

        String[] words = {"a", "ap", "app", "appl", "apple", "apply", "banana", "ban", "bana", "banan", "bananas"};
        System.out.println(root.longestWordWithAllPrefixes(words));
    }


}


