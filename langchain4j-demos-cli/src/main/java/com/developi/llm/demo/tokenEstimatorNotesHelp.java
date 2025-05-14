package com.developi.llm.demo;

import com.developi.jnx.utils.AbstractStandaloneJnxApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.openntf.langchain4j.data.DominoDocumentLoader;
import org.openntf.langchain4j.data.MetadataDefinition;

public class tokenEstimatorNotesHelp extends AbstractStandaloneJnxApp {

    public static void main(String[] args) {
        new tokenEstimatorNotesHelp().run(args);
    }

    @Override
    protected void _init() {
    }

    @Override
    @SuppressWarnings("unused")
    protected void _run(DominoClient dominoClient) {
        Database database = dominoClient.openDatabase("", "help/help14_client.nsf");

        Set<Integer> docIds = database.openCollection("(All)")
                                      .orElseThrow()
                                      .getAllIds(true, false);

        var metadataDef = MetadataDefinition.builder(MetadataDefinition.DEFAULT)
                                            .addString("Subject")
                                            .build();

        List<Document> docs = DominoDocumentLoader.create(metadataDef)
                                                  .database(database)
                                                  .noteIds(docIds)
                                                  .fieldName("Subject")
                                                  .fieldName("Body")
                                                  .loadDocuments();

        OpenAiTokenCountEstimator estimator = new OpenAiTokenCountEstimator(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE);
        System.out.println(estimator.estimateTokenCountInText(docs.get(0).text()));

        AtomicLong tokenCount = new AtomicLong(0);

        docs.parallelStream().forEach(doc -> tokenCount.addAndGet(estimator.estimateTokenCountInText(doc.text())));

        System.out.println("Estimated token count: " + tokenCount.get());
    }

}
