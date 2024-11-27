package com.developi.renderkit;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.lang3.StringUtils;

import com.ibm.xsp.extlib.component.outline.AbstractOutline;
import com.ibm.xsp.extlib.tree.ITreeNode;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.theme.bootstrap.renderkit.html.extlib.outline.tree.MenuRenderer;

public class NavigatorMenuRenderer extends MenuRenderer {

	private static final long serialVersionUID = 1L;

    public NavigatorMenuRenderer(AbstractOutline outline, int type) {
    	super(outline, type);
	}

	@Override
    protected String getContainerStyleClass(TreeContextImpl node) {
    	/* Originally, it checks the type. We don't know and we don't care for now. 
    	if(type==TYPE_LIST) {
            return "nav nav-list";
        }
        return "nav nav-pills nav-stacked"; */
		
		// nav-sidebar is a type of nav. But it's the only type now, so no problem with making it hardcoded.
    	return "nav nav-sidebar";
    }

	@Override
	protected String getItemStyleClass(TreeContextImpl tree, boolean enabled, boolean selected) {
		String styleClass = super.getItemStyleClass(tree, enabled, selected);
		
        return ExtLibUtil.concatStyleClasses(styleClass, "nav-item");		
	}
	
	@Override
	protected void renderEntryItemLinkAttributes(FacesContext context, ResponseWriter writer, TreeContextImpl tree, boolean enabled, boolean selected)
			throws IOException {
		super.renderEntryItemLinkAttributes(context, writer, tree, enabled, selected);

		ITreeNode node = tree.getNode();
		
		//System.out.println(tree.getNode().getClass().getCanonicalName() + " : " + tree.getNode().getHref());
		
		writer.writeAttribute("class", "nav-link", null);

		// we use imageAlt for FA icons
		if(StringUtils.isNotEmpty(node.getImageAlt())) {
			writer.startElement("i", null);
			writer.writeAttribute("class", node.getImageAlt(), null);
			writer.endElement("i");
		}		
	}

	/**
	 * Test:

 	<xe:navigator
		id="navigator2"
		expandEffect="wipe"
		expandable="true"
		keepState="true"
		rendererType="limitless.NavigatorRenderer">
		<xe:this.treeNodes>
			<xe:pageTreeNode
				label="Home"
				selection="/home"
				page="/index.xsp"
				imageAlt="fas fa-home">
			</xe:pageTreeNode>
			<xe:pageTreeNode
				label="Contacts"
				selection="/home/contacts"
				page="/contacts.xsp">
			</xe:pageTreeNode>
			<xe:separatorTreeNode></xe:separatorTreeNode>
			<xe:pageTreeNode
				label="Other"
				selection="/home/other"
				page="/other.xsp">
			</xe:pageTreeNode>
			<xe:basicLeafNode
				label="Link 1"
				href="#">
			</xe:basicLeafNode>
		</xe:this.treeNodes>
	</xe:navigator>

	 */
	
}
