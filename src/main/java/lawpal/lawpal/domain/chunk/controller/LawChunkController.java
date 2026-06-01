package lawpal.lawpal.domain.chunk.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lawpal.lawpal.common.response.ApiResponse;
import lawpal.lawpal.common.response.ApiResponseEnum;
import lawpal.lawpal.domain.chunk.service.ChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/laws/chunks")
@Tag(name = "청크 생성 API ", description = "저장된 내용을 바탕으로 청크를 수동으로 생성합니다.")
public class LawChunkController {

    private final ChunkService chunkService;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> createChunks(){
        chunkService.createChunks();
        ApiResponse<Void> response =  ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
