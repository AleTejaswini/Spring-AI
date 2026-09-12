package com.ollama.springai.services;

import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ollama.springai.text.prompttemplate.dtos.CountryCuisines;

@Service
public class OllamaService {

	private final ChatClient chatClient;

	@Autowired
	private EmbeddingModel embeddingModel;

	@Autowired
	private VectorStore vectorStore;

	public OllamaService(ChatClient.Builder builder) {
		ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
		this.chatClient = builder
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build();
	}

	public ChatResponse generateAnswer(String question) {
		return chatClient.prompt(question).call().chatResponse();
	}

	public ChatResponse generateAnswerWithRoles(String question) {
		return chatClient.prompt()
				.system("You are a helpful assistant that can answer any question.")
				.user(question)
				.call()
				.chatResponse();
	}

	public String getTravelGuidance(String city, String month, String language, String budget) {
		PromptTemplate promptTemplate = new PromptTemplate("Welcome to the {city} travel guide!\n"
				+ "If you're visiting in {month}, here's what you can do:\n" + "1. Must-visit attractions.\n"
				+ "2. Local cuisine you must try.\n" + "3. Useful phrases in {language}.\n"
				+ "4. Tips for traveling on a {budget} budget.\n" + "Enjoy your trip!");
		Prompt prompt = promptTemplate
				.create(Map.of("city", city, "month", month, "language", language, "budget", budget));

		return chatClient.prompt(prompt).call().content();
	}

	public CountryCuisines getCuisines(String country, String numCuisines, String language) {
		PromptTemplate promptTemplate = new PromptTemplate("You are an expert in traditional cuisines.\n"
				+ "Answer the question: What is the traditional cuisine of {country}?\n"
				+ "Return a list of {numCuisines} in {language}.\n" + "You provide information about a specific dish \n"
				+ "from a specific country.\n" + "Avoid giving information about fictional places.\n"
				+ "If the country is fictional or non-existent \n" + "return the country with out any cuisines.");

		Prompt prompt = promptTemplate
				.create(Map.of("country", country, "numCuisines", numCuisines, "language", language));

		return chatClient.prompt(prompt).call().entity(CountryCuisines.class);
	}

	public float[] embed(String text) {
		return embeddingModel.embed(text);
	}

	public double findSimilarity(String text1, String text2) {
		List<float[]> response = embeddingModel.embed(List.of(text1, text2));
		return cosineSimilarity(response.get(0), response.get(1));
	}

	private double cosineSimilarity(float[] vectorA, float[] vectorB) {
		if (vectorA.length != vectorB.length) {
			throw new IllegalArgumentException("Vectors must be of the same length");
		}

		double dotProduct = 0.0;
		double magnitudeA = 0.0;
		double magnitudeB = 0.0;

		for (int i = 0; i < vectorA.length; i++) {
			dotProduct += vectorA[i] * vectorB[i];
			magnitudeA += vectorA[i] * vectorA[i];
			magnitudeB += vectorB[i] * vectorB[i];
		}

		return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
	}

	public List<Document> searchJobs(String query) {
		return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(3).build());
	}

	public String answer(String query) {
		return chatClient.prompt(query)
				.advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
				.call()
				.content();
	}

	public String getStockPrice(String company) {
		return chatClient.prompt()
				.user("Get stock symbol and stock price for " + company)
				.toolNames("stockRetrievalFunction")
				.call()
				.content();
	}
}
