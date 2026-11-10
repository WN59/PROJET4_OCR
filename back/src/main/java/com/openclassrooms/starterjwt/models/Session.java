package com.openclassrooms.starterjwt.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Entité JPA d'une session de yoga (table {@code sessions}).
 * <p>
 * Relations :
 * <ul>
 *   <li>{@link Teacher} en {@code @OneToOne} via {@code teacher_id} (contrainte UNIQUE actuelle)</li>
 *   <li>Participants {@link User} en {@code @ManyToMany} via la table {@code PARTICIPATE}</li>
 * </ul>
 */
@Entity
@Table(name = "sessions")
@EntityListeners(AuditingEntityListener.class)
@Data
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Session {
    /** Clé primaire auto-incrémentée. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom de la session. */
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String name;

    /** Date de la séance. */
    @NotNull
    @Column(nullable = false)
    private Date date;

    /** Description. */
    @NotNull
    @Size(max = 2500)
    @Column(nullable = false)
    private String description;

    /** Professeur associé (OneToOne → un teacher ne peut être lié qu'à une session). */
    @OneToOne(optional = false)
    @NotNull
    @JoinColumn(name = "teacher_id", referencedColumnName = "id", nullable = false)
    private Teacher teacher;

    /** Utilisateurs inscrits à la session. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "PARTICIPATE",
            joinColumns = @JoinColumn(name = "session_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users;

    @CreatedDate
    @Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
