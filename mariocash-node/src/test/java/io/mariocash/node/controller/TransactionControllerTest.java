/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.zhihexireng.node.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.zhihexireng.node.TestConfig;
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
public class TransactionControllerTest {
    private static final Logger log = LoggerFactory.getLogger(TransactionControllerTest.class);

    @Autowired
    private MockMvc mockMvc;

    private JacksonTester<TransactionDto> json;
    private static final String FROM = "fb6b782a7f40de97e50181ee31cba6ed352e2a4e";

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    public void shouldGetTransactionByHash() throws Exception {

        // 트랜잭션 풀에 있는 트랜잭션을 조회 후 블록 내 트랜잭션 조회 로직 추가 필요.
        TransactionDto req = new TransactionDto();
        req.setFrom(FROM);
        req.setData("transaction data");

        MockHttpServletResponse postResponse = mockMvc.perform(post("/txs")
                .contentType(MediaType.APPLICATION_JSON).content(json.write(req).getJson()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        String postTxHash = json.parseObject(postResponse.getContentAsString()).getTxHash();

        MockHttpServletResponse getResponse = mockMvc.perform(get("/txs/" + postTxHash))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        assertThat(postResponse.getContentAsString()).isEqualTo(getResponse.getContentAsString());
    }

    @Test
    public void shouldAddTransactionAtTransactionPool() throws Exception {
        TransactionDto req = new TransactionDto();
        req.setFrom(FROM);
        req.setData("Dezang");

        MockHttpServletResponse response = mockMvc.perform(post("/txs")
                .contentType(MediaType.APPLICATION_JSON).content(json.write(req).getJson()))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).contains("Dezang");
    }
}
