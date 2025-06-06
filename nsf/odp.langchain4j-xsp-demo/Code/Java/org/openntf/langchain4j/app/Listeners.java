package org.openntf.langchain4j.app;

import java.io.Serializable;

import javax.servlet.http.HttpSessionEvent;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener;
import com.ibm.xsp.application.events.SessionListener;

public class Listeners implements Serializable, SessionListener, ApplicationListener {

	private static final long serialVersionUID = 1L;

	// No injection would work in app-listeners
	
	@Override
	public void applicationCreated(ApplicationEx app) {
	}

	@Override
	public void applicationDestroyed(ApplicationEx app) {
	}

	@Override
	public void sessionCreated(ApplicationEx app, HttpSessionEvent session) {
	}

	@Override
	public void sessionDestroyed(ApplicationEx app, HttpSessionEvent session) {
	}

}
