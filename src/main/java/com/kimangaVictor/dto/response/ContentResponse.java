package com.kimangaVictor.dto.response;

import java.util.List;

/**
 * Everything the site needs in one round trip.
 *
 * <p>The backend sleeps on Railway's free tier, so a cold start costs several seconds. Fetching all
 * content in a single request means the visitor pays that penalty once rather than six times.
 */
public record ContentResponse(
        ProfileResponse profile,
        List<StatResponse> stats,
        List<ProjectResponse> projects,
        List<ExperienceResponse> experience,
        List<SkillGroupResponse> skills,
        List<CertificationResponse> certifications
) {
}
