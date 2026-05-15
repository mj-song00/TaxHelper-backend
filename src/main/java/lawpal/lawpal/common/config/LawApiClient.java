package lawpal.lawpal.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.law.dto.request.LawListRequest;
import lawpal.lawpal.domain.law.dto.request.LawRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class LawApiClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${api.url}")
    private String apiUrl;


    public LawListRequest fetchLawList(String query) {
        String url = apiUrl + "&query=" + query + "&type=JSON";
        log.info("법령 API 요청 URL = {}", url);
        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            log.info("법령 API 응답 성공");
            log.debug("법령 API 응답 데이터 = {}", response);

            return objectMapper.readValue(response, LawListRequest.class);

        } catch (RestClientException e) {
            log.error("법령 API 호출 실패");
            log.error("요청 URL = {}", url);
            log.error("에러 메시지 = {}", e.getMessage(), e);

            throw new BaseException(ExceptionEnum.API_CALL_FAILED);
        } catch (Exception e) {

            log.error("법령 API JSON 매핑 실패");
            log.error("응답 매핑 중 예외 발생", e);

            throw new BaseException(ExceptionEnum.API_MATCHING_FAILED);
        }
    }
}
