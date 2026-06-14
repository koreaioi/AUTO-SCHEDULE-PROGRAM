package tave.auto_scheduling.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record ApplicantAssignmentDto(
        List<ApplicantDto> dataList,
        int hardScore,
        int softScore
) {
    public static ApplicantAssignmentDto from(List<ApplicantDto> dataList, int hardScore, int softScore) {
        return ApplicantAssignmentDto.builder()
                .dataList(dataList)
                .hardScore(hardScore)
                .softScore(softScore)
                .build();
    }
}
