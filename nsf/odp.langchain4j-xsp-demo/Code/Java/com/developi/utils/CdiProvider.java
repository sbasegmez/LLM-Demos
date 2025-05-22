package com.developi.utils;

import org.apache.commons.lang3.StringUtils;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;

@RequestScoped
public class CdiProvider {
 
    @Produces
    @Named("openAiApiKey")
    public String getOpenAiApiKey() {
    	//return "demo";
    	String apiKey = ExtLibUtil.getXspProperty("langchain4j.OPENAI_API_KEY");
    	
    	if(StringUtils.isNotEmpty(apiKey)) {
    		return apiKey;
    	} 
    	
    	System.out.println("Using Demo api key!");
 
    	return "demo";
    }
    
}
