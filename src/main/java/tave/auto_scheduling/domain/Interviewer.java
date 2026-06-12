package tave.auto_scheduling.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.EAGER;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "interviewer")
public class Interviewer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String part;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "interviewer_availability", joinColumns = @JoinColumn(name = "interviewer_id"))
    @Column(name = "available_time")
    private List<LocalDateTime> availableTimes = new ArrayList<>();
}
