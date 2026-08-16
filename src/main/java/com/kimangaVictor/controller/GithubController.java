package com.kimangaVictor.controller;

import com.kimangaVictor.dto.response.GithubRepoResponse;
import com.kimangaVictor.dto.response.GithubStatsResponse;
import com.kimangaVictor.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GithubController {

    private final GithubService githubService;

    @GetMapping("/repos")
    public List<GithubRepoResponse> repos() {
        return githubService.repos();
    }

    @GetMapping("/stats")
    public GithubStatsResponse stats() {
        return githubService.stats();
    }
}
