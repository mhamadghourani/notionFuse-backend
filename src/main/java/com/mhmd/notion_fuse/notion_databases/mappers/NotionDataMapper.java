package com.mhmd.notion_fuse.notion_databases.mappers;

import com.mhmd.notion_fuse.notion_databases.model.NotionDatabaseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class NotionDataMapper {
    private final ObjectMapper objectMapper;

    public List<NotionDatabaseDto> toDatabaseDtoList(String rawJson){
        List<NotionDatabaseDto> dtoList = new ArrayList<>();

        try{
            JsonNode root = objectMapper.readTree(rawJson);
            JsonNode results = root.path("results");
            if(results.isArray()){
                for(JsonNode node:results){
                    String id=node.path("id").asText();
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
                    dtoList.add(new NotionDatabaseDto(id, title, emoji));
                }
            }

        }catch (Exception e){
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


}