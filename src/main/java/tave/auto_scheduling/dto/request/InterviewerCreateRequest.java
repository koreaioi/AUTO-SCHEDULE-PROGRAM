package tave.auto_scheduling.dto.request;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewerCreateRequest(
        String name,
        String part,
        List<LocalDateTime> availableTimes
) {}
