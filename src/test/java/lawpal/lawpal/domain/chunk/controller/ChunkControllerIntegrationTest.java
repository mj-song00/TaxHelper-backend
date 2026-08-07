package lawpal.lawpal.domain.chunk.controller;

import lawpal.lawpal.domain.chunk.dto.response.ChunkResponse;
import lawpal.lawpal.domain.chunk.dto.response.PrecResponse;
import lawpal.lawpal.domain.chunk.service.ChunkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("FastAPI-Spring 청크 검색 HTTP 계약")
class ChunkControllerIntegrationTest {

    private ChunkService chunkService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        chunkService = mock(ChunkService.class);
        mockMvc = standaloneSetup(new ChunkController(chunkService)).build();
    }

    @Test
    @DisplayName("법령 검색 쿼리를 서비스 인자로 변환하고 Spring 응답 형태로 직렬화한다")
    void lawChunkContract() throws Exception {
        ChunkResponse serviceResponse = ChunkResponse.builder()
                .list(List.of())
                .currentPage(1)
                .totalPages(2)
                .totalElements(11)
                .build();
        when(chunkService.getChunks(
                org.mockito.ArgumentMatchers.any(Pageable.class),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/v1/chunks/law-chunks")
                        .param("page", "1")
                        .param("size", "30")
                        .param("keywords", "장기주택저당차입금", "공제한도")
                        .param("query", "이자상환액 공제 한도")
                        .param("lawNames", "소득세법", "소득세법 시행령"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(11));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(chunkService).getChunks(
                pageable.capture(),
                org.mockito.ArgumentMatchers.eq(List.of("장기주택저당차입금", "공제한도")),
                org.mockito.ArgumentMatchers.eq("이자상환액 공제 한도"),
                org.mockito.ArgumentMatchers.eq(List.of("소득세법", "소득세법 시행령"))
        );
        assertEquals(0, pageable.getValue().getPageNumber());
        assertEquals(30, pageable.getValue().getPageSize());
    }

    @Test
    @DisplayName("판례 검색 쿼리를 서비스 인자로 변환하고 Spring 응답 형태로 직렬화한다")
    void precedentChunkContract() throws Exception {
        PrecResponse serviceResponse = PrecResponse.builder()
                .list(List.of())
                .currentPage(2)
                .totalPages(3)
                .totalElements(21)
                .build();
        when(chunkService.getPrecs(
                org.mockito.ArgumentMatchers.any(Pageable.class),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyList(),
                org.mockito.ArgumentMatchers.anyList()
        )).thenReturn(serviceResponse);

        mockMvc.perform(get("/api/v1/chunks/prec-chunks")
                        .param("page", "2")
                        .param("size", "10")
                        .param("keywords", "실질과세")
                        .param("query", "명의자 실제 귀속자 판례")
                        .param("courtNames", "대법원")
                        .param("caseNumbers", "2020두12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.currentPage").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(21));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(chunkService).getPrecs(
                pageable.capture(),
                org.mockito.ArgumentMatchers.eq(List.of("실질과세")),
                org.mockito.ArgumentMatchers.eq("명의자 실제 귀속자 판례"),
                org.mockito.ArgumentMatchers.eq(List.of("대법원")),
                org.mockito.ArgumentMatchers.eq(List.of("2020두12345"))
        );
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
    }
}
