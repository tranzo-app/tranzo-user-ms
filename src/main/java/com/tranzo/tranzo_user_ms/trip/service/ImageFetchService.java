package com.tranzo.tranzo_user_ms.trip.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tranzo.tranzo_user_ms.trip.enums.ImageSource;
import com.tranzo.tranzo_user_ms.trip.model.TripImageEntity;
import com.tranzo.tranzo_user_ms.trip.repository.TripImageRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageFetchService {

    private final TripImageRepository tripImageRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${trip.image.unsplash.api-key}")
    private String unsplashApiKey;

    @Value("${trip.image.unsplash.api-url}")
    private String unsplashApiUrl;

    @Value("${trip.image.unsplash.per-page}")
    private int unsplashPerPage;

    @Value("${trip.image.unsplash.orientation}")
    private String unsplashOrientation;

    @Value("${trip.image.unsplash.content-filter}")
    private String unsplashContentFilter;

    @Value("${trip.image.default-images}")
    private String defaultImages;

    @Transactional(readOnly = true)
    public List<TripImageEntity> getImagesForDestination(String destination) {
        // First, check if images already exist for this destination
        List<TripImageEntity> existingImages = tripImageRepository.findByDestinationOrderByUsageCountDesc(destination);
        
        if (!existingImages.isEmpty()) {
            log.info("Found {} existing images for destination: {}", existingImages.size(), destination);
            return existingImages;
        }

        // If no images exist, fetch from external API
        log.info("No existing images for destination: {}. Fetching from external API...", destination);
        return fetchImagesFromApi(destination);
    }

    @Transactional
    public List<TripImageEntity> fetchImagesFromApi(String destination) {
        List<TripImageEntity> images = new ArrayList<>();

        if (unsplashApiKey == null || unsplashApiKey.isEmpty()) {
            log.warn("Unsplash API key not configured. Using default images.");
            return getDefaultImages(destination);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Client-ID " + unsplashApiKey);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = buildUnsplashUrl(destination);
            log.debug("Fetching images from Unsplash API: {}", url);

            ResponseEntity<UnsplashSearchResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    UnsplashSearchResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                logRateLimitInfo(response.getHeaders());
                
                List<UnsplashPhoto> photos = response.getBody().getResults();
                if (photos != null && !photos.isEmpty()) {
                    for (UnsplashPhoto photo : photos) {
                        String imageUrl = photo.getUrls() != null ? photo.getUrls().getRegular() : null;
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            TripImageEntity image = TripImageEntity.builder()
                                    .imageUrl(imageUrl)
                                    .destination(destination)
                                    .source(ImageSource.API_FETCHED)
                                    .usageCount(0)
                                    .build();
                            image = tripImageRepository.save(image);
                            images.add(image);
                        }
                    }
                    log.info("Successfully fetched and saved {} images from Unsplash for destination: {}", images.size(), destination);
                } else {
                    log.warn("No images found in API response for destination: {}. Using default images.", destination);
                    return getDefaultImages(destination);
                }
            } else {
                log.warn("Unsplash API returned status {} for destination: {}. Using default images.", 
                        response.getStatusCode(), destination);
                return getDefaultImages(destination);
            }
        } catch (RestClientException e) {
            log.error("Failed to fetch images from Unsplash API for destination: {}. Using default images. Error: {}", 
                    destination, e.getMessage(), e);
            return getDefaultImages(destination);
        }

        return images;
    }

    private String buildUnsplashUrl(String destination) {
        return String.format("%s?query=%s&per_page=%d&orientation=%s&content_filter=%s",
                unsplashApiUrl,
                destination.replace(" ", "+"),
                unsplashPerPage,
                unsplashOrientation,
                unsplashContentFilter);
    }

    private void logRateLimitInfo(HttpHeaders headers) {
        String rateLimit = headers.getFirst("X-Ratelimit-Limit");
        String rateRemaining = headers.getFirst("X-Ratelimit-Remaining");
        if (rateLimit != null && rateRemaining != null) {
            log.debug("Unsplash API Rate Limit - Limit: {}, Remaining: {}", rateLimit, rateRemaining);
            if (Integer.parseInt(rateRemaining) < 50) {
                log.warn("Unsplash API rate limit running low. Remaining: {}", rateRemaining);
            }
        }
    }

    @Transactional
    public List<TripImageEntity> getDefaultImages(String destination) {
        List<TripImageEntity> defaultImageEntities = new ArrayList<>();

        // Check if default images already exist for this destination
        List<TripImageEntity> existingDefaults = tripImageRepository.findByDestinationAndSource(destination, ImageSource.DEFAULT);
        if (!existingDefaults.isEmpty()) {
            return existingDefaults;
        }

        // Create default images
        String[] defaultUrls = defaultImages.split(",");
        for (String url : defaultUrls) {
            String trimmedUrl = url.trim();
            if (!trimmedUrl.isEmpty()) {
                TripImageEntity image = TripImageEntity.builder()
                        .imageUrl(trimmedUrl)
                        .destination(destination)
                        .source(ImageSource.DEFAULT)
                        .usageCount(0)
                        .build();
                image = tripImageRepository.save(image);
                defaultImageEntities.add(image);
            }
        }

        log.info("Created {} default images for destination: {}", defaultImageEntities.size(), destination);
        return defaultImageEntities;
    }

    @Transactional
    public List<TripImageEntity> saveUserProvidedImages(List<String> imageUrls, String destination) {
        List<TripImageEntity> images = new ArrayList<>();
        
        for (String imageUrl : imageUrls) {
            // Check if image already exists
            if (!tripImageRepository.existsByImageUrl(imageUrl)) {
                TripImageEntity image = TripImageEntity.builder()
                        .imageUrl(imageUrl)
                        .destination(destination)
                        .source(ImageSource.USER_PROVIDED)
                        .usageCount(0)
                        .build();
                image = tripImageRepository.save(image);
                images.add(image);
            } else {
                tripImageRepository.findByImageUrl(imageUrl).ifPresent(images::add);
            }
        }
        
        return images;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class UnsplashSearchResponse {
        @JsonProperty("total")
        private Integer total;

        @JsonProperty("total_pages")
        private Integer totalPages;

        @JsonProperty("results")
        private List<UnsplashPhoto> results;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class UnsplashPhoto {
        @JsonProperty("id")
        private String id;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("width")
        private Integer width;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("color")
        private String color;

        @JsonProperty("description")
        private String description;

        @JsonProperty("urls")
        private UnsplashUrls urls;

        @JsonProperty("user")
        private UnsplashUser user;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class UnsplashUrls {
        @JsonProperty("raw")
        private String raw;

        @JsonProperty("full")
        private String full;

        @JsonProperty("regular")
        private String regular;

        @JsonProperty("small")
        private String small;

        @JsonProperty("thumb")
        private String thumb;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    private static class UnsplashUser {
        @JsonProperty("id")
        private String id;

        @JsonProperty("username")
        private String username;

        @JsonProperty("name")
        private String name;
    }
}
