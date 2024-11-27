package com.developi.llm.demo;

import com.developi.jnx.utils.Utils;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.util.Arrays;
import java.util.List;

public class FewShotsDemo {

    interface FewshotsAiService {

        @SystemMessage("""
                 Classify incoming sentences into sentiments: Positive, Negative, Neutral, Sarcastic.
                 Here are some examples with expected sentiments:
                                \s
                 Text: I'm very unhappy with this decision.
                 Sentiment: Negative
                                \s
                 Text: I absolutely like the idea!
                 Sentiment: Positive
                                \s
                 Text: Not sure about this.
                 Sentiment: Neutral
                                \s
                 Text: I would bu millions to protect others.
                 Sentiment: Sarcastic
                \s""")
        String ask(String sentence);

    }


    public static void main(String[] args) {
        // Import env file
        Utils.initDotenv();

        FewshotsAiService service = AiServices.builder(FewshotsAiService.class)
                                              .chatLanguageModel(OpenAiChatModel.builder()
                                                                                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                                                                                .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                                .temperature(0.1)
                                                                                .build())
                                              .build();

        List<String> sentences = Arrays.asList(
                "I love this product!",
                "This product is terrible!",
                "I would rather wait before commenting.",
                "Best product in the hall of shame!"
        );

        sentences.forEach(sentence -> {
            System.out.println("\nSentence: " + sentence);
            System.out.println(" > " + service.ask(sentence));
        });

    }
}
