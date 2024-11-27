package com.developi.renderkit;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.component.outline.AbstractOutline;
import com.ibm.xsp.theme.bootstrap.renderkit.html.extlib.outline.OutlineMenuRenderer;
import com.ibm.xsp.theme.bootstrap.renderkit.html.extlib.outline.tree.MenuRenderer;

public class NavigatorRenderer extends OutlineMenuRenderer {

	@Override
    protected MenuRenderer createMenuRenderer(FacesContext context, AbstractOutline outline) {
        int type = MenuRenderer.TYPE_PILL;
        if(outline!=null) {
            String styleClass = outline.getStyleClass();
            if(StringUtil.isNotEmpty(styleClass) && styleClass.contains("nav-list")) { // $NON-NLS-1$
                type = MenuRenderer.TYPE_LIST;
            }
        }
        return new NavigatorMenuRenderer(outline,type);
    }
}