package lawpal.lawpal.domain.cases.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lawpal.lawpal.common.response.ApiResponse;
import lawpal.lawpal.common.response.ApiResponseEnum;
import lawpal.lawpal.domain.cases.service.CaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "판례목록 요청 ", description = "국가법령정보 공동활용 OPEN API에서 판례목록을 요청합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/precedent")
public class CaseController {

    public final CaseService caseService;

    @Operation(summary = "판례 목록 데이터 요청", description = "판례 목록을 조회, 저장합니다. 데이터 출처가 대법원인 데이터만 저장됩니다.")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Void>> request(
            @RequestParam String query
    ){
        caseService.requestData(query);

        ApiResponse<Void> response =
                ApiResponse.successWithOutData(ApiResponseEnum.DATA_SAVED_COMPLETED);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
