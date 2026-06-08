package lawpal.lawpal.domain.cases.service;

import lawpal.lawpal.common.config.LawApiClient;
import lawpal.lawpal.common.exception.BaseException;
import lawpal.lawpal.common.exception.ExceptionEnum;
import lawpal.lawpal.domain.cases.dto.request.PreSearchRequest;
import lawpal.lawpal.domain.cases.dto.request.PrecSummaryRequest;
import lawpal.lawpal.domain.cases.dto.request.PrecedentListRequest;
import lawpal.lawpal.domain.cases.entity.Case;
import lawpal.lawpal.domain.cases.repository.CaseRepository;
import lawpal.lawpal.domain.precedent.entity.Precedent;
import lawpal.lawpal.domain.precedent.repository.PrecedentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                        .dataSourceName(item.getDataSourceName())
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
}


