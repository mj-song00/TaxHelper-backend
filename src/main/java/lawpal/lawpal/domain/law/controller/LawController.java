package lawpal.lawpal.domain.law.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lawpal.lawpal.common.response.ApiResponse;
import lawpal.lawpal.common.response.ApiResponseEnum;
import lawpal.lawpal.domain.law.service.LawService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "국가법령정보 공동활용 API 요청 ", description = "국가법령정보 공동활용 OPEN API를 요청합니다.")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/laws")
public class LawController {

    public final LawService lawService;


    @Operation(summary = "OPEN API 데이터 요청", description = "법령 목록을 조회, 저장합니다. 중복값은 제외됩니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Void>> request(
            @RequestParam String query
    ){
        lawService.requestData(query);
        ApiResponse<Void> response =
                ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "법령 상세조회 저장 ", description = "해당 법령을 상세조회 및 저장합니다. 중복값은 제외됩니다.")
    @GetMapping("/detail")
    public ResponseEntity<ApiResponse<Void>> getDetail(
            @RequestParam(required = false) String lawName
    ){
        lawService.saveLawDetail(lawName);
        ApiResponse<Void> response =
                ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
