package com.developi.llm;

import java.io.Serializable;
import java.util.random.RandomGenerator;

import org.openntf.misc.utils.NotesName;

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

	public String sendMessageHelpfulAssistant(String message) {
		try {
			String userName = NotesName.toCommon(DominoUtils.getCurrentDirectoryUser());
			
			return chatterService.getHelpfulAssistant().chat(this.memoryId, userName, message);
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public String sendMessageHelpdeskAssistant(String message) {
	    try {
	        String userName = NotesName.toCommon(DominoUtils.getCurrentDirectoryUser());
	        
	        return chatterService.getHelpdeskAssistant().chat(this.memoryId, userName, message);
	    } catch(Exception e) {
	        e.printStackTrace();
	        throw new RuntimeException(e);
	    }
	}
	
}
