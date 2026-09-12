package com.ollama.springai;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	@Autowired
	private VectorStore vectorStore;

	@PostConstruct
	public void init() {
		try {
			TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(100, 100, 5, 1000, true);

			TextReader jobListReader = new TextReader(new ClassPathResource("job_listings.txt"));
			List<Document> documents = tokenTextSplitter.split(jobListReader.get());
			vectorStore.add(documents);

			TextReader productDataReader = new TextReader(new ClassPathResource("product-data.txt"));
			documents = tokenTextSplitter.split(productDataReader.get());
			vectorStore.add(documents);

			log.info("Loaded job listings and product data into the vector store");
		}
		catch (Exception ex) {
			log.warn("Skipping vector-store data load so the app can still start: {}", ex.getMessage());
		}
	}
}
