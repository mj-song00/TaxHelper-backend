package lawpal.lawpal.domain.law.service;

import lawpal.lawpal.common.config.LawApiClient;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.law.dto.request.LawListRequest;
import lawpal.lawpal.domain.law.dto.request.LawSearchRequest;
import lawpal.lawpal.domain.law.dto.request.LawSummaryRequest;
import lawpal.lawpal.domain.law.entity.Law;
import lawpal.lawpal.domain.law.repository.LawRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class LawService {

    private final LawApiClient lawApiClient;
    private final LawRepository lawRepository;


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

                    Law law = Law.builder()
                            .lawSerialNumber(item.getMst())
                            .lawKey(item.getMst())
                            .lawId(item.getLawId())
                            .name(item.getLawName())
                            .lawType(item.getLawType())
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
}
