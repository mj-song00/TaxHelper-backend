package lawpal.lawpal.domain.law.service;

import lawpal.lawpal.common.config.LawApiClient;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.law.dto.request.*;
import lawpal.lawpal.domain.law.entity.*;
import lawpal.lawpal.domain.law.repository.*;
import lawpal.lawpal.domain.ministry.entity.Ministry;
import lawpal.lawpal.domain.ministry.repository.MinistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LawService {

    private final LawApiClient lawApiClient;
    private final LawRepository lawRepository;

    private final LawArticleRepository lawArticleRepository;
    private final LawParagraphRepository lawParagraphRepository;
    private final LawSubparagraphRepository lawSubparagraphRepository;
    private final LawSupplementRepository lawSupplementRepository;
    private final LawTypeRepository lawTypeRepository;

    private final MinistryRepository ministryRepository;

    public void requestData(String query) {

        int page = 1;
        int numOfRows = 100;

        int totalItemCount = 0;
        int currentCount = 0;
        int nonCurrentSkipCount = 0;
        int duplicateSkipCount = 0;
        int savedCount = 0;

        while (true) {

            LawListRequest lawRequest =
                    lawApiClient.fetchLawList(query, page, numOfRows);

            if (lawRequest == null || lawRequest.getLawSearch() == null) {
                throw new BaseException(ExceptionEnum.API_CALL_FAILED);
            }

            LawSearchRequest lawSearch = lawRequest.getLawSearch();
            List<LawSummaryRequest> lawList = lawSearch.getLaw();

            if (lawList == null || lawList.isEmpty()) {
                break;
            }

            System.out.println("API totalCnt = " + lawSearch.getTotalCount());
            System.out.println("현재 페이지 데이터 개수 = " + lawList.size());

            for (LawSummaryRequest item : lawList) {

                totalItemCount++;

                if (!"현행".equals(item.getHistoryCode())) {
                    nonCurrentSkipCount++;

                    continue;
                }

                currentCount++;

                if (lawRepository.existsByLawSerialNumber(item.getMst())) {
                    duplicateSkipCount++;

                    continue;
                }

                LawType lawType = lawTypeRepository
                        .findByTypeName(item.getLawType())
                        .orElseGet(() -> lawTypeRepository.save(
                                LawType.builder()
                                        .typeName(item.getLawType())
                                        .build()
                        ));

                Ministry ministry = ministryRepository
                        .findByMinistryCode(item.getMinistryCode())
                        .orElseGet(() -> ministryRepository.save(
                                Ministry.builder()
                                        .ministryName(item.getMinistryName())
                                        .ministryCode(item.getMinistryCode())
                                        .build()
                        ));

                Law law = Law.builder()
                        .lawSerialNumber(item.getMst())
                        .lawId(item.getLawId())
                        .lawKey(item.getMst())
                        .name(item.getLawName())
                        .lawType(lawType)
                        .ministry(ministry)
                        .proclamationNumber(item.getProclamationNumber())
                        .proclamationDate(item.getProclamationDate())
                        .effectiveDate(item.getEffectiveDate())
                        .detailLink(item.getDetailLink())
                        .historyCode(item.getHistoryCode())
                        .revisionType(item.getRevisionType())
                        .build();

                lawRepository.save(law);
                savedCount++;

            }

            int totalCnt = Integer.parseInt(lawSearch.getTotalCount());

            if (page * numOfRows >= totalCnt) {
                break;
            }

            page++;
        }

        System.out.println("목록 저장 완료");
        System.out.println("전체 처리 항목 수 = " + totalItemCount);
        System.out.println("현행 항목 수 = " + currentCount);
        System.out.println("현행 아님 스킵 수 = " + nonCurrentSkipCount);
        System.out.println("중복 스킵 수 = " + duplicateSkipCount);
        System.out.println("저장 개수 = " + savedCount);
    }

    public void saveLawDetail() {
        List<Law> laws = lawRepository.findAll();


        int index = 0;
        for (Law law : laws) {
            index++;
            log.info("본문 저장 진행 {}/{} lawId = {}, name = {}",
                    index,
                    laws.size(),
                    law.getLawId(),
                    law.getName());

            LawDetailRequest detail = lawApiClient.fetchLawDetail(law.getLawId());


            if (detail == null || detail.get기본정보() == null) {
                LawDetailRequest mstDetail = lawApiClient.fetchLawDetailByMst(law.getLawSerialNumber());

                if (mstDetail == null || mstDetail.get기본정보() == null) {
                    log.warn("본문 저장 최종 스킵 lawId = {}, mst = {}, name = {}",
                            law.getLawId(),
                            law.getLawSerialNumber(),
                            law.getName());
                    continue;
                }

                detail = mstDetail;
            }

            if (detail.get조문() != null && detail.get조문().getUnits()!= null) {
                for (ArticleUnitRequest articleRequest : detail.get조문().getUnits()) {

                    LawArticle article = LawArticle.builder()
                            .law(law)
                            .articleNumber(articleRequest.getArticleNo())
                            .articleKey(articleRequest.getArticleNo())
                            .articleTitle(articleRequest.getTitle())
                            . articleContent(articleRequest.getContent())
                            .build();

                    lawArticleRepository.save(article);

                    if (articleRequest.getParagraph() != null) {
                        for (ParagraphRequest paragraphRequest : articleRequest.getParagraph()) {

                            LawParagraph paragraph = LawParagraph.builder()
                                    .lawArticle(article)
                                    .paragraphNumber(paragraphRequest.getParagraphNo())
                                    .content(paragraphRequest.getContent())
                                    .build();

                            lawParagraphRepository.save(paragraph);

                            if (paragraphRequest.getItem() != null) {
                                for (SubParagraphRequest subparagraphRequest : paragraphRequest.getItem()) {

                                    LawSubparagraph subparagraph = LawSubparagraph.builder()
                                            .lawParagraph(paragraph)
                                            .subparagraphNumber(subparagraphRequest.getItemNo())
                                            .content(subparagraphRequest.getContent())
                                            .build();

                                    lawSubparagraphRepository.save(subparagraph);
                                }
                            }
                        }
                    }
                }
            }

            if (detail.get부칙() != null && detail.get부칙().getUnits() != null) {
                for (SupplementUnitRequest supplementRequest : detail.get부칙().getUnits()) {

                    String content = supplementRequest.getContent() != null
                            ? supplementRequest.getContent().stream()
                            .flatMap(List::stream)
                            .collect(Collectors.joining("\n"))
                            : null;

                    LawSupplement supplement = LawSupplement.builder()
                            .law(law)
                            .supplementKey(supplementRequest.getKey())
                            .proclamationDate(supplementRequest.getProclamationDate())
                            .proclamationNumber(supplementRequest.getProclamationNo())
                            .content(content)
                            .build();

                    lawSupplementRepository.save(supplement);
                }
            }
        }
    }
}
