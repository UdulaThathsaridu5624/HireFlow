package com.ctse.profileservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employer_analytics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long companyId;

    @Builder.Default
    @Column(name = "profile_views", columnDefinition = "bigint default 0")
    private Long profileViews = 0L;

    @Builder.Default
    @Column(name = "job_posts", columnDefinition = "bigint default 0")
    private Long jobPosts = 0L;

    @Builder.Default
    @Column(name = "applications_received", columnDefinition = "bigint default 0")
    private Long applicationsReceived = 0L;

    @Builder.Default
    @Column(name = "followers_count", columnDefinition = "bigint default 0")
    private Long followersCount = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
