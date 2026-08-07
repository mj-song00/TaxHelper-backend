package lawpal.lawpal.domain.chunk.service;

import lawpal.lawpal.domain.cases.entity.Case;
import lawpal.lawpal.domain.chunk.dto.response.ChunkResponse;
import lawpal.lawpal.domain.chunk.dto.response.PrecResponse;
import lawpal.lawpal.domain.chunk.entity.Chunk;
import lawpal.lawpal.domain.chunk.entity.PrecChunk;
import lawpal.lawpal.domain.chunk.enums.LawChunkType;
import lawpal.lawpal.domain.chunk.enums.PrecChunkType;
import lawpal.lawpal.domain.chunk.repository.ChunkRepository;
import lawpal.lawpal.domain.chunk.repository.PrecChunkRepository;
import lawpal.lawpal.domain.law.entity.Law;
import lawpal.lawpal.domain.law.repository.LawAmendmentRepository;
import lawpal.lawpal.domain.law.repository.LawArticleRepository;
import lawpal.lawpal.domain.law.repository.LawSupplementRepository;
import lawpal.lawpal.domain.precedent.entity.Precedent;
import lawpal.lawpal.domain.precedent.repository.PrecedentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChunkService 키워드 검색")
class ChunkServiceTest {

    @Mock LawArticleRepository lawArticleRepository;
    @Mock LawSupplementRepository lawSupplementRepository;
    @Mock LawAmendmentRepository lawAmendmentRepository;
    @Mock ChunkRepository chunkRepository;
    @Mock PrecedentRepository precedentRepository;
    @Mock PrecChunkRepository precChunkRepository;
    @Mock OpenAiEmbeddingService openAiEmbeddingService;

    @InjectMocks ChunkService chunkService;

    @Nested
    @DisplayName("법령 청크 키워드 검색")
    class LawChunkKeywordSearch {

        @Test
        @DisplayName("검색어가 없으면 Repository 페이지 결과를 그대로 반환한다")
        void returnsRepositoryPageWhenSearchTermsAreEmpty() {
            PageRequest pageable = PageRequest.of(1, 2);
            Chunk chunk = lawChunk("부가가치세법", LawChunkType.ARTICLE, "제10조", "재화의 공급");
            when(chunkRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(new PageImpl<>(List.of(chunk), pageable, 3));

            ChunkResponse response = chunkService.getChunks(pageable, null, "  ", null);

            assertEquals(2, response.getCurrentPage());
            assertEquals(2, response.getTotalPages());
            assertEquals(3, response.getTotalElements());
            assertEquals("제10조", response.getList().get(0).getTitle());
            verify(chunkRepository, never()).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("검색어가 있으면 관련도 점수 순으로 정렬한다")
        void ranksMatchingArticleBeforeWeakMatch() {
            Chunk direct = lawChunk(
                    "소득세법", LawChunkType.ARTICLE, "제52조 특별소득공제",
                    "장기주택저당차입금 이자상환액의 공제한도와 상환기간"
            );
            Chunk weak = lawChunk(
                    "소득세법", LawChunkType.ARTICLE, "제1조 목적",
                    "소득세의 일반적인 목적"
            );
            when(chunkRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(weak, direct));

            ChunkResponse response = chunkService.getChunks(
                    PageRequest.of(0, 10),
                    List.of("장기주택저당차입금", "공제한도"),
                    "이자상환액 한도",
                    List.of("소득세법")
            );

            assertEquals(1, response.getTotalElements());
            assertEquals("제52조 특별소득공제", response.getList().get(0).getTitle());
            assertTrue(response.getList().get(0).getScore() > 0);
        }

        @Test
        @DisplayName("삭제 조문은 검색 후보에 있어도 결과에서 제외한다")
        void filtersDeletedArticle() {
            Chunk deleted = lawChunk("소득세법", LawChunkType.ARTICLE, "제52조 삭제", "삭제");
            when(chunkRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(deleted));

            ChunkResponse response = chunkService.getChunks(
                    PageRequest.of(0, 10), List.of("제52조"), "제52조", List.of("소득세법")
            );

            assertEquals(0, response.getTotalElements());
            assertTrue(response.getList().isEmpty());
        }

        @Test
        @DisplayName("대손충당금 검색에서 무관한 외국납부세액 조문을 제외한다")
        void filtersUnrelatedForeignTaxCreditForBadDebtSearch() {
            Chunk relevant = lawChunk(
                    "법인세법", LawChunkType.ARTICLE, "제34조 대손충당금",
                    "채무보증으로 발생한 구상채권과 업무무관 가지급금은 설정대상채권에서 제외한다"
            );
            Chunk unrelated = lawChunk(
                    "법인세법", LawChunkType.ARTICLE, "제57조 외국납부세액",
                    "국외원천소득에 대한 외국법인세액 공제"
            );
            when(chunkRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(unrelated, relevant));

            ChunkResponse response = chunkService.getChunks(
                    PageRequest.of(0, 10),
                    List.of("대손충당금", "구상채권", "제외"),
                    "대손충당금 설정대상채권 제외",
                    List.of("법인세법")
            );

            assertEquals(1, response.getTotalElements());
            assertEquals("제34조 대손충당금", response.getList().get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("판례 청크 키워드 검색")
    class PrecedentChunkKeywordSearch {

        @Test
        @DisplayName("검색 조건이 없으면 Repository 페이지 결과를 그대로 반환한다")
        void returnsRepositoryPageWhenNoConditionsExist() {
            PageRequest pageable = PageRequest.of(0, 10);
            PrecChunk chunk = precedentChunk(
                    PrecChunkType.SUMMARY, "판결요지", "실질과세 원칙을 적용한다",
                    "2020두12345", "부과처분취소", "대법원"
            );
            when(precChunkRepository.findAll(pageable))
                    .thenReturn(new PageImpl<>(List.of(chunk), pageable, 1));

            PrecResponse response = chunkService.getPrecs(pageable, null, null, null, null);

            assertEquals(1, response.getCurrentPage());
            assertEquals(1, response.getTotalElements());
            assertEquals("2020두12345", response.getList().get(0).getCaseNumber());
            verify(precChunkRepository, never()).findAll(any(Specification.class));
        }

        @Test
        @DisplayName("사건번호와 법원명 일치 판례에 가중치를 부여한다")
        void ranksCaseNumberAndCourtMatches() {
            PrecChunk exact = precedentChunk(
                    PrecChunkType.ISSUE, "판시사항", "명의자와 실제 귀속자가 다른 경우",
                    "2020두12345", "부과처분취소", "대법원"
            );
            PrecChunk other = precedentChunk(
                    PrecChunkType.FULL_TEXT, "판례내용", "명의자에 관한 다른 사건",
                    "2019구합100", "부과처분취소", "서울행정법원"
            );
            when(precChunkRepository.findAll(any(Specification.class)))
                    .thenReturn(List.of(other, exact));

            PrecResponse response = chunkService.getPrecs(
                    PageRequest.of(0, 10),
                    List.of("명의자"),
                    "실질 귀속자",
                    List.of("대법원"),
                    List.of("2020두12345")
            );

            assertEquals(2, response.getTotalElements());
            assertEquals("2020두12345", response.getList().get(0).getCaseNumber());
            assertTrue(response.getList().get(0).getScore() > response.getList().get(1).getScore());
        }
    }

    private static Chunk lawChunk(String lawName, LawChunkType type, String title, String content) {
        Law law = Law.builder()
                .lawSerialNumber("serial-" + title)
                .lawKey("key-" + title)
                .name(lawName)
                .build();
        return Chunk.builder()
                .id((long) title.hashCode())
                .law(law)
                .chunkType(type)
                .sourceId(1L)
                .title(title)
                .content(content)
                .build();
    }

    private static PrecChunk precedentChunk(
            PrecChunkType type,
            String title,
            String content,
            String caseNumber,
            String caseName,
            String courtName
    ) {
        Case cases = Case.builder()
                .caseNumber(caseNumber)
                .caseCode("400107")
                .caseTypeName("세무")
                .sentencingDate("2024.01.01")
                .courtName(courtName)
                .caseName(caseName)
                .build();
        Precedent precedent = Precedent.builder()
                .id((long) caseNumber.hashCode())
                .precedentSerialNumber("serial-" + caseNumber)
                .cases(cases)
                .build();
        return PrecChunk.builder()
                .id((long) content.hashCode())
                .chunkType(type)
                .title(title)
                .content(content)
                .precedent(precedent)
                .build();
    }
}
