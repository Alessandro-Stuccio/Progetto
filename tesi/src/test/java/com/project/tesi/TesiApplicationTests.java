package com.project.tesi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Richiede PostgreSQL e RabbitMQ in esecuzione (Docker dev profile)")
class TesiApplicationTests {

	@Test
	void contextLoads() {
	}

}
