package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureIndex;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeCallersContentProvider;

public class ShowCallersContentProviderAction extends ContentProviderAction {

	private final FeatureIndex featureIndex;

	public ShowCallersContentProviderAction(ConfigurableViewer configurableViewer, FeatureIndex featureIndex) {
		super(configurableViewer);
		this.featureIndex = featureIndex;

		setToolTipText("Show Features Including Each Feature");
		setImageDescriptor(PDEPluginImages.DESC_CALLERS);
	}

	@Override
	public IContentProvider createContentProvider() {
		return new FeatureTreeCallersContentProvider(FeatureSupport.getManager(), featureIndex);
	}

}