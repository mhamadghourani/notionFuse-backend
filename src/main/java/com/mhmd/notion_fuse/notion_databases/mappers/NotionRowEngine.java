package com.mhmd.notion_fuse.notion_databases.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

@Component
@RequiredArgsConstructor
public class NotionRowEngine {

    private final ObjectMapper objectMapper;

    public List<JsonNode> extractRawRows(String rawQueryResponse) {
        List<JsonNode> rows = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(rawQueryResponse);
            JsonNode results = root.path("results");

            if (results.isArray()) {
                for (JsonNode row : results) {
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting rows: " + e.getMessage());
        }
        return rows;
    }

    public Map<String, Object> prepareForInsert(JsonNode rawRow, String sourceName, String primaryTitleName) {

        JsonNode propertiesNode = rawRow.path("properties");
        ObjectNode newProperties = objectMapper.createObjectNode();

        if (propertiesNode.isObject()) {
            ObjectNode properties = (ObjectNode) propertiesNode;

            Iterator<Map.Entry<String, JsonNode>> it = properties.fields();

            while (it.hasNext()) {
                Map.Entry<String, JsonNode> field = it.next();

                String columnName = field.getKey();
                ObjectNode propNode = (ObjectNode) field.getValue().deepCopy();
                String type = propNode.path("type").asText();

                // Skip system-generated fields
                if (List.of(
                        "formula",
                        "rollup",
                        "created_time",
                        "created_by",
                        "last_edited_time",
                        "last_edited_by",
                        "unique_id"
                ).contains(type)) {
                    continue;
                }

                propNode.remove("id");

                if ("title".equals(type)) {
                    newProperties.set("Name", propNode);
                }
                else if (List.of("select", "multi_select", "status").contains(type)) {
                    cleanSelectProperty(propNode, type);
                    newProperties.set(columnName, propNode);
                }
                else {
                    newProperties.set(columnName, propNode);
                }
            }
        }

        ObjectNode mergedFromNode = objectMapper.createObjectNode();
        ObjectNode selectNode = objectMapper.createObjectNode();
        selectNode.put("name", sourceName);
        mergedFromNode.set("select", selectNode);

        newProperties.set("Merged From", mergedFromNode);

        return objectMapper.convertValue(newProperties, Map.class);
    }

    private void cleanSelectProperty(ObjectNode propNode, String type) {
        if ("select".equals(type) || "status".equals(type)) {
            JsonNode node = propNode.path(type);
            if (node.isObject()) {
                ((ObjectNode) node).remove("id");
                ((ObjectNode) node).remove("color");
            }
        }
        else if ("multi_select".equals(type)) {
            JsonNode array = propNode.path("multi_select");
            if (array.isArray()) {
                for (JsonNode item : array) {
                    if (item.isObject()) {
                        ((ObjectNode) item).remove("id");
                        ((ObjectNode) item).remove("color");
                    }
                }
            }
        }
    }
}