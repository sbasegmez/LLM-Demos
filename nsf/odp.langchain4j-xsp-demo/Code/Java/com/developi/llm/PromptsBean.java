package com.developi.llm;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("prompts")
public class PromptsBean {

	@Inject
	@Named("chatterService")
	private ChatterService chatterService;

	private SentimentServiceFewShots sentimentServiceFewShots;

	public String sendComment(String comment) {
		return getSentimentServiceFewShots().ask(comment);
	}

	public SentimentServiceFewShots getSentimentServiceFewShots() {
		if (sentimentServiceFewShots == null) {
			this.sentimentServiceFewShots = AiServices	.builder(SentimentServiceFewShots.class)
														.chatModel(chatterService.getChatModel())
														.build();
		}

		return sentimentServiceFewShots;
	}

	interface SentimentServiceFewShots {

		@SystemMessage("""
				Classify incoming sentences into sentiments: Positive, Negative, Neutral, Sarcastic.
				Here are some examples with expected sentiments:

				Text: I'm very unhappy with this decision.
				Sentiment: Negative

				Text: I absolutely like the idea!
				Sentiment: Positive

				Text: Not sure about this.
				Sentiment: Neutral

				Text: I would bu millions to protect others.
				Sentiment: Sarcastic
				""")
		String ask(String sentence);

	}

}
