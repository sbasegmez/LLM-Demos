package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import java.util.List;
import java.util.Set;
import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;

public class ingestManifestos extends AbstractStandaloneJnxApp {

    /**
     * Demo database location in Server!!dbFilePath format
     */
    private final static String DEMO_DB_PATH = "maggie/developi!!openntf/DemoLangchain4j.nsf";

    /**
     * OLLAMA instance URI. e.g. http://<hostname>:11434
     */
    private static final String OLLAMA_URI = "http://localhost:11434";

    /**
     * Embedded model to be used in OLLAMA
     */
    private static final String OLLAMA_EMB_MODELNAME = "mxbai-embed-large:latest";

    public static void main(String[] args) {
        new ingestManifestos().run(args);
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        // Prepare an embedding model
        EmbeddingModel embeddingModelOllama = OllamaEmbeddingModel.builder()
                                                                  .baseUrl(OLLAMA_URI)
                                                                  .modelName(OLLAMA_EMB_MODELNAME)
                                                                  .maxRetries(3)
                                                                  .logRequests(true)
                                                                  .logResponses(true)
                                                                  .build();

        EmbeddingModel embeddingModelOpenAi = OpenAiEmbeddingModel.builder()
                                                                  .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
                                                                  .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                  .build();

//        Disabled tp prevent accidental run...
//        submit(dominoClient, embeddingModelOllama, "projects_mxbai_nochunk");
//        submit(dominoClient, embeddingModelOpenAi, "projects_openai_nochunk");
        submit(dominoClient, embeddingModelOllama, "manifestos_mxbai_chunk");
    }

    public void submit(DominoClient dominoClient, EmbeddingModel embeddingModel, String collectionName) {
        Database database = dominoClient.openDatabase(DEMO_DB_PATH);

        // Prepare embedding store
        EmbeddingStore<TextSegment> embeddingStore = MilvusEmbeddingStore.builder()
                                                                         .host("skaro.developi.info")
                                                                         .port(19530)
                                                                         .databaseName("default")
                                                                         .collectionName(collectionName)
                                                                         .dimension(embeddingModel.dimension())
                                                                         .build();

        // Clear existing embeddings
        embeddingStore.removeAll();

        // Ingestor will combine the ingesting elements
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                                                                .embeddingModel(embeddingModel)
                                                                .embeddingStore(embeddingStore)
                                                                .documentSplitter(DocumentSplitters.recursive(4000, 128))
                                                                .build();

        Set<Integer> manifestoDocIds = database.openCollection("Manifestos")
                                               .orElseThrow()
                                               .getAllIds(true, false);

        var metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                            .addString("PartyName")
                                            .addString("Country")
                                            .addInteger("Year")
                                            .build();

        List<Document> docs = DominoDocumentLoader.create(metadataDef)
                                                  .database(database)
                                                  .noteIds(manifestoDocIds)
                                                  .filePattern("*.pdf")
                                                  .documentParser(new ApachePdfBoxDocumentParser())
                                                  .loadDocuments();

        System.out.println("Ingesting set of " + docs.size() + " documents");

        IngestionResult result = ingestor.ingest(docs);

        System.out.println("All ingested" + (result.tokenUsage() == null ? "" : (" : " + result.tokenUsage() + " tokens used!")));

    }

}
