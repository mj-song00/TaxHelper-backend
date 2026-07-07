package lawpal.lawpal.domain.cases.service;

import lawpal.lawpal.common.config.LawApiClient;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.cases.dto.request.*;
import lawpal.lawpal.domain.cases.entity.Case;
import lawpal.lawpal.domain.cases.repository.CaseRepository;
import lawpal.lawpal.domain.precedent.entity.Precedent;
import lawpal.lawpal.domain.precedent.enums.PrecedentStatus;
import lawpal.lawpal.domain.precedent.repository.PrecedentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CaseService {

    private final CaseRepository caseRepository;
    private final PrecedentRepository precedentRepository;
    private final LawApiClient lawApiClient;

    public void requestData(String query) {

        int page = 1;
        int totalItemCount = 0;
        int nonSupremeCourtSkipCount = 0;
        int duplicateSkipCount = 0;
        int savedCount = 0;

        while (true) {

            PrecedentListRequest precList =
                    lawApiClient.fetchPreList(query, page);

            if (precList == null || precList.getPreSearch() == null) {
                throw new BaseException(ExceptionEnum.API_CALL_FAILED);
            }

            PreSearchRequest precSearch = precList.getPreSearch();
            List<PrecSummaryRequest> list = precSearch.getPrec();

            if (list == null || list.isEmpty()) {
                break;
            }

            for (PrecSummaryRequest item : list) {

                totalItemCount++;

                if (!"대법원".equals(item.getDataSourceName())) {
                    nonSupremeCourtSkipCount++;
                    continue;
                }

                if (precedentRepository.existsByPrecedentSerialNumber(item.getPrecedentSerialNumber())) {
                    duplicateSkipCount++;

                    log.info("중복 스킵 precedentSerialNumber = {}, caseNumber = {}, caseName = {}",
                            item.getPrecedentSerialNumber(),
                            item.getCaseNumber(),
                            item.getCaseName());

                    continue;
                }

                Case caseEntity = Case.builder()
                        .caseNumber(item.getCaseNumber())
                        .caseCode(item.getCaseCode())
                        .caseTypeName(item.getCaseTypeName())
                        .sentence(item.getSentence())
                        .sentencingDate(item.getSentencingDate())
                        .courtTypeCode(item.getCourtTypeCode())
                        .courtName(item.getCourtName())
                        .caseName(item.getCaseName())
                        .build();

                try {
                    caseRepository.save(caseEntity);
                } catch (Exception e) {
                    log.error("caseName={}", item.getCaseName());
                    throw e;
                }

                Precedent precedent = Precedent.builder()
                        .precedentSerialNumber(item.getPrecedentSerialNumber())
                        .judgmentType(item.getJudgmentType())
                        .caseType(item.getCaseTypeName())
                        .cases(caseEntity)
                        .build();

                precedentRepository.save(precedent);

                savedCount++;
            }

            page++;
        }

        log.info("판례 목록 저장 완료");
        log.info("전체 처리 항목 수 = {}", totalItemCount);
        log.info("대법원 아님 스킵 수 = {}", nonSupremeCourtSkipCount);
        log.info("중복 스킵 수 = {}", duplicateSkipCount);
        log.info("저장 개수 = {}", savedCount);
    }

    public void savePreDetail() {
        List<Precedent> precedents = precedentRepository.findAll();

        int successCount = 0;
        int notFoundCount = 0;
        int failCount = 0;

        for (Precedent item : precedents) {

            log.info("판례 저장 진행 {}/{} id = {}, precedentSerialNumber = {}",
                    item.getId(),
                    precedents.size(),
                    item.getId(),
                    item.getPrecedentSerialNumber());

            try {
                String fullText = null;
                String issue = null;
                String referenceArticles = null;
                String referenceCases = null;
                String judgmentSummary = null;

                /*
                 * 1. 기존 JSON 상세 조회
                 */
                PrecedentRequest detail =
                        lawApiClient.fetchPrecDetail(item.getPrecedentSerialNumber());

                if (detail != null && detail.getPrecService() != null) {
                    PrecedentDetailRequest precService = detail.getPrecService();

                    fullText = precService.get판례내용();
                    issue = precService.get판시사항();
                    referenceArticles = precService.get참조조문();
                    referenceCases = precService.get참조판례();
                    judgmentSummary = precService.get판결요지();

                    log.info("JSON 판례 상세 조회 성공 id = {}, precedentSerialNumber = {}",
                            item.getId(),
                            item.getPrecedentSerialNumber());
                }

                /*
                 * JSON 응답이 없거나 판례 본문이 없다면
                 * 국세법령정보시스템 HTML 상세 조회
                 */
                if (fullText == null || fullText.isBlank()) {
                    String detailUrl =
                            "https://www.law.go.kr/LSW/precInfoP.do"
                                    + "?precSeq="
                                    + item.getPrecedentSerialNumber()
                                    + "&mode=0";

                    log.info("HTML 판례 상세 조회 시작 id = {}, url = {}",
                            item.getId(),
                            detailUrl);

                    Document document = Jsoup.connect(detailUrl)
                            .userAgent(
                                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                                            + "AppleWebKit/537.36 "
                                            + "(KHTML, like Gecko) "
                                            + "Chrome/149.0.0.0 Safari/537.36"
                            )
                            .referrer("https://www.law.go.kr/")
                            .timeout(150_000)
                            .get();

                    /*
                     * 불필요한 화면 요소 제거
                     */
                    document.select(
                            "script, style, iframe, nav, header, footer, "
                                    + "button, input, form, noscript"
                    ).remove();

                    /*
                     * 테이블 형태 데이터 파싱
                     *
                     * 예:
                     * <tr>
                     *     <th>판시사항</th>
                     *     <td>내용</td>
                     * </tr>
                     */
                    for (Element row : document.select("tr")) {
                        Elements children = row.children();

                        if (children.size() < 2) {
                            continue;
                        }

                        String label = children.get(0)
                                .text()
                                .replaceAll("\\s+", "")
                                .replace("：", ":")
                                .replace(":", "");

                        String value = children.get(1).text().trim();

                        if (value.isBlank()) {
                            continue;
                        }

                        if ("판시사항".equals(label)) {
                            issue = value;
                        } else if ("판결요지".equals(label)) {
                            judgmentSummary = value;
                        } else if ("참조조문".equals(label)) {
                            referenceArticles = value;
                        } else if ("참조판례".equals(label)) {
                            referenceCases = value;
                        } else if ("판례내용".equals(label)
                                || "판결내용".equals(label)
                                || "전문".equals(label)
                                || "판결문".equals(label)) {
                            fullText = value;
                        }
                    }

                    /*
                     * dt/dd 형태 데이터 파싱
                     *
                     * 예:
                     * <dt>판시사항</dt>
                     * <dd>내용</dd>
                     */
                    for (Element dt : document.select("dt")) {
                        Element dd = dt.nextElementSibling();

                        if (dd == null || !"dd".equalsIgnoreCase(dd.tagName())) {
                            continue;
                        }

                        String label = dt.text()
                                .replaceAll("\\s+", "")
                                .replace("：", ":")
                                .replace(":", "");

                        String value = dd.text().trim();

                        if (value.isBlank()) {
                            continue;
                        }

                        if ("판시사항".equals(label)) {
                            issue = value;
                        } else if ("판결요지".equals(label)) {
                            judgmentSummary = value;
                        } else if ("참조조문".equals(label)) {
                            referenceArticles = value;
                        } else if ("참조판례".equals(label)) {
                            referenceCases = value;
                        } else if ("판례내용".equals(label)
                                || "판결내용".equals(label)
                                || "전문".equals(label)
                                || "판결문".equals(label)) {
                            fullText = value;
                        }
                    }

                    /*
                     * 표 구조에서 판례 전문을 찾지 못한 경우
                     * 본문 영역 후보를 확인
                     */
                    if (fullText == null || fullText.isBlank()) {
                        Elements contentElements = document.select(
                                "#contentBody, "
                                        + "#content, "
                                        + "#bodyContent, "
                                        + ".contentBody, "
                                        + ".content, "
                                        + ".conBox, "
                                        + ".lawcon, "
                                        + ".lawContent, "
                                        + ".precContent"
                        );

                        String contentText = contentElements.text().trim();

                        if (!contentText.isBlank()) {
                            fullText = contentText;
                        }
                    }

                    /*
                     * 사이트의 HTML 구조를 아직 정확히 특정하지 못한 경우를 위한
                     * 마지막 fallback
                     */
                    if (fullText == null || fullText.isBlank()) {
                        String bodyText = document.body() == null
                                ? null
                                : document.body().text().trim();

                        if (bodyText != null
                                && !bodyText.isBlank()
                                && !bodyText.contains("요청하신 판례가 존재하지 않습니다")) {
                            fullText = bodyText;
                        }
                    }

                    log.info("HTML 판례 상세 조회 완료 id = {}, precedentSerialNumber = {}",
                            item.getId(),
                            item.getPrecedentSerialNumber());
                }

                /*
                 * JSON과 HTML 모두 본문을 가져오지 못한 경우
                 */
                if (fullText == null || fullText.isBlank()) {
                    notFoundCount++;
                    item.updateStatus(PrecedentStatus.DETAIL_FAILED);

                    log.warn("판례 상세 없음 id = {}, precedentSerialNumber = {}",
                            item.getId(),
                            item.getPrecedentSerialNumber());

                    continue;
                }

                log.info("판례내용 length = {}", fullText.length());
                log.info("판시사항 length = {}",
                        issue == null ? 0 : issue.length());
                log.info("참조조문 length = {}",
                        referenceArticles == null ? 0 : referenceArticles.length());
                log.info("참조판례 length = {}",
                        referenceCases == null ? 0 : referenceCases.length());
                log.info("판결요지 length = {}",
                        judgmentSummary == null ? 0 : judgmentSummary.length());

                item.updateDetail(
                        fullText,
                        issue,
                        referenceArticles,
                        referenceCases,
                        judgmentSummary
                );

                item.updateStatus(PrecedentStatus.DETAIL_SAVED);
                successCount++;

            } catch (Exception e) {
                failCount++;
                item.updateStatus(PrecedentStatus.DETAIL_FAILED);

                log.error(
                        "판례 상세 저장 실패 후 스킵 id = {}, precedentSerialNumber = {}",
                        item.getId(),
                        item.getPrecedentSerialNumber(),
                        e
                );
            }
        }

        log.info(
                "판례 상세 저장 완료 total = {}, success = {}, notFound = {}, fail = {}",
                precedents.size(),
                successCount,
                notFoundCount,
                failCount
        );
    }
}


