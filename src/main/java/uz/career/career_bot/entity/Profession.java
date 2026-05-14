package uz.career.career_bot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Profession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;
}