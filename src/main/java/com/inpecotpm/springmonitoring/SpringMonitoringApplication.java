package com.inpecotpm.springmonitoring;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.event.Level;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@SpringBootApplication
@RestController
@Slf4j
public class SpringMonitoringApplication {

	private final RestTemplate restTemplate;

	public SpringMonitoringApplication(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@GetMapping("/")
	public ResponseEntity get(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String level) {

		HttpStatus httpStatus = HttpStatus.OK;
		if(StringUtils.hasText(status)) {
			httpStatus = HttpStatus.resolve(Integer.valueOf(status.trim()));
		}

		Level logLevel = Level.DEBUG;
		if(StringUtils.hasText(level)) {
			logLevel = Level.valueOf(level.trim().toUpperCase());
		}

		switch (logLevel) {
			case ERROR:
				RuntimeException ex = new RuntimeException("This is an error");
				log.error("This is an error message", ex);
				throw ex;
			case WARN:
				log.warn("This is a warning message");
				break;
			case INFO:
				log.info("This is a informational message");
				break;
			case DEBUG:
				log.debug("This is a debug message");
				break;
			case TRACE:
				log.trace("This is a trace message");
				break;
		}

		String body = String.format("This is the response body. Response status: %s, message level %s", httpStatus, logLevel);
		return new ResponseEntity(body, httpStatus);
	}

	@GetMapping("/oom")
	public ResponseEntity oom(
		@RequestParam(required = false, defaultValue = "1") int objectsNumber) throws ParserConfigurationException, IOException, SAXException {

		Resource largeXmlFile = new ClassPathResource("large_file.xml");
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

		List<Document> documents = new ArrayList<>(objectsNumber);
		
		for (int i = 0; i < objectsNumber; i++) {
			Document document = documentBuilder.parse(largeXmlFile.getInputStream());
			documents.add(document);
		}

		String body = String.format("This is the response body. Objects number: %s", objectsNumber);
		return new ResponseEntity(body, HttpStatus.OK);
	}

	@GetMapping("/forward")
	public ResponseEntity<String> forward(@RequestParam String url) {
		return this.restTemplate.getForEntity(URI.create(url), String.class);
	}

	@Configuration
	public static class Config {
		@Bean
		public RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringMonitoringApplication.class, args);
	}
}
