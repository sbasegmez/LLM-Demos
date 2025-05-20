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
    -   Copy the .env.example file to your home directory, rename it to .env, and add your OpenAI API key. Also set other variables.
-   **LangChain4j Domino XSP Demo Database**: You can get it from [here](./nsf/odp.langchain4j-xsp-demo).

## Langchain4j - Domino XSP Demo Database

The demo database contains a couple of XPages and a few Java files demonstrating basic usage of AiService class, RAG pipeline and the chat model.

-   **Project Metadata Database**: Some of the code requires a project metadata database, which is publicly available as an [OpenNTF project](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF%20Projects%20Dataset).
-   **Langchain4j-Domino Integration**: The project utilizes langchain4j-domino, which will be made publicly available in the near future. Preview version is [here](https://github.com/sbasegmez/langchain4j-domino).
-   **Vector Database**: Demo code uses Chroma](https://www.trychroma.com/), preferably installed locally. The project metadata and Help database should be ingested. See [CLI Demos](langchain4j-demos-cli/) for the code.
-   **OpenAI API**: The AiService demos require access to the OpenAI API, along with a valid API key.
-   **Other XPages plugins:** They need to be installed on server and designer client. 
    -   [Domino JNX - XPages](https://github.com/HCL-TECH-SOFTWARE/domino-jnx)
    -   [Jakarta EE Project](https://www.openntf.org/main.nsf/project.xsp?r=project/XPages%20Jakarta%20EE%20Support)

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

#### Java

This project has been tested on Java 17 or higher.

#### Apple Silicon

If you’re running Apple Silicon computers, use a x86_64 JVM (e.g. IBM Semeru OpenSDK 17) to run the demo databases.
