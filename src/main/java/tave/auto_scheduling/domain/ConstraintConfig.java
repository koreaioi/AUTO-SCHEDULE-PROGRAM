package tave.auto_scheduling.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "constraint_config")
public class ConstraintConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "constraint_name", nullable = false, unique = true)
    private String constraintName;

    @Column(name = "constraint_type", nullable = false)
    private String constraintType; // "HARD" or "SOFT"

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int weight = 1;

    @Column
    private Integer threshold;

    @Column(name = "target_part")
    private String targetPart;

    @Column(name = "preferred_days")
    private String preferredDays; // 쉼표 구분 요일 (ex: "SUNDAY,MONDAY")
}
