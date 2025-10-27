package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.RequiredArgsConstructor;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflows")
public class Workflow {

    @Id
    @Column(length = 64)
    private String id;

    private String name;

    private boolean active;

    @Column(name = "is_archived")
    private boolean isArchived;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String versionId;
    private int triggerCount;

    @Lob
    @Column(columnDefinition = "jsonb")
    private String rawJson;
}
