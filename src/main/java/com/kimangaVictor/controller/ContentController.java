package com.kimangaVictor.controller;

import com.kimangaVictor.dto.response.*;
import com.kimangaVictor.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public read-only content API backing every section of the site. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    /** Everything at once. This is what the frontend actually calls on load. */
    @GetMapping("/content")
    public ContentResponse content() {
        return contentService.content();
    }

    @GetMapping("/profile")
    public ProfileResponse profile() {
        return contentService.profile();
    }

    @GetMapping("/projects")
    public List<ProjectResponse> projects() {
        return contentService.projects();
    }

    @GetMapping("/experience")
    public List<ExperienceResponse> experience() {
        return contentService.experience();
    }

    @GetMapping("/skills")
    public List<SkillGroupResponse> skills() {
        return contentService.skills();
    }

    @GetMapping("/certifications")
    public List<CertificationResponse> certifications() {
        return contentService.certifications();
    }
}
