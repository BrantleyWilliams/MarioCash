package dev.zhihexireng.node;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TransactionController.class)
@Import(TestConfig.class)
public class TransactionControllerTests {
    private static final Logger log = LoggerFactory.getLogger(TransactionControllerTests.class);

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<TransactionDto> json;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void 트랜잭션_해쉬로_조회() throws Exception {
        // 트랜잭션 풀에 있는 트랜잭션을 조회 후 블록 내 트랜잭션 조회 로직 추가 필요.
        TransactionDto req = new TransactionDto();
        req.setFrom("Dezang");
        req.setData("transaction data");

        MockHttpServletResponse response = mockMvc.perform(post("/txs")
                .contentType(MediaType.APPLICATION_JSON).content(json.write(req).getJson()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        String txHash = json.parseObject(response.getContentAsString()).getTxHash();

        MockHttpServletResponse findRes = mockMvc.perform(get("/txs/" + txHash))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        log.debug(findRes.getContentAsString());
    }

    @Test
    public void 트랜잭션이_트랜잭션풀에_추가되어야_한다() throws Exception {
        TransactionDto req = new TransactionDto();
        req.setData("Dezang");

        MockHttpServletResponse response = mockMvc.perform(post("/txs")
                .contentType(MediaType.APPLICATION_JSON).content(json.write(req).getJson()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).contains("Dezang");
    }
}
