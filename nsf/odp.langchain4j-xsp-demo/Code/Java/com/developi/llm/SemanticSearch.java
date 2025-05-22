package com.developi.llm;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openntf.misc.jnx.DominoClientRunner;
import org.openntf.misc.utils.XspUtils;

import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.FTQueryResult;
import com.hcl.domino.data.NoteIdWithScore;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("semantic")
public class SemanticSearch {

	public String searchSemantic(String searchText) {
		JsonJavaArray result = DominoClientRunner.runOnDominoClient(dc -> searchSemanticInternal(dc, searchText));
		return result.toString();
	}

	public String searchSemantic2(String searchText) {
		JsonJavaArray result = DominoClientRunner.runOnDominoClient(dc -> searchSemanticInternal2(dc, searchText));
		return result.toString();
	}

	public String searchFulltext(String searchText) {
		JsonJavaArray result = DominoClientRunner.runOnDominoClient(dc -> searchFulltextInternal(dc, searchText));
		return result.toString();
	}

	// Internal version
	private JsonJavaArray searchFulltextInternal(DominoClient dominoClient, String searchText) {
		Database db = dominoClient.openDatabase(XspUtils.getXspProperty("PMT_METADATA_DB", "demos/pmt_metadata.nsf"));

		JsonJavaArray resultArray = new JsonJavaArray();
		Set<FTQuery> options = EnumSet.of(FTQuery.SCORES, FTQuery.FUZZY);

		String query = searchText.replaceAll("\\s+", " AND ");
		System.out.println(query);
		FTQueryResult ftResult = db.queryFTIndex(query, 1000, options, null, 0, 1000);

		List<NoteIdWithScore> scores = ftResult.getMatchesWithScore();

		scores.forEach(
				noteIdWithScore -> {
					System.out.println(noteIdWithScore.getNoteId() + " : " + noteIdWithScore.getScore());
					db	.getDocumentById(noteIdWithScore.getNoteId())
										.ifPresent(doc -> {
											if ("project".equalsIgnoreCase(doc.getAsText("Form", ' '))) {
												JsonJavaObject jsonValue = new JsonJavaObject();
												jsonValue.put("name", doc.getAsText("name", ' '));
												jsonValue.put("unid", doc.getUNID());
												jsonValue.put("score", noteIdWithScore.getScore());
												resultArray.add(jsonValue);
											}
										});
				});

		return resultArray;
	}

	private JsonJavaArray searchSemanticInternal(DominoClient dominoClient, String searchText) {
		JsonJavaArray resultArray = new JsonJavaArray();

		String collectionName = "projects_mxbai_nochunk";

		// Prepare an embedding model
		EmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
															.baseUrl(XspUtils.getXspProperty("OLLAMA_URI",
																	"http://skaro.developi.info:11434"))
															.modelName(XspUtils.getXspProperty("OLLAMA_MODEL",
																	"mxbai-embed-large:latest"))
															.maxRetries(3)
															.logRequests(true)
															.logResponses(true)
															.build();

		// Prepare embedding store
		EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore	.builder()
																			.baseUrl(XspUtils.getXspProperty(
																					"CHROMA_URI",
																					"http://skaro.developi.info:8000"))
																			.collectionName(collectionName)
																			.build();

		// Build a Search Request
		EmbeddingSearchRequest request = EmbeddingSearchRequest	.builder()
																.maxResults(20)
																.minScore(0.4)
																.queryEmbedding(embeddingModel	.embed(searchText)
																								.content())
																.build();

		// Search
		EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

		// Result to json file
		result	.matches()
				.forEach(m -> {
					TextSegment project = m.embedded();

					JsonJavaObject jsonValue = new JsonJavaObject();
					jsonValue.put("name", project	.metadata()
													.getString("name"));
					jsonValue.put("unid", project	.metadata()
													.getString("unid"));
					jsonValue.put("score", m.score());

					resultArray.add(jsonValue);
				});

		return resultArray;
	}

	@Inject
	@Named("openAiApiKey")
	private String openAiApiKey;

	private JsonJavaArray searchSemanticInternal2(DominoClient dominoClient, String searchText) {
		JsonJavaArray resultArray = new JsonJavaArray();

		String collectionName = "projects_openai_nochunk";

		// Prepare an embedding model
		EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
															.modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
															.apiKey(openAiApiKey)
															.build();

		// Prepare embedding store
		EmbeddingStore<TextSegment> embeddingStore = ChromaEmbeddingStore	.builder()
																			.baseUrl(XspUtils.getXspProperty(
																					"CHROMA_URI",
																					"http://skaro.developi.info:8000"))
																			.collectionName(collectionName)
																			.build();

		// Build a Search Request
		EmbeddingSearchRequest request = EmbeddingSearchRequest	.builder()
																.maxResults(20)
																.minScore(0.4)
																.queryEmbedding(embeddingModel	.embed(searchText)
																								.content())
																.build();

		// Search
		EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

		// duplicate check
		Set<String> resultSet = new HashSet<>();

		// Result to json file
		result	.matches()
				.forEach(m -> {
					TextSegment project = m.embedded();
					String projectName = project.metadata()
												.getString("name");

					if (!resultSet.contains(projectName)) {
						JsonJavaObject jsonValue = new JsonJavaObject();
						jsonValue.put("name", projectName);
						jsonValue.put("unid", project	.metadata()
														.getString("unid"));
						jsonValue.put("score", m.score());

						resultArray.add(jsonValue);
						resultSet.add(projectName);
					}
				});

		return resultArray;
	}
}
