package com.example.backend;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class WordCountUtil {

    /**
     * Extracts individual words and how often they occur from the given JsonNode,
     * and updates the given HashMap with these counts. If a word already exists in
     * the map, the count from the new node is added to the count in the map.
     * Words are cleaned from unwanted characters and transformed to lowercase.
     * 
     * The JsonNode needs to have a "rendered" key, otherwise an exception is
     * thrown.
     * 
     * @param node       The node to extract the words and counts from
     * @param wordCounts The map to store the words and counts in
     */
    public static void extractWordCounts(JsonNode node, Map<String, Integer> wordCounts) {

        if (node == null) {
            return;
        }

        if (node.isArray()) {
            throw new IllegalArgumentException("Array nodes are not supported");
        }

        if (!node.has("rendered")) {
            throw new IllegalArgumentException("Node does not have a 'rendered' key");
        }

        String content = Jsoup.parse(node.get("rendered").toString()).text();
        String[] words = content.split("\\s+");
        for (String word : words) {
            word = word.toLowerCase().trim();
            word = cleanWord(word);
            if (StringUtils.isNotBlank(word)) {
                wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
            }
        }
    }

    /**
     * Cleans the given word by removing all newline and tab characters, and
     * replacing all non-alphanumeric characters from the start end end of the
     * string. Considers characters from all languages, e.g. to support foreign
     * names.
     * 
     * @param word The string to clean
     * @return The string cleaned from all unwanted characters
     */
    private static String cleanWord(String word) {

        word = word.replace("\\t", "").replace("\\n", "");
        word = word.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");
        return word;
    }

}
