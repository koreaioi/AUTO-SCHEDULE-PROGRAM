package tave.auto_scheduling.config;

import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tave.auto_scheduling.constraint.InterviewConstraintProvider;
import tave.auto_scheduling.domain.ApplicantAssignment;
import tave.auto_scheduling.domain.InterviewSchedule;

import java.util.List;
import java.util.UUID;

@Configuration
public class OptaPlannerSolverConfig {

    @Bean
    public SolverConfig solverConfig() {
        SolverConfig config = new SolverConfig();

        config.setSolutionClass(InterviewSchedule.class);
        config.setEntityClassList(List.of(ApplicantAssignment.class));
        config.setScoreDirectorFactoryConfig(
                new ScoreDirectorFactoryConfig()
                        .withConstraintProviderClass(InterviewConstraintProvider.class)
        );

        return config;
    }

    @Bean
    public SolverFactory<InterviewSchedule> solverFactory(SolverConfig solverConfig) {
        return SolverFactory.create(solverConfig);
    }

    @Bean
    public SolverManager<InterviewSchedule, UUID> solverManager(SolverFactory<InterviewSchedule> solverFactory) {
        return SolverManager.create(solverFactory);
    }

}
