package com.ollama.springai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.data-init.enabled=false")
class OllamaDemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
