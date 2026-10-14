package lawpal.lawpal.domain.law.dto.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LawStructureDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @ValueSource(strings = {"\"공동부령\"", "\"\"", "null",
            "{}", "{\"공동부령단위\":{\"공동부령명\":\"부처명\",\"공동부령번호\":\"123\"}}",
            "[{\"부처\":\"가\"},{\"부처\":\"나\"}]"})
    void preservesJointMinistryTextAndStructuredContent(String content) throws Exception {
        String json = "{\"법령\":{\"기본정보\":{\"공동부령정보\":" + content + "}}}";
        BasicInfo info = objectMapper.readValue(json, LawRequest.class).get법령().get기본정보();
        var source = objectMapper.readTree(content);
        if (source.isNull()) {
            org.junit.jupiter.api.Assertions.assertNull(info.getJoinMinistry());
        } else if (source.isTextual()) {
            assertEquals(source.textValue(), info.getJoinMinistry());
        } else {
            assertEquals(source, objectMapper.readTree(info.getJoinMinistry()));
        }
    }

    @Test
    void acceptsMissingJointMinistry() throws Exception {
        BasicInfo info = objectMapper.readValue("{}", BasicInfo.class);
        org.junit.jupiter.api.Assertions.assertNull(info.getJoinMinistry());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"본문\"", "[\"본문\"]", "[[\"본문\"]]"})
    void acceptsLawContentShapesThroughFullResponse(String content) throws Exception {
        String json = "{\"법령\":{\"개정문\":{\"개정문내용\":" + content
                + "},\"부칙\":{\"부칙단위\":{\"부칙내용\":" + content + "}}}}";
        LawDetailRequest detail = objectMapper.readValue(json, LawRequest.class).get법령();
        assertEquals(List.of(List.of("본문")), detail.get개정문().getContents());
        assertEquals(List.of(List.of("본문")), detail.get부칙().getUnits().get(0).getContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"[\"첫 줄\",\"둘째 줄\",\"셋째 줄\"]",
            "[[\"첫 줄\",\"둘째 줄\"],[\"셋째 줄\"]]"})
    void preservesAllContentLinesInStorageOrder(String content) throws Exception {
        AmendmentsRequest request = objectMapper.readValue(
                "{\"개정문내용\":" + content + "}", AmendmentsRequest.class);
        assertEquals(List.of("첫 줄", "둘째 줄", "셋째 줄"),
                request.getContents().stream().flatMap(List::stream).toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"개정문내용\":null}", "{\"개정문내용\":[]}"})
    void acceptsAbsentOrEmptyAmendmentContent(String json) throws Exception {
        AmendmentsRequest request = objectMapper.readValue(json, AmendmentsRequest.class);
        org.junit.jupiter.api.Assertions.assertTrue(
                request.getContents() == null || request.getContents().isEmpty());
    }

    @Test
    void acceptsSingleObjectForSubparagraphAndItem() throws Exception {
        String json = """
                {
                  "항번호": "1",
                  "호": {
                    "호번호": "1",
                    "호내용": "호 내용",
                    "목": {
                      "목번호": "가",
                      "목내용": "목 내용"
                    }
                  }
                }
                """;

        ParagraphRequest paragraph = objectMapper.readValue(json, ParagraphRequest.class);

        assertEquals(1, paragraph.getItem().size());
        assertEquals(1, paragraph.getItem().get(0).getSubItem().size());
        assertEquals("가", paragraph.getItem().get(0).getSubItem().get(0).getSubNo());
        assertEquals(
                "목 내용",
                paragraph.getItem().get(0).getSubItem().get(0).getSubContent().get(0)
        );
    }

    @Test
    void stillAcceptsArraysForSubparagraphAndItem() throws Exception {
        String json = """
                {
                  "항번호": "1",
                  "호": [{
                    "호번호": "1",
                    "목": [{
                      "목번호": "가",
                      "목내용": ["첫 줄", "둘째 줄"]
                    }]
                  }]
                }
                """;

        ParagraphRequest paragraph = objectMapper.readValue(json, ParagraphRequest.class);

        assertEquals(1, paragraph.getItem().size());
        assertEquals(
                2,
                paragraph.getItem().get(0).getSubItem().get(0).getSubContent().size()
        );
    }
}
