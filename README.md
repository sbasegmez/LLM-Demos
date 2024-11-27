# Large Language Models - Demo Project

This repository supports my sessions on large language models and their integration into Domino apps. You can find the slides for the sessions below:

- [Experimenting with Integrating Large Language Models into Domino Apps" at Engage 2024.](https://speakerdeck.com/sbasegmez/engage-2024-experimenting-with-integrating-large-language-models-into-domino-apps)
- [Integrating Large Language Models into Domino Applications: A Primer at OpenNTF Webinars](https://speakerdeck.com/sbasegmez/openntf-webinars-2024-integrating-large-language-models-into-domino-applications-a-primer)



### Demos

#### Module: langchain4j-demos-cli

-   **Prompts and Fewshots Demos**: Demonstrations of the basic functionality of the AiService.
-   **ingestProjectMetadata**: Demonstrates how project metadata can be ingested into a vector database.





### Prerequisites

-   **Project Metadata Database**: Some of the code requires a project metadata database, which is publicly available as the [OpenNTF project](https://www.openntf.org/main.nsf/project.xsp?r=project/OpenNTF Projects Dataset).

-   **Langchain4j-Domino Integration**: The project utilizes langchain4j-domino, which will be made publicly available in the near future.

-   **LLM Management Tool**: The ingestion code leverages the [Ollama](https://ollama.com/) LLM management tool, preferably installed locally.

-   **Vector Database**: The ingestion target is a [Chroma](https://www.trychroma.com/) vector database, preferably installed locally.

-   **OpenAI API**: The AiService demos require access to the OpenAI API, along with a valid API key.
    -   Copy the .env.example file to your user directory, rename it to .env, and add your OpenAI API key.



### Compatibility

#### Java

This project has been tested on Java 17 or higher.

#### Apple Silicon

If you’re running Apple Silicon computers, use a x86_64 JVM (e.g. IBM Semeru OpenSDK 17) to run the demo databases.
