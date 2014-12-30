package uk.org.secondfiddle.pep.features.explorer.action;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import uk.org.secondfiddle.pep.features.explorer.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.explorer.viewer.FeatureTreeCalleesContentProvider;
import uk.org.secondfiddle.pep.features.explorer.viewer.FeatureViewerComparator;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class ShowCalleesContentProviderAction extends ContentProviderAction {

	public ShowCalleesContentProviderAction(ConfigurableViewer configurableViewer,
			FeatureModelManager featureModelManager, ProductModelManager productModelManager) {
		super(configurableViewer, featureModelManager, productModelManager);
		setToolTipText("Show Features Included by Each Feature");
		setImageDescriptor(PDEPluginImages.DESC_CALLEES);
	}

	@Override
	public ViewerComparator createViewerComparator() {
		return new FeatureViewerComparator();
	}

	@Override
	public IContentProvider createContentProvider() {
		return new FeatureTreeCalleesContentProvider(featureModelManager, productModelManager);
	}

	@Override
	public boolean isSupportsFilters() {
		return true;
	}

	@Override
	public boolean isSupportsPlugins() {
		return true;
	}

}