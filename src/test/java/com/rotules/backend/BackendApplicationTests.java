package com.rotules.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.security.oauth2.client.registration.clover.client-id=testClientId",
		"spring.security.oauth2.client.registration.clover.client-secret=testClientSecret"
})class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
