package com.kimangaVictor.service;

import com.kimangaVictor.domain.*;
import com.kimangaVictor.dto.response.*;
import com.kimangaVictor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Read side of the content CMS. Maps entities to the response records the frontend consumes. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentService {

    private final ProfileRepository profileRepository;
    private final StatRepository statRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillGroupRepository skillGroupRepository;
    private final CertificationRepository certificationRepository;

    public ContentResponse content() {
        return new ContentResponse(
                profile(), stats(), projects(), experience(), skills(), certifications());
    }

    public ProfileResponse profile() {
        return profileRepository.findAll().stream().findFirst().map(this::toResponse).orElse(null);
    }

    public List<StatResponse> stats() {
        return statRepository.findAllByOrderBySortOrderAsc().stream()
                .map(s -> new StatResponse(s.getValue(), s.getLabel()))
                .toList();
    }

    public List<ProjectResponse> projects() {
        return projectRepository.findAllByOrderBySortOrderAsc().stream().map(this::toResponse).toList();
    }

    public List<ExperienceResponse> experience() {
        return experienceRepository.findAllByOrderBySortOrderAsc().stream().map(this::toResponse).toList();
    }

    public List<SkillGroupResponse> skills() {
        return skillGroupRepository.findAllByOrderBySortOrderAsc().stream()
                .map(s -> new SkillGroupResponse(s.getSlug(), s.getTitle(), s.getIcon(), List.copyOf(s.getItems())))
                .toList();
    }

    public List<CertificationResponse> certifications() {
        return certificationRepository.findAllByOrderBySortOrderAsc().stream()
                .map(c -> new CertificationResponse(
                        c.getSlug(), c.getTitle(), c.getIssuer(), c.getAwarded(), c.getCredentialUrl()))
                .toList();
    }

    private ProfileResponse toResponse(Profile p) {
        return new ProfileResponse(p.getFirstName(), p.getLastName(), p.getTitle(), p.getLocation(),
                p.getTagline(), p.getEmail(), p.getPhone(), p.getGithubUrl(), p.getLinkedinUrl(),
                p.getResumeUrl());
    }

    private ProjectResponse toResponse(Project p) {
        return new ProjectResponse(p.getSlug(), p.getTitle(), p.getNumber(), p.getCategory(),
                p.getDescription(), p.getPeriod(), List.copyOf(p.getTags()), p.getRepoUrl(),
                p.getLiveUrl(), p.getThumbnailUrl(), p.isFeatured());
    }

    private ExperienceResponse toResponse(Experience e) {
        return new ExperienceResponse(e.getSlug(), e.getRole(), e.getOrganisation(), e.getLocation(),
                e.getPeriod(), e.getKind().name(), List.copyOf(e.getBullets()));
    }
}
