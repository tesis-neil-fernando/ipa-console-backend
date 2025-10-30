package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "workflows", indexes = {@Index(name = "idx_workflows_active", columnList = "active")})
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

    /**
     * Inverse side of the OneToOne with ProcessEntity.
     * Note: JPA providers may ignore LAZY on OneToOne mappings unless bytecode enhancement or provider-specific
     * proxying is enabled (for example Hibernate's bytecode instrumentation or @LazyToOne).
     * If you rely on lazy-loading here, enable enhancement or consider mapping changes.
     */
    @OneToOne(mappedBy = "workflow", fetch = FetchType.LAZY, optional = true)
    private ProcessEntity process;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "workflow_tags",
            joinColumns = @JoinColumn(name = "workflow_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    public WorkflowEntity() {
    }

    public WorkflowEntity(String id, String name, boolean active, boolean archived, String rawJson) {
        this.id = id;
        this.name = name;
        this.active = active;
        this.archived = archived;
        this.rawJson = rawJson;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity p) {
        this.process = p;
        if (p != null) p.setWorkflow(this);
    }

    public Set<TagEntity> getTags() {
        return tags;
    }

    public void setTags(Set<TagEntity> tags) {
        this.tags = tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkflowEntity)) return false;
        WorkflowEntity that = (WorkflowEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
