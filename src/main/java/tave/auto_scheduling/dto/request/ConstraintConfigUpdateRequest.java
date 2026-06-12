package tave.auto_scheduling.dto.request;

public record ConstraintConfigUpdateRequest(
        Boolean enabled,
        Integer weight,
        Integer threshold,
        String preferredDays
) {}
