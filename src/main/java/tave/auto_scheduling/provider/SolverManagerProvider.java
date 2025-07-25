package tave.auto_scheduling.provider;

import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.springframework.context.annotation.Configuration;
import tave.auto_scheduling.config.TerminateType;
import tave.auto_scheduling.config.TerminationPolicyConfig;
import tave.auto_scheduling.domain.InterviewSchedule;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class SolverManagerProvider {

    private final SolverConfig solverConfig;
    private final TerminationPolicyConfig terminationPolicyConfig;

    public SolverManager<InterviewSchedule, UUID> createSolverManager(TerminateType type) {

        TerminationConfig terminationConfig = terminationPolicyConfig.provide(type);
        solverConfig.setTerminationConfig(terminationConfig);

        return SolverManager.create(solverConfig);
    }

}