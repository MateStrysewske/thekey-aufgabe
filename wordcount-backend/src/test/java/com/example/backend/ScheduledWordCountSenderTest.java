package com.example.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScheduledWordCountSenderTest {

    private static final String WEBSOCKET_ENDPOINT = "/topic/wordcounts";

    @Mock
    private RestService restService;

    @Mock
    private SimpMessagingTemplate template;

    @InjectMocks
    private ScheduledWordCountSender sender;

    @BeforeEach
    public void setUpTests() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendWordCounts() {

        JsonNode mockBlogPosts = new ObjectMapper().createObjectNode();

        Map<String, Integer> mockWordCounts = new HashMap<>();
        mockWordCounts.put("word1", 2);
        mockWordCounts.put("word2", 7);

        when(restService.fetchBlogPosts()).thenReturn(mockBlogPosts);

        ScheduledWordCountSender spySender = spy(sender);
        doReturn(mockWordCounts).when(spySender).convertBlogPosts(mockBlogPosts);
        spySender.sendWordCounts();

        verify(template).convertAndSend(WEBSOCKET_ENDPOINT, mockWordCounts);
    }

    @Test
    public void testCorrectWordCounts() throws Exception {

        Map<String, Integer> wordCounts = extractWordCountsFromTestData(
                "src/test/resources/blog-posts/test-data-correct-word-counts.txt");

        assertEquals(3, wordCounts.get("this"));
        // Ensure that words are transformed to lowercase
        assertNull(wordCounts.get("This"));
        assertEquals(2, wordCounts.get("a"));
        assertEquals(1, wordCounts.get("title"));
        assertEquals(1, wordCounts.get("2"));
    }

    @Test
    public void testJsonSections() throws Exception {

        Map<String, Integer> wordCounts = extractWordCountsFromTestData(
                "src/test/resources/blog-posts/test-data-json-sections.txt");

        // Ensure that blog post with ID 2 is still converted
        assertEquals(2, wordCounts.get("much"));
        // Ensure that only "title", "content" and "excerpt" are converted
        assertNull(wordCounts.get("fish"));
    }

    @Test
    public void testSpecialCharactersAreCleared() throws Exception {

        Map<String, Integer> wordCounts = extractWordCountsFromTestData(
                "src/test/resources/blog-posts/test-data-special-characters.txt");

        assertEquals(2, wordCounts.get("characters"));
        assertEquals(1, wordCounts.get("some"));
        assertEquals(1, wordCounts.get("doesn't"));
        assertNull(wordCounts.get("information."));
        assertNull(wordCounts.get("\t\n\n\t\n"));
    }

    @Test
    public void testForeignCharactersAreIncluded() throws Exception {

        Map<String, Integer> wordCounts = extractWordCountsFromTestData(
                "src/test/resources/blog-posts/test-data-foreign-characters.txt");

        assertEquals(1, wordCounts.get("egalité"));
        assertEquals(1, wordCounts.get("être"));
        assertEquals(1, wordCounts.get("spaß"));
        assertEquals(1, wordCounts.get("äonen"));
        assertEquals(1, wordCounts.get("übellaunig"));
    }

    private Map<String, Integer> extractWordCountsFromTestData(String fileName) throws Exception {

        String testDataPath = fileName;
        String testData = new String(Files.readAllBytes(Paths.get(testDataPath)));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testDataJson = mapper.readTree(testData);

        return sender.convertBlogPosts(testDataJson);
    }

}
