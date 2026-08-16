package com.kimangaVictor.dto.response;

import java.util.List;

public record SkillGroupResponse(String slug, String title, String icon, List<String> items) {
}
