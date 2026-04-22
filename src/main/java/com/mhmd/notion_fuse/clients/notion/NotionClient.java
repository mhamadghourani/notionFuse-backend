package com.mhmd.notion_fuse.clients.notion;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotionClient {

    private final RestClient notionRestClient;

    public Map<String, Object> listDataBases(String accessToken){
        return notionRestClient.post()
                .uri("/search")
                .header("Authorization", "Bearer " + accessToken)
                .body(Map.of("filter",
                        Map.of(
                                "value","database","property","object"
                        )))
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String listDataBasesAsRawJson(String accessToken) {
        return notionRestClient.post()
                .uri("/search")
                .header("Authorization", "Bearer " + accessToken)
                .body(Map.of("filter", Map.of(
                        "value", "database",
                        "property", "object"
                )))
                .retrieve()
                .body(String.class);
    }


    public String queryDatabase(String databaseId, String accessToken, String startCursor) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("page_size", 100);

        if (startCursor != null && !startCursor.isBlank()) {
            bodyMap.put("start_cursor", startCursor);
        }

        return notionRestClient.post()
                .uri("/databases/" + databaseId + "/query")
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .body(bodyMap)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
                })
                .body(String.class);
    }


    public String createMergedDatabase(String accessToken, String parentPageId, String title, Map<String, Object> dynamicProperties) {
        Map<String, Object> body = Map.of(
                "parent", Map.of("type", "page_id", "page_id", parentPageId),
                "title", List.of(Map.of(
                        "type", "text",
                        "text", Map.of("content", title)
                )),
                "properties", dynamicProperties
        );

        return notionRestClient.post()
                .uri("/databases")
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .body(body)
                .retrieve()
                .body(String.class);
    }


    public void insertDynamicRow(String accessToken, String databaseId, Map<String, Object> dynamicProperties) {
        Map<String, Object> body = Map.of(
                "parent", Map.of("database_id", databaseId),
                "properties", dynamicProperties
        );

        notionRestClient.post()
                .uri("/pages")
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
    public String getDatabase(String databaseId, String accessToken) {
        return notionRestClient.get()
                .uri("/databases/" + databaseId)
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .retrieve()
                .body(String.class);
    }
}
