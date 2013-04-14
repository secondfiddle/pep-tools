package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeCallersContentProvider;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class ShowCallersContentProviderAction extends ContentProviderAction {

	private final FeatureIndex featureIndex;

	public ShowCallersContentProviderAction(ConfigurableViewer configurableViewer,
			FeatureModelManager featureModelManager, ProductModelManager productModelManager, FeatureIndex featureIndex) {
		super(configurableViewer, featureModelManager, productModelManager);
		this.featureIndex = featureIndex;

		setToolTipText("Show Features Including Each Feature");
		setImageDescriptor(PDEPluginImages.DESC_CALLERS);
	}

	@Override
	public ViewerComparator createViewerComparator() {
		return new ViewerComparator();
	}

	@Override
	public IContentProvider createContentProvider() {
		return new FeatureTreeCallersContentProvider(featureModelManager, productModelManager, featureIndex);
	}

	@Override
	public boolean isSupportsFilters() {
		return false;
	}

	@Override
	public boolean isSupportsPlugins() {
		return false;
	}

}