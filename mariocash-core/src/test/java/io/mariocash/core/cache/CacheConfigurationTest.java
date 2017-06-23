package dev.zhihexireng.core.cache;


import dev.zhihexireng.core.blockchain.Transaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CacheConfigurationTest {

    @Autowired
    ApplicationContext applicationContext;

    private static final Logger log = LoggerFactory.getLogger(CacheConfigurationTest.class);

    @Value("#{cacheManager.getCache('unconfirmTransaction')}")
    private ConcurrentMapCache uTx;

    @Autowired
    private Object cacheManager;


    @Test
    public void uTxCache() {
        assert uTx.getName() != "";

        uTx.put("TEST0", new Transaction());
        uTx.put("TEST1", new Transaction());
        uTx.put("TEST2", new Transaction());

        Transaction tx = uTx.get("TEST0", Transaction.class);

        log.debug("" + tx.getTimestamp());
        assert tx.getTimestamp() != 0L;

        ConcurrentMap eh = uTx.getNativeCache();
        Iterator<Object> list = eh.keySet().iterator();
        Object key;
        while (list.hasNext()) {
            key = list.next();
            log.debug(key + "" + uTx.get(key).toString());
        }
        assert eh.size() > 0;

    }

    @Test
    public void uTxCacheClear() {
        Cache confirm = ((CacheManager)this.cacheManager).getCache("confirmBlock");
        log.debug(confirm.getName());

        log.debug(cacheManager.getClass().getName());
        log.debug(cacheManager.toString());


    }



}
