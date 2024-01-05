package org.kpi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(profiles = {"test"})
@SpringBootTest(classes = SpringConsumerApp.class)
class SpringConsumerAppTest {

    @Test
    void contextLoads() {

    }
}