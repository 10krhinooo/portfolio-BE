package com.kimangaVictor.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.kimangaVictor.config.PortfolioProperties;
import com.kimangaVictor.domain.*;
import com.kimangaVictor.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Loads {@code seed/portfolio.json} into the content tables on boot.
 *
 * <p>Seeding is keyed on each entity's slug, so re-running it updates existing rows in place rather
 * than duplicating them. That makes the JSON file the single source of truth: edit it, redeploy,
 * and the API reflects the change without a manual migration. Rows whose slug has been removed from
 * the JSON are deleted, so the database can never drift ahead of the file.
 */
@Component
@RequiredArgsConstructor
public class ContentSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ContentSeeder.class);
    private static final String SEED_RESOURCE = "seed/portfolio.json";

    private final PortfolioProperties properties;
    private final JsonMapper jsonMapper;
    private final ProfileRepository profileRepository;
    private final StatRepository statRepository;
    private final ProjectRepository projectRepository;
    private final ExperienceRepository experienceRepository;
    private final SkillGroupRepository skillGroupRepository;
    private final CertificationRepository certificationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        JsonNode root;
        try (InputStream in = new ClassPathResource(SEED_RESOURCE).getInputStream()) {
            root = jsonMapper.readTree(in);
        }

        if (properties.seed().reset()) {
            log.info("portfolio.seed.reset=true, clearing content tables before seeding");
            projectRepository.deleteAll();
            experienceRepository.deleteAll();
            skillGroupRepository.deleteAll();
            certificationRepository.deleteAll();
            statRepository.deleteAll();
            profileRepository.deleteAll();
        }

        seedProfile(root.path("profile"));
        seedStats(root.path("stats"));
        seedProjects(root.path("projects"));
        seedExperience(root.path("experience"));
        seedSkills(root.path("skills"));
        seedCertifications(root.path("certifications"));

        log.info("Seeded content: {} projects, {} experience entries, {} skill groups, {} certifications",
                projectRepository.count(), experienceRepository.count(),
                skillGroupRepository.count(), certificationRepository.count());
    }

    private void seedProfile(JsonNode node) {
        Profile profile = profileRepository.findAll().stream().findFirst().orElseGet(Profile::new);
        profile.setFirstName(text(node, "firstName"));
        profile.setLastName(text(node, "lastName"));
        profile.setTitle(text(node, "title"));
        profile.setLocation(text(node, "location"));
        profile.setTagline(text(node, "tagline"));
        profile.setEmail(text(node, "email"));
        profile.setPhone(text(node, "phone"));
        profile.setGithubUrl(text(node, "githubUrl"));
        profile.setLinkedinUrl(text(node, "linkedinUrl"));
        profile.setResumeUrl(text(node, "resumeUrl"));
        profileRepository.save(profile);
    }

    /** Stats have no natural key, so they are simply replaced wholesale. */
    private void seedStats(JsonNode array) {
        statRepository.deleteAll();
        int order = 0;
        for (JsonNode node : array) {
            Stat stat = new Stat();
            stat.setValue(text(node, "value"));
            stat.setLabel(text(node, "label"));
            stat.setSortOrder(order++);
            statRepository.save(stat);
        }
    }

    private void seedProjects(JsonNode array) {
        upsert(array, projectRepository, projectRepository.findAll(), Project::getSlug, Project::new,
                (node, project) -> {
                    project.setSlug(text(node, "slug"));
                    project.setNumber(text(node, "number"));
                    project.setTitle(text(node, "title"));
                    project.setCategory(text(node, "category"));
                    project.setDescription(text(node, "description"));
                    project.setPeriod(text(node, "period"));
                    project.setRepoUrl(text(node, "repoUrl"));
                    project.setLiveUrl(text(node, "liveUrl"));
                    project.setThumbnailUrl(text(node, "thumbnailUrl"));
                    project.setFeatured(node.path("featured").asBoolean(false));
                    project.setTags(strings(node.path("tags")));
                });
    }

    private void seedExperience(JsonNode array) {
        upsert(array, experienceRepository, experienceRepository.findAll(), Experience::getSlug, Experience::new,
                (node, experience) -> {
                    experience.setSlug(text(node, "slug"));
                    experience.setRole(text(node, "role"));
                    experience.setOrganisation(text(node, "organisation"));
                    experience.setLocation(text(node, "location"));
                    experience.setPeriod(text(node, "period"));
                    experience.setKind(Experience.Kind.valueOf(node.path("kind").asText("WORK")));
                    experience.setBullets(strings(node.path("bullets")));
                });
    }

    private void seedSkills(JsonNode array) {
        upsert(array, skillGroupRepository, skillGroupRepository.findAll(), SkillGroup::getSlug, SkillGroup::new,
                (node, group) -> {
                    group.setSlug(text(node, "slug"));
                    group.setTitle(text(node, "title"));
                    group.setIcon(text(node, "icon"));
                    group.setItems(strings(node.path("items")));
                });
    }

    private void seedCertifications(JsonNode array) {
        upsert(array, certificationRepository, certificationRepository.findAll(), Certification::getSlug,
                Certification::new,
                (node, certification) -> {
                    certification.setSlug(text(node, "slug"));
                    certification.setTitle(text(node, "title"));
                    certification.setIssuer(text(node, "issuer"));
                    certification.setAwarded(text(node, "awarded"));
                    certification.setCredentialUrl(text(node, "credentialUrl"));
                });
    }

    /**
     * Matches JSON entries to existing rows by slug, applies {@code apply}, assigns the sort order
     * from the array position, and deletes any row whose slug no longer appears in the JSON.
     */
    private <T> void upsert(JsonNode array,
                            org.springframework.data.jpa.repository.JpaRepository<T, Long> repository,
                            List<T> existing,
                            java.util.function.Function<T, String> slugOf,
                            java.util.function.Supplier<T> factory,
                            BiConsumer<JsonNode, T> apply) {
        List<String> seenSlugs = new ArrayList<>();
        int order = 0;
        for (JsonNode node : array) {
            String slug = text(node, "slug");
            seenSlugs.add(slug);
            T entity = existing.stream()
                    .filter(e -> slug.equals(slugOf.apply(e)))
                    .findFirst()
                    .orElseGet(factory);
            apply.accept(node, entity);
            setSortOrder(entity, order++);
            repository.save(entity);
        }
        existing.stream()
                .filter(e -> !seenSlugs.contains(slugOf.apply(e)))
                .forEach(repository::delete);
    }

    private void setSortOrder(Object entity, int order) {
        switch (entity) {
            case Project p -> p.setSortOrder(order);
            case Experience e -> e.setSortOrder(order);
            case SkillGroup s -> s.setSortOrder(order);
            case Certification c -> c.setSortOrder(order);
            default -> throw new IllegalArgumentException("No sort order on " + entity.getClass());
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static List<String> strings(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(node -> values.add(node.asText()));
        return values;
    }
}
