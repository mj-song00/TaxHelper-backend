package lawpal.lawpal.domain.law.service;

import lawpal.lawpal.common.config.LawApiClient;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.law.dto.request.*;
import lawpal.lawpal.domain.law.entity.*;
import lawpal.lawpal.domain.law.repository.*;
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


    public void requestData(String query) {
        int page = 1;
        int numOfRows = 100;

        while (true) {

            System.out.println("현재 요청 페이지 = " + page);

            LawListRequest lawRequest = lawApiClient.fetchLawList(query, page, numOfRows);

            if (lawRequest == null || lawRequest.getLawSearch() == null) {
                throw new BaseException(ExceptionEnum.API_CALL_FAILED);
            }

            LawSearchRequest lawSearch = lawRequest.getLawSearch();
            List<LawSummaryRequest> lawList = lawSearch.getLaw();

            System.out.println("API totalCnt = " + lawSearch.getTotalCount());

            if (lawList != null) {

                System.out.println("현재 페이지 데이터 개수 = " + lawList.size());

                for (LawSummaryRequest item : lawList) {

                    if (lawRepository.existsByLawKey(item.getMst())) {

                        System.out.println("중복 스킵 lawKey = " + item.getMst()
                                + ", lawId = " + item.getLawId()
                                + ", name = " + item.getLawName());

                        continue;
                    }

                    LawType lawType = lawTypeRepository.findByTypeName(item.getLawType())
                            .orElseGet(() -> lawTypeRepository.save(
                                    LawType.builder()
                                            .typeName(item.getLawType())
                                            .build()
                            ));

                    Law law = Law.builder()
                            .lawSerialNumber(item.getMst())
                            .lawKey(item.getMst())
                            .lawId(item.getLawId())
                            .name(item.getLawName())
                            .lawType(lawType)
                            .proclamationNumber(item.getProclamationNumber())
                            .detailLink(item.getDetailLink())
                            .build();

                    lawRepository.save(law);
                }
            }

            int totalCnt = Integer.parseInt(lawSearch.getTotalCount());

            if (page * numOfRows >= totalCnt) {
                break;
            }

            page++;
        }
    }

    public void saveLawDetail() {
        List<Law> laws = lawRepository.findAll();

        for (Law law : laws) {
            LawDetailRequest detail = lawApiClient.fetchLawDetail(law.getLawId());

            if (detail == null || detail.get기본정보() == null) {
                continue;
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
