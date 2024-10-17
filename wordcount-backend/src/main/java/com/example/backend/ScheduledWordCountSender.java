package com.example.backend;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ScheduledWordCountSender {

    private static final int POLL_INTERVAL = 5000;
    private static final String WEBSOCKET_ENDPOINT = "/topic/wordcounts";

    private final Logger logger = LoggerFactory.getLogger(ScheduledWordCountSender.class);

    private SimpMessagingTemplate template;
    private RestService restService;

    public ScheduledWordCountSender(SimpMessagingTemplate template, RestService restService) {
        this.template = template;
        this.restService = restService;
    }

    /**
     * Uses RestService.fetchBlogPosts to retrieve new blog posts, converts them
     * to a map that associates individual words in these posts with how often they
     * occur, and sends this map to connected frontends via the WEBSOCKET_ENDPOINT.
     */
    @Scheduled(fixedRate = POLL_INTERVAL)
    public void sendWordCounts() {

        JsonNode blogPosts = restService.fetchBlogPosts();
        Map<String, Integer> wordCounts = convertBlogPosts(blogPosts);
        logger.info("Sending word counts...");
        template.convertAndSend(WEBSOCKET_ENDPOINT, wordCounts);
        logger.info("Word counts sent");
    }

    public Map<String, Integer> convertBlogPosts(JsonNode blogPosts) {

        HashMap<String, Integer> wordCounts = new HashMap<>();

        for (JsonNode node : blogPosts) {
            handleJsonNodes(node, wordCounts);
        }

        return wordCounts;
    }

    private void handleJsonNodes(JsonNode node, HashMap<String, Integer> wordCounts) {

        WordCountUtil.extractWordCounts(node.get("title"), wordCounts);
        WordCountUtil.extractWordCounts(node.get("excerpt"), wordCounts);
        WordCountUtil.extractWordCounts(node.get("content"), wordCounts);
    }

}