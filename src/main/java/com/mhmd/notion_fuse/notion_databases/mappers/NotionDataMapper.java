package com.mhmd.notion_fuse.notion_databases.mappers;

import com.mhmd.notion_fuse.notion_databases.model.NotionDatabaseDto;
import com.mhmd.notion_fuse.exceptions.InvalidNotionParentException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class NotionDataMapper {
    private final ObjectMapper objectMapper;

    public List<NotionDatabaseDto> toDatabaseDtoList(String rawJson){
        List<NotionDatabaseDto> dtoList = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode results = root.path("results");
            if (results.isArray()) {
                for (JsonNode node : results) {
                    String id = node.path("id").asText();

                    String title = "Untitled Database";
                    JsonNode titleArray = node.path("title");
                    if (titleArray.isArray() && !titleArray.isEmpty()) {
                        title = titleArray.get(0).path("plain_text").asText(title);
                    }

                    String emoji = null;
                    JsonNode iconNode = node.path("icon");
                    if (!iconNode.isMissingNode() && "emoji".equals(iconNode.path("type").asText())) {
                        emoji = iconNode.path("emoji").asText(null);
                    }

                    String url = node.path("url").asText();

                    String parentId = null;
                    JsonNode parentNode = node.path("parent");
                    String parentType = parentNode.path("type").asText();

                    if ("page_id".equals(parentType)) {
                        parentId = parentNode.path("page_id").asText();
                    } else if ("block_id".equals(parentType)) {
                        parentId = parentNode.path("block_id").asText();
                    } else if ("workspace".equals(parentType)) {
                        parentId = "workspace";
                    }

                    dtoList.add(new NotionDatabaseDto(id, title, emoji, url, parentId));
                }
            }

        } catch (Exception e) {
            System.err.println("Notion Parsing Layer Exception: " + e.getMessage());
        }
        return dtoList;
    }

    public String extractDatabaseId(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("id").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to read newly created database target metadata ID", e);
        }
    }

    public String extractParentIdFromSchema(String databaseSchemaJson) {
        try {
            JsonNode root = objectMapper.readTree(databaseSchemaJson);
            JsonNode parentNode = root.path("parent");
            String type = parentNode.path("type").asText();

            if ("page_id".equals(type)) {
                return parentNode.path("page_id").asText();
            } else if ("database_id".equals(type)) {
                return parentNode.path("database_id").asText();
            } else if ("block_id".equals(type)) {
                // Instantly block the execution flow and alert the ControllerAdvice
                throw new InvalidNotionParentException(
                        "Your source database is nested inside a column block or structural layout. " +
                                "Please move the database out of the column into the main body of the page before merging."
                );
            }
        } catch (InvalidNotionParentException e) {
            throw e; // Pass our custom error upward to the controller safety net
        } catch (Exception e) {
            System.err.println("Error extracting parent ID: " + e.getMessage());
        }
        return null;
    }
}