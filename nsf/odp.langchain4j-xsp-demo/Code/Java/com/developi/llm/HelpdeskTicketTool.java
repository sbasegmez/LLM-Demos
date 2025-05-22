package com.developi.llm;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import dev.langchain4j.agent.tool.Tool;
import lotus.domino.Database;
import lotus.domino.Document;

public class HelpdeskTicketTool {

	
	@Tool("""
			Create help desk ticket and return the problem id for future reference.
			We need two parameters to use this tool.
			- Severity is either one of "Low", "Medium", "Critical" or "Unknown"
			- Problem Definition is contains detailed information about the problem, optionally steps to reproduce and platform. 
			""")
	public String createHelpdeskTicket(String severity, String problemDefinition) {
		System.out.println("Creating a ticket: " + problemDefinition);
		
		try {
			Database appDb = ExtLibUtil.getCurrentDatabase();
			Document doc = appDb.createDocument();
			
			doc.replaceItemValue("Form", "HelpdeskTicket");
			doc.replaceItemValue("Severity", severity);
			doc.replaceItemValue("Problem", problemDefinition);
		
			doc.computeWithForm(false, false);
		
			if(doc.save()) {
				System.out.println("Saved!");
				return doc.getItemValueString("ProblemId");
			}

		} catch(Throwable t) {
			t.printStackTrace();
			throw new RuntimeException("We can't create the ticket now!", t);
		}
		
		System.out.println("NOT Saved!");
		throw new RuntimeException("We can't create the ticket now!");
	}
	
}
