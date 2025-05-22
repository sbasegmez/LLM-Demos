package com.developi.llm;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import dev.langchain4j.agent.tool.Tool;
import lotus.domino.Database;
import lotus.domino.Document;

public class BugReportTool {

	
	@Tool("""
			Create a bug report and return the problem id for future reference.
			We need three things to complete this step.
			- Product includes the product name and the version, separated by space.
			- Severity is either one of "Low", "Medium", "Critical" or "Unknown"
			- Problem Definition is contains detailed information about the problem, including steps to reproduce and platform. 
			""")
	public String createBugReport(String product, String severity, String problemDefinition) {
		System.out.println("Creating a bug report: " + problemDefinition);
		
		try {
			Database appDb = ExtLibUtil.getCurrentDatabase();
			Document doc = appDb.createDocument();
			
			doc.replaceItemValue("Form", "BugReport");
			doc.replaceItemValue("ProductNameVersion", product);
			doc.replaceItemValue("Severity", severity);
			doc.replaceItemValue("Problem", problemDefinition);
		
			doc.computeWithForm(false, false);
		
			if(doc.save()) {
				System.out.println("Saved!");
				return doc.getItemValueString("ProblemId");
			}

		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		System.out.println("NOT Saved!");
		throw new RuntimeException("We can't create the bug report now!");
	}
	
}
