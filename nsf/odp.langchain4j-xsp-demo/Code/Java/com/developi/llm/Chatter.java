package com.developi.llm;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import com.developi.utils.NotesName;
import com.ibm.xsp.model.domino.DominoUtils;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ConversationScoped
@Named("chatter")
public class Chatter implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int memoryId = RandomGenerator.getDefault().nextInt();
	
	@Inject
	@Named("chatterService")
	ChatterService chatterService;

	public Chatter() {
		System.out.println("Creating new chatter");
	}

	public String sendMessage(String message) {
		try {
			String userName = NotesName.toCommon(DominoUtils.getCurrentDirectoryUser());
			
			return chatterService.getAssistant().chat(this.memoryId, userName, message);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
