package lawpal.lawpal.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.law.dto.request.LawDetailRequest;
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

    @Value("${api.detail}")
    private String detailUrl;


    public LawListRequest fetchLawList(String keyword, int page, int numOfRows) {
        String url = apiUrl
                + "&target=eflaw"
                + "&query=" + keyword
                + "&type=JSON"
                + "&page=" + page
                + "&display=" + numOfRows;

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            log.info("법령 API 응답 성공");

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

    public  LawDetailRequest fetchLawDetail(String lawId) {
        String url =  detailUrl
                + "&target=eflaw"
                + "&ID=" + lawId
                + "&type=JSON";

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            /**
             * JsonNode : 잭은에서 제공하는 json 트리 구조 객체
             */
            JsonNode root = objectMapper.readTree(response);
            JsonNode lawNode = root.get("Law");

            if (lawNode == null) {
                log.warn("Law node 없음. lawId = {}", lawId);
                return null;
            }

            if (lawNode.isTextual()) {
                log.warn("법령 상세 없음. lawId = {}, message = {}", lawId, lawNode.asText());
                return null;
            }

            log.info("법령 API 응답 성공");

            LawRequest lawRequest = objectMapper.readValue(response, LawRequest.class);

            return lawRequest.get법령();

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

    public LawDetailRequest fetchLawDetailByMst(String lawSerialNumber) {
        String url = detailUrl
                + "&target=eflaw"
                + "&MST=" + lawSerialNumber
                + "&type=JSON";

        try {
            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            /**
             * JsonNode : 잭은에서 제공하는 json 트리 구조 객체
             */
            JsonNode root = objectMapper.readTree(response);
            JsonNode lawNode = root.get("Law");

            if (lawNode == null) {
                log.warn("Law node 없음. lawId = {}", lawSerialNumber);
                return null;
            }

            if (lawNode.isTextual()) {
                log.warn("법령 상세 없음. lawId = {}, message = {}", lawSerialNumber, lawNode.asText());
                return null;
            }

            log.info("법령 API 응답 성공");

            LawRequest lawRequest = objectMapper.readValue(response, LawRequest.class);

            return lawRequest.get법령();

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
