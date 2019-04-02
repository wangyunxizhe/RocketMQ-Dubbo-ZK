package com.yuan.store.service.provider;

import com.yuan.store.mapper.StoreMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StoreServiceProviderTest {

    @Autowired
    private StoreMapper storeMapper;

    @Test
    public void selectVersion() throws Exception {
        System.out.println(storeMapper.selectVersion("001", "001"));
    }

}