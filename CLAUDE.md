# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Spring-AI is a hands-on demo of Generative AI integration in Java using Spring Boot, Spring AI, and Ollama (Llama 3.2). The actual Maven project lives in the `llama-demo/` subdirectory — run all commands from there, not the repo root.

Concepts demonstrated: ChatClient/LLM integration, prompt templates, structured output, embeddings, cosine similarity, semantic search, vector store, RAG, and function calling.

## Commands

Run all commands from `llama-demo/`:

```bash
mvn clean install       # build
mvn spring-boot:run     # run the app (serves on http://localhost:8081)
mvn test                 # run all tests
mvn test -Dtest=OllamaDemoApplicationTests   # run a single test class
```

### Runtime prerequisite

The app talks to a local Ollama instance at `http://localhost:11434` (see `application.properties`). Install Ollama and pull the model before running:

```bash
ollama pull llama3.2
```

Ollama embeddings are disabled (`spring.ai.ollama.embedding.enabled=false`) because the local server returns 501 unless started with `--embeddings`; the on-device Transformers embedding model is used instead via the default Spring AI auto-configuration.

## Architecture

- **Single `OllamaService`** (`services/OllamaService.java`) is the hub for all Spring AI interaction — every feature-specific controller autowires this one service rather than talking to `ChatClient`/`EmbeddingModel`/`VectorStore` directly. When adding a new AI capability, add a method here first.
- **Controller-per-feature pages**: each demo feature (travel guide, cuisine helper, function calling, product RAG bot, job search/embeddings, similarity finder) is its own `@Controller` with a `GET /show...` handler that returns a Thymeleaf template and a `POST` handler that calls into `OllamaService` and repopulates the model for the same template. Templates live in `src/main/resources/templates/` and are named to match (e.g. `travelGuide.html` ↔ `TravelGuideController`).
- **Chat memory**: `OllamaService` builds its `ChatClient` with a `MessageChatMemoryAdvisor` backed by an in-memory `MessageWindowChatMemory`, so conversational state persists only for the life of the JVM.
- **Vector store**: `VectorStoreConfig` provides a `SimpleVectorStore` bean (in-memory, backed by the configured `EmbeddingModel`). `DataInitializer` (`@PostConstruct`, gated by `app.data-init.enabled`, default `true`) loads and chunks `job_listings.txt` and `product-data.txt` from `src/main/resources/` into it at startup using a `TokenTextSplitter`. It swallows/logs errors rather than failing startup, so the app still boots if the embedding step fails (e.g. Ollama not running). Disable via `app.data-init.enabled=false` (already done in the test `@SpringBootTest`).
- **RAG**: `ProductDataBot` queries the vector store through `OllamaService#answer`, which attaches a `QuestionAnswerAdvisor(vectorStore)` to the chat prompt (retrieval + augmentation happens via the advisor, not manual prompt construction).
- **Function calling**: `Functions` (`@Configuration`) registers a `Function<StockRetrievalService.Request, StockRetrievalService.Response>` bean named `stockRetrievalFunction`, described via `@Description` for the LLM's tool-selection. `OllamaService#getStockPrice` invokes it by name via `chatClient.prompt().toolNames("stockRetrievalFunction")`. To add a new tool: create the `Function` implementation, register a `@Bean` with a `@Description`, and reference its bean name via `.toolNames(...)`.
- **Structured output**: `OllamaService#getCuisines` uses `PromptTemplate` + `.entity(CountryCuisines.class)` to have Spring AI deserialize the model's response directly into a DTO (`text/prompttemplate/dtos/CountryCuisines.java`).
- **Embeddings/similarity**: `EmbeddingDemo`, `SimilarityFinder`, and `JobSearchHelper` (all in `embeddings/`) are thin controllers over `OllamaService#embed`, `#findSimilarity` (manual cosine similarity), and `#searchJobs` (vector store similarity search) respectively.

## Notes

- Do not commit `.metadata/` (Eclipse workspace state) — it's excluded at the repo root via `.gitignore`, but `llama-demo/.metadata/` currently is not; be careful not to stage it.
