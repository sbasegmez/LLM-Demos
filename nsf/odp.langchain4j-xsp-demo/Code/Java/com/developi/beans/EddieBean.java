package com.developi.beans;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.openntf.misc.collections.AbstractTimedMap;
import org.openntf.misc.utils.XspUtils;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lotus.domino.NotesException;

/**
 * Eddie is the all-knowing semi-intelligent orchestrator of everything related to the application (see Hitchhiker's Guide for reference). 
 * 
 * @author sbasegmez
 *
 */

@ApplicationScoped
@Named("eddie")
public class EddieBean extends AbstractTimedMap {

	private static final long serialVersionUID = 1L;

	//private static Logger logger = LoggerFactory.getLogger(EddieBean.class);
	
	private String baseUrl;
	private String themeUrl = "/dpg-theme";
	
	@Inject
	@ConfigProperty(name = "dsg.refreshEddie.mins", defaultValue="60")
	private long refreshIntervalMins;

	@Override
	public long getRefreshIntervalMins() {
		return refreshIntervalMins;
	}

	@Override
	public void updateValues() {
	}
	
	@PostConstruct
	private void initBasics() {
		reviewStaffBeforeStart();
		
		this.baseUrl = XspUtils.getDatabaseURL();		 
	}
	
	/**
	 * This routine will check things before running the app. Should not throw exception...
	 * 
	 * TODO If anything goes wrong, redirect to an error page. Or maybe we do it on first time pageLoad!
	 * 
	 */
	public void reviewStaffBeforeStart() {
	}

	public String getBaseUrl() {
		return baseUrl;
	}

	public String getThemeUrl() {
		return themeUrl;
	}

	public boolean isFtIndexed() {
		try {
			return ExtLibUtil.getCurrentDatabase().isFTIndexed();
		} catch (NotesException e) {
			return false;
		}
	}

	public String getLogoutLink() {
		return "/names.nsf?logout&redirectTo=" + getBaseUrl();
	}
	
}
