package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
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

    @OneToOne(mappedBy = "workflow", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private ProcessEntity process;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "workflow_tags",
            joinColumns = @JoinColumn(name = "workflow_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowEntity)) return false;
        WorkflowEntity that = (WorkflowEntity) o;
        return Objects.equals(id, that.id);
    }
    @Override
    public int hashCode() { return Objects.hash(id); }

    public void setProcess(ProcessEntity p) {
        this.process = p;
        if (p != null) p.setWorkflow(this);
    }

}
