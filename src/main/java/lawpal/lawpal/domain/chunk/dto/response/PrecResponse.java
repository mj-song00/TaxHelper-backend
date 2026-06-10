package lawpal.lawpal.domain.chunk.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PrecResponse {

    private final List<PrecChunkResponseItem> list;
    private final int currentPage; // 현재 페이지 번호
    private final int totalPages; // 총 페이지 수
    private final long totalElements; // 총 요소 수
}
