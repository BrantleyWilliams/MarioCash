package dev.zhihexireng.node.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(JsonRpcConfig.class)
public class AccountApiImplTest {
    private static final Logger log = LoggerFactory.getLogger(TransactionApi.class);

    @Test
    public void accountApiImplTest() throws Exception {
        AccountApiImpl accapi = new AccountApiImpl();
        String account = accapi.createAccount();
        assertThat(account).isNotEmpty();
    }
}
