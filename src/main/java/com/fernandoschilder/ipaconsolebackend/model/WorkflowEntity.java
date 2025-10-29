package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflows")
public class WorkflowEntity {
    @Id
    @Column(length = 64, name = "workflow_id")
    private String id;
    private String name;
    private boolean active;
    @Column(name = "is_archived")
    private boolean archived;
    @Column(name = "raw_json", columnDefinition = "text")
    private String rawJson;
    @OneToMany(mappedBy = "workflow")
    private Set<ProcessEntity> processes;
}
