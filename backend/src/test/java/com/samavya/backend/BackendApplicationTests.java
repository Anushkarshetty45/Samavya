package com.samavya.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.flyway.enabled=false",
		"spring.datasource.password=test"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
