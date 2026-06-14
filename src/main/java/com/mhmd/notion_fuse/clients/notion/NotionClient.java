package com.mhmd.notion_fuse.clients.notion;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
@RequiredArgsConstructor
public class NotionClient {

    private final RestClient notionRestClient;

    public Map<String, Object> listDataBases(String accessToken) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("value", "database");
        filter.put("property", "object");
        Map<String, Object> body = new HashMap<>();
        body.put("filter", filter);

        return notionRestClient.post()
                .uri("/search")
                .header("Authorization", "Bearer " + accessToken)
                .body(body)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String listDataBasesAsRawJson(String accessToken) {
        Map<String, Object> filter = new HashMap<>();
        filter.put("value", "database");
        filter.put("property", "object");
        Map<String, Object> body = new HashMap<>();
        body.put("filter", filter);

        return notionRestClient.post()
                .uri("/search")
                .header("Authorization", "Bearer " + accessToken)
                .body(body)
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
                .body(String.class);
    }

    public String getBlock(String blockId, String accessToken) {
        return notionRestClient.get()
                .uri("/blocks/" + blockId)
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .retrieve()
                .body(String.class);
    }

    public String createMergedDatabase(String accessToken, String parentPageId, String title, Map<String, Object> dynamicProperties) {
        Map<String, Object> body = new HashMap<>();
        String cleanId = parentPageId.trim();

        Map<String, Object> parent = new HashMap<>();
        parent.put("type", "page_id");
        parent.put("page_id", cleanId);
        body.put("parent", parent);

        Map<String, String> textContent = new HashMap<>();
        textContent.put("content", title != null ? title : "Untitled Database");
        Map<String, Object> textObj = new HashMap<>();
        textObj.put("text", textContent);
        textObj.put("type", "text");
        List<Map<String, Object>> titleList = new ArrayList<>();
        titleList.add(textObj);
        body.put("title", titleList);
        body.put("properties", (dynamicProperties != null) ? dynamicProperties : new HashMap<String, Object>());

        return notionRestClient.post()
                .uri("/databases")
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public String insertDynamicRow(String accessToken, String databaseId, Map<String, Object> dynamicProperties) {
        Map<String, Object> body = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        parent.put("database_id", databaseId);
        body.put("parent", parent);
        body.put("properties", dynamicProperties != null ? dynamicProperties : new HashMap<String, Object>());

        return notionRestClient.post()
                .uri("/pages")
                .header("Authorization", "Bearer " + accessToken)
                .header("Notion-Version", "2022-06-28")
                .body(body)
                .retrieve()
                .body(String.class);
    }

    public void updateDynamicRow(String accessToken, String pageId, Map<String, Object> dynamicProperties) {
        Map<String, Object> body = new HashMap<>();
        body.put("properties", dynamicProperties != null ? dynamicProperties : new HashMap<String, Object>());
        notionRestClient.patch()
                .uri("/pages/" + pageId)
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