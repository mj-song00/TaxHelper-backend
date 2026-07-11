package lawpal.lawpal.domain.chunk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lawpal.lawpal.common.response.ApiResponse;
import lawpal.lawpal.common.response.ApiResponseEnum;
import lawpal.lawpal.domain.chunk.dto.request.VectorChunkSearchRequest;
import lawpal.lawpal.domain.chunk.dto.response.ChunkResponse;
import lawpal.lawpal.domain.chunk.dto.response.PrecResponse;
import lawpal.lawpal.domain.chunk.service.ChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chunks")
@Tag(name = "청크 생성 API ", description = "저장된 내용을 바탕으로 청크를 수동으로 생성합니다.")
public class ChunkController {

    private final ChunkService chunkService;

    @PostMapping("")
    @Operation(summary = "조문, 부칙, 개정문 단위로 청크를 저장합니다.", description = "현재 항, 목, 호 단위의 청크는 없습니다.")
    public ResponseEntity<ApiResponse<Void>> createChunks(){
        chunkService.createChunks();
        ApiResponse<Void> response =  ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/law-chunks")
    @Operation(
            summary = "저장된 청크를 조회합니다.",
            description = "현재는 조문, 부칙, 개정문 단위 청크만 제공합니다."
    )
    public  ResponseEntity<ApiResponse<ChunkResponse>>  getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> keywords,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> lawNames

    ) {

        Pageable pageable = PageRequest.of(page - 1, size);

        ChunkResponse response = chunkService.getChunks(
                pageable,
                keywords,
                query,
                lawNames
        );


        ApiResponse<ChunkResponse> apiResponse =
                ApiResponse.successWithData(response, ApiResponseEnum.GET_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/law-chunks/search")
    @Operation(
            summary = "저장된 법령 청크를 벡터 유사도로 조회합니다.",
            description = "FastAPI가 생성한 queryEmbedding을 pgvector <-> 연산자로 검색합니다."
    )
    public ResponseEntity<ApiResponse<ChunkResponse>> searchLawChunksByVector(
            @RequestBody VectorChunkSearchRequest request
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(request.getPage(), 1) - 1,
                Math.max(request.getSize(), 1)
        );

        ChunkResponse response = chunkService.getChunksByVectorSearch(pageable, request);

        ApiResponse<ChunkResponse> apiResponse =
                ApiResponse.successWithData(response, ApiResponseEnum.GET_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/prec-chunks")
    @Operation(summary = "판시사항, 참조판례, 사건명, 판결요지, 판례내용 단위로 청크를 저장합니다.")
    public ResponseEntity<ApiResponse<Void>> createPrecChunks(){
        chunkService.createPrec();
        ApiResponse<Void> response =  ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return  ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/prec-chunks/search")
    @Operation(
            summary = "저장된 판례 청크를 벡터 유사도로 조회합니다.",
            description = "FastAPI가 생성한 queryEmbedding을 pgvector <-> 연산자로 검색합니다."
    )
    public ResponseEntity<ApiResponse<PrecResponse>> searchPrecChunksByVector(
            @RequestBody VectorChunkSearchRequest request
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(request.getPage(), 1) - 1,
                Math.max(request.getSize(), 1)
        );

        PrecResponse response = chunkService.getPrecsByVectorSearch(pageable, request);

        ApiResponse<PrecResponse> apiResponse =
                ApiResponse.successWithData(response, ApiResponseEnum.GET_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/prec-chunks")
    @Operation(
            summary = "저장된 판례 청크를 조회합니다.",
            description = "판시사항, 참조판례, 사건명, 판결요지, 판례내용을 제공합니다."
    )
    public ResponseEntity<ApiResponse<PrecResponse>> getPrecList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> keywords,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> courtNames,
            @RequestParam(required = false) List<String> caseNumbers
    ){
        Pageable pageable = PageRequest.of(page - 1, size);

        PrecResponse response = chunkService.getPrecs(
                pageable,
                keywords,
                query,
                courtNames,
                caseNumbers
        );

        ApiResponse<PrecResponse> apiResponse =
                ApiResponse.successWithData(response, ApiResponseEnum.GET_SUCCESS);

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }
}
