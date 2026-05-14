package uz.career.career_bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoobleResponse {

    @JsonProperty("totalCount")
    private long totalCount;

    @JsonProperty("jobs")
    private List<JoobleJob> jobs;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JoobleJob {

        @JsonProperty("title")
        private String title;

        @JsonProperty("location")
        private String location;

        @JsonProperty("snippet")
        private String snippet;

        @JsonProperty("salary")
        private String salary;

        @JsonProperty("source")
        private String source;

        @JsonProperty("type")
        private String type;

        @JsonProperty("link")
        private String link;

        @JsonProperty("company")
        private String company;

        @JsonProperty("updated")
        private String updated;

        @JsonProperty("id")
        private Long id;
    }
}