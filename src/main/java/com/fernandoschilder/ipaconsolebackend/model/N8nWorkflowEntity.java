package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "workflows")
public class N8nWorkflowEntity {
    @Id
    @Column(length = 64)
    private String id;
    private String name;
    private boolean active;
    @Column(name = "is_archived")
    private boolean archived;
    @Column(name = "raw_json", columnDefinition = "text")
    private String rawJson;
}
