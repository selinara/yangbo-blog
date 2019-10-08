package com.chl.gbo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GboApplicationTests implements InitializingBean {

//    private static final Logger log = LogManager.getLogger(TestController.class);
//
//    @Autowired
//    private TestController testController;

    protected MockMvc mockMvc;

    @Test
    public void contextLoads() throws Exception{
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/bookmall"))
                .andExpect(MockMvcResultMatchers.status().is(200))
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(contentAsString);
//        log.info(jsonArray.toString());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        mockMvc = MockMvcBuilders.standaloneSetup(testController).build();
    }
}
