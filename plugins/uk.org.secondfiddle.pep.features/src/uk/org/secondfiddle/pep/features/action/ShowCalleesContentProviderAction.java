package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeCalleesContentProvider;

public class ShowCalleesContentProviderAction extends ContentProviderAction {

	public ShowCalleesContentProviderAction(ConfigurableViewer configurableViewer) {
		super(configurableViewer);
		setToolTipText("Show Features Included by Each Feature");
		setImageDescriptor(PDEPluginImages.DESC_CALLEES);
	}

	@Override
	public IContentProvider createContentProvider() {
		return new FeatureTreeCalleesContentProvider(FeatureSupport.getManager());
	}

}