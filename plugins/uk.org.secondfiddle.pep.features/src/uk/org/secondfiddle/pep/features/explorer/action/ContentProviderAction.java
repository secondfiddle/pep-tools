package uk.org.secondfiddle.pep.features.explorer.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.FeatureModelManager;

import uk.org.secondfiddle.pep.features.explorer.ConfigurableViewer;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public abstract class ContentProviderAction extends Action {

	private final ConfigurableViewer configurableViewer;

	protected final FeatureModelManager featureModelManager;

	protected final ProductModelManager productModelManager;

	public ContentProviderAction(ConfigurableViewer configurableViewer, FeatureModelManager featureModelManager,
			ProductModelManager productModelManager) {
		super("", AS_RADIO_BUTTON);
		this.configurableViewer = configurableViewer;
		this.featureModelManager = featureModelManager;
		this.productModelManager = productModelManager;
	}

	public abstract ViewerComparator createViewerComparator();

	public abstract IContentProvider createContentProvider();

	public void run() {
		configurableViewer.setContentProvider(this);
	}

	public abstract boolean isSupportsFilters();

	public abstract boolean isSupportsPlugins();

}