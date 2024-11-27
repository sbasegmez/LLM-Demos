package com.developi.llm;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openntf.domino.utils.XSPUtil;
import org.openntf.langchain4j.data.DominoDocumentLoader;

import com.developi.utils.XspUtils;
import com.developi.utils.jnx.DominoClientRunner;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("chatterService")
public class ChatterService {

	@Inject
	@Named("openAiApiKey")
	private String openAiApiKey;
	
	// private static final EmbeddingModel model;
	private ChatLanguageModel chatModel;
	private HelpfulAssistant assistant;

	private EmbeddingStore<TextSegment> guideEmbeddingStore;

	public ChatterService() {
	}

	ChatLanguageModel getChatModel() {
		if (this.chatModel == null) {
			System.out.println("Creating a new chat model");

			this.chatModel = OpenAiChatModel.builder()
											.apiKey(openAiApiKey)
											.timeout(Duration.ofSeconds(30))
											.modelName(OpenAiChatModelName.GPT_4_O) // Mini would be enough!
											.temperature(0.1)
											.logRequests(true)
											.logResponses(true)
											.build();
		}

		return this.chatModel;
	}

	HelpfulAssistant getAssistant() {
		if (assistant == null) {
			this.assistant = DominoClientRunner.runOnDominoClient(dc -> createAssistant(dc));
		} else {
			// debug memory
		}

		return assistant;
	}

	private HelpfulAssistant createAssistant(DominoClient dc) {
		System.out.println("Creating a new assistant");

		// We can't get current db yet.
		String dbPath = XSPUtil.getCurrentDatabase().getApiPath();
		Database database = dc.openDatabase(dbPath);

		this.guideEmbeddingStore = new InMemoryEmbeddingStore<>();

		EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
															.baseUrl(XspUtils.getXspProperty("OLLAMA_URI", "http://deepthought:11434"))
															.modelName(XspUtils.getXspProperty("OLLAMA_MODEL", "mxbai-embed-large:latest"))
															.build();

		Set<Integer> noteIds = database	.openCollection("Guides")
										.orElseThrow()
										.getAllIds(true, false);

		List<Document> documents = DominoDocumentLoader	.newLoader()
														.fieldName("Content")
														.addMetaField("Subject")
														.database(database)
														.noteIds(noteIds)
														.loadDocuments();

		List<TextSegment> segments = DocumentSplitters	.recursive(300, 0)
														.splitAll(documents);

		List<Embedding> embeddings = embeddingModel	.embedAll(segments)
													.content();
		this.guideEmbeddingStore.addAll(embeddings, segments);

		ContentRetriever contentRetriever = EmbeddingStoreContentRetriever	.builder()
																			.embeddingStore(guideEmbeddingStore)
																			.embeddingModel(embeddingModel)
																			.maxResults(2) // on each interaction we
																							// will retrieve the 2 most
																							// relevant segments
																			.minScore(0.8) // we want to retrieve
																							// segments at least
																							// somewhat similar to user
																							// query
																			.build();

		return AiServices	.builder(HelpfulAssistant.class)
							.chatLanguageModel(getChatModel())
							.contentRetriever(contentRetriever)
							.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
							.tools(new BugReportTool())
							.build();

	}
}
