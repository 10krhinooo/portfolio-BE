package com.kimangaVictor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param type   event name, e.g. {@code page_view}, {@code project_click}, {@code cv_download}
 * @param path   page path the event happened on
 * @param target optional subject of the event, e.g. the project slug that was clicked
 */
public record AnalyticsEventRequest(

        @NotBlank
        @Size(max = 60)
        String type,

        @Size(max = 300)
        String path,

        @Size(max = 300)
        String referrer,

        @Size(max = 200)
        String target
) {
}
