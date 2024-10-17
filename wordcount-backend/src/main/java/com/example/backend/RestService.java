package com.example.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class RestService {

    private final static String BLOG_POSTS_URL = "https://www.thekey.academy/wp-json/wp/v2/posts";

    private final Logger logger = LoggerFactory.getLogger(RestService.class);
    private final RestTemplate restTemplate;

    public RestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode fetchBlogPosts() {
        logger.info("Fetching blog posts...");
        return restTemplate.getForObject(BLOG_POSTS_URL, JsonNode.class);
    }

}
