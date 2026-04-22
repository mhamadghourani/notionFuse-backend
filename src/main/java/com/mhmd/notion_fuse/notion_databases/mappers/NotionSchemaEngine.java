package com.mhmd.notion_fuse.notion_databases.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
// Use the standard Jackson imports
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

@Component
@RequiredArgsConstructor
public class NotionSchemaEngine {

    private final ObjectMapper objectMapper;

    public String getPrimaryTitleName(String rawSchema) {
        try {
            JsonNode root = objectMapper.readTree(rawSchema);
            JsonNode props = root.path("properties");
            // Use .fields() on the ObjectNode, but ensure we cast safely
            if (props.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> it = props.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> field = it.next();
                    if ("title".equals(field.getValue().path("type").asText())) {
                        return field.getKey();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding primary title: " + e.getMessage());
        }
        return "Name";
    }

    public Map<String, Object> mergeSchemas(String rawSchemaA, String rawSchemaB) {
        Map<String, Object> combinedProperties = new LinkedHashMap<>();

        try {
            JsonNode rootA = objectMapper.readTree(rawSchemaA);
            JsonNode rootB = objectMapper.readTree(rawSchemaB);

            processProperties(rootA.path("properties"), combinedProperties);
            processProperties(rootB.path("properties"), combinedProperties);

            if (!combinedProperties.containsKey("Name")) {
                combinedProperties.put("Name", Map.of("title", Map.of()));
            }

            combinedProperties.put("Merged From", Map.of("select", Map.of("options", List.of())));

        } catch (Exception e) {
            System.err.println("Error merging Notion schemas: " + e.getMessage());
        }

        return combinedProperties;
    }

    private void processProperties(JsonNode props, Map<String, Object> combinedProperties) {
        if (!props.isObject()) return;


        Iterator<Map.Entry<String, JsonNode>> it = props.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> field = it.next();
            String originalName = field.getKey();
            ObjectNode columnConfig = ((ObjectNode) field.getValue()).deepCopy();
            columnConfig.remove("id");

            if ("title".equals(columnConfig.path("type").asText())) {
                if (!combinedProperties.containsKey("Name")) {
                    combinedProperties.put("Name", objectMapper.convertValue(columnConfig, Map.class));
                }
            } else if (!combinedProperties.containsKey(originalName)) {
                combinedProperties.put(originalName, objectMapper.convertValue(columnConfig, Map.class));
            }
        }
    }
}