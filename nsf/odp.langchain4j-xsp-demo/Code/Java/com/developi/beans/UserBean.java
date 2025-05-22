package com.developi.beans;

import java.io.Serializable;
import java.util.List;

import javax.faces.context.FacesContext;

import org.openntf.misc.utils.NotesName;

import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import lotus.domino.NotesException;
import lotus.domino.Session;

@ApplicationScoped
@Named("user")
public class UserBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public NotesName getNotesName() {
		Session session = ExtLibUtil.getCurrentSession();
		try {
			return new NotesName(session.getEffectiveUserName());
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public boolean hasRole(String role) {
		List<?> roles = XSPContext.getXSPContext(FacesContext.getCurrentInstance()).getUser().getRoles();

		return null == roles ? false: roles.contains(role);
	}
	
	public String getCommonName() {
		return getNotesName().getCommonName();
	}
	
	public boolean isAdmin() {
		return hasRole("[Admin]");
	}


	
}
