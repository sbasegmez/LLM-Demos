# Large Language Models - Demos

This repository supports my sessions on large language models and their integration into Domino apps. You can find the slides for the sessions below:

- [Experimenting with Integrating Large Language Models into Domino Apps" at Engage 2024.](https://speakerdeck.com/sbasegmez/engage-2024-experimenting-with-integrating-large-language-models-into-domino-apps)
- [Integrating Large Language Models into Domino Applications: A Primer at OpenNTF Webinars](https://speakerdeck.com/sbasegmez/openntf-webinars-2024-integrating-large-language-models-into-domino-applications-a-primer)
    - [Youtube](https://www.youtube.com/watch?v=TtdR1sX1Kbg)
- Transforming Domino Applications with LLMs: DominoIQ and Beyond

This is also the showcase project for the [Langchain4j for Domino](https://github.com/sbasegmez/langchain4j-domino) project.

## Langchain4j - CLI Demos

There are a few Java samples demonstrating how Domino objects within a database can be ingested, searched semantically, or utilized via AIService. These samples' prerequisites are listed below. Feel free to contribute your own code with relevant modifications or play with the code.

-   **Project Metadata Database**: Some of the code requires a project metadata database, which is publicly available as an [OpenNTF project](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Projects%20Dataset).
-   **LLM Model Container**: The ingestion code leverages the [Ollama](https://ollama.com/) LLM management tool, preferably installed locally.
-   **Vector Databases**: Demo code uses Chroma](https://www.trychroma.com/) and [Milvus](https://milvus.io/) vector stores, preferably installed locally.
-   **OpenAI API**: The AiService demos require access to the OpenAI API, along with a valid API key.
-   **LangChain4j Domino XSP Demo Database**: You can get it from [here](./nsf/odp.langchain4j-xsp-demo).

### Contents

- [PromptsDemo](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/PromptsDemo.java)
    - Demonstrate use of an AI service with a simple prompt.
    - Uses OpenAI API.
- [FewShotsDemo](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/FewShotsDemo.java)
    - Use of the few shots prompt technique in a simple AiService
    - Uses OpenAI API.
- [tokenEstimatorNotesHelp](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/tokenEstimatorNotesHelp.java)
    - Read Notes Help database and give an estimate for how many token it will take to ingest.
    - Uses Notes Help database and OpenAI API.
- [ModerationDemo](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/ModerationDemo.java)
    - Demonstrate use of ModerationModel with a chat model
    - Uses OpenAI.
- [IngestProjectMetadata](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/IngestProjectMetadata.java)
    - Demonstrates a simple ingestion pipeline with `DominoDocumentLoader` for the OpenNTF projects. 
    - Uses OpenNTF Project metadata database,  mxbai embedding model from Ollama server, creates a Milvus vector store.
- [IngestNotesHelp](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/IngestNotesHelp.java)
    - Demonstrates a simple ingestion pipeline with `DominoDocumentLoader` for Notes Help documents and running parallel procesing for the ingestion. 
    - Uses local Notes help database, OpenAI Text embedding model and creates a Chroma vector store.
- [IngestManifestos](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/IngestManifestos.java)
    - Demonstrates a simple ingestion pipeline with `DominoDocumentLoader` and attachment uploading for manifestos from the demo database. 
    - Uses demo database, mxbai embedding model from Ollama server, creates a Milvus vector store.
- [AskAboutManifesto](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/AskAboutManifesto.java)
    - Demonstrates a simple RAG pipeline with previously ingested manifesto documents.
    - Uses model and vector store from `IngestManifestos` class.
- [AskAboutNotes](langchain4j-demos-cli/src/main/java/com/developi/llm/demo/AskAboutNotes.java)
    - Demonstrates a simple RAG pipeline with previously ingested help documents.
    - Uses model and vector store from `IngestNotesHelp` class.

### Running Standalone Java Files

-   Copy the `.env.example` file to your home directory, rename it to `.env`, and add your OpenAI API key and set other variables.
-   Use Java SDK version 17 
-   If Java code connects to a Notes database:
    -   If you’re running Apple Silicon computers, make sure you use a x86_64 JVM (e.g. IBM Semeru OpenSDK 17) to run the demo databases.
    -   The following environment variables need to be set:
        -   `DYLD_LIBRARY_PATH`, `LD_LIBRARY_PATH`, `Notes_ExecDirectory` pointing to the Notes directory (e.g. `/Applications/HCL Notes.app/Contents/MacOS` for Mac OS)
        -   `Notes_IDPath`, `Notes_IDPassword` to use custom ID file and password. If you don't provide an ID file and password, you will either remove password from your Notes installation, or keep local Notes open when running examples.

>   [!WARNING]
>
>   If you are using macOS with System Integrity Protection (introduced in El Capitan) enabled,  SIP strips the `DYLD_LIBRARY_PATH` environment variable from the test process. Use your IDE's own run configuration instead. 

## Langchain4j - Domino XSP Demo Database

The demo database contains a couple of XPages and a few Java files demonstrating basic usage of AiService class, RAG pipeline and the chat model.

-   **Project Metadata Database**: Some of the code requires a project metadata database, which is publicly available as an [OpenNTF project](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Projects%20Dataset).
-   **Langchain4j-Domino Integration**: The project utilizes langchain4j-domino, which will be made publicly available in the near future. Preview version is [here](https://github.com/sbasegmez/langchain4j-domino).
-   **Vector Database**: Demo code uses Chroma](https://www.trychroma.com/), preferably installed locally. The project metadata and Help database should be ingested. See [CLI Demos](langchain4j-demos-cli/) for the code.
-   **OpenAI API**: The AiService demos require access to the OpenAI API, along with a valid API key.
-   **Other XPages plugins:** They need to be installed on server and designer client. 
    -   [Domino JNX - XPages](https://github.com/HCL-TECH-SOFTWARE/domino-jnx)
    -   [Jakarta EE Project](https://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20Jakarta%20EE%20Support)

### Contents

-   **Demo1 : Few Shots example**
    -   Demonstrates running a small AiService with a prompt.
-   **Demo2 : Semantic Search**
    -   Demonstrates semantic search over previously ingested project metadata documents.
    -   Make sure you ingested project metadata with two different model. 
    -   Check the  [SemanticSearch.java](nsf/odp.langchain4j-xsp-demo/Code/Java/com/developi/llm/SemanticSearch.java) for details. 
-   **Demo3 : Helpful Assistant**
    -   Demonstrates using an assistant chatbot with a simple tool support and a short RAG pipeline..
-   **Demo4 : Helpdesk Assistant**
    -   More advanced example for a helpdesk assistant with RAG and tool support.

## Domino IQ Demo

See [Readme.txt](nsf/nsf.engage25-dominoiq-demo/Readme.txt) for details

## Setup

To configure the demos, open the xsp.properties file in your server’s data directory and define the following variables:

```properties
PMT_METADATA_DB=demos/pmt_metadata.nsf
OLLAMA_URI=http://<ollama-server>:11434
OLLAMA_MODEL=mxbai-embed-large:latest
CHROMA_URI=http://<chroma-server>:8000
langchain4j.OPENAI_API_KEY=<Openai-api-key>
```



### Compatibility

This project has been tested with Domino 14 and Java 17.

