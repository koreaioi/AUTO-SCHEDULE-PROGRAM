package tave.auto_scheduling.dto.response;

import lombok.Builder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import java.io.ByteArrayInputStream;

@Builder
public record ExcelFileInputStreamDto(
        InputStreamResource inputStreamResource,
        HttpHeaders headers,
        long contentLength
) {

    public static ExcelFileInputStreamDto from(ByteArrayInputStream byteArrayInputStream, HttpHeaders headers, long contentLength) {
        return ExcelFileInputStreamDto.builder()
                .inputStreamResource(new InputStreamResource(byteArrayInputStream))
                .headers(headers)
                .contentLength(contentLength)
                .build();
    }

}