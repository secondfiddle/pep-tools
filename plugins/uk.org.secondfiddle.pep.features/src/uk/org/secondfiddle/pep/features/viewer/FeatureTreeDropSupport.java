package uk.org.secondfiddle.pep.features.viewer;

import java.util.Collection;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.swt.dnd.TransferData;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.PluginSupport;
import uk.org.secondfiddle.pep.features.support.ProductSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class FeatureTreeDropSupport extends ViewerDropAdapter {

	public FeatureTreeDropSupport(Viewer viewer) {
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data) {
		IFeatureModel featureTarget = getValidFeatureTarget();
		if (featureTarget != null) {
			Collection<IFeatureModel> featureSources = getValidFeatureSources(data);
			featureSources.remove(featureTarget);
			RefactoringSupport.addIncludedFeatures(featureTarget, featureSources);

			Collection<IPluginModelBase> pluginSources = getValidPluginSources(data);
			RefactoringSupport.addIncludedPlugins(featureTarget, pluginSources);
		}

		IProductModel productTarget = getValidProductTarget();
		if (productTarget != null) {
			Collection<IFeatureModel> featureSources = getValidFeatureSources(data);
			RefactoringSupport.addProductFeatures(productTarget, featureSources);
		}

		return false;
	}

	private IFeatureModel getValidFeatureTarget() {
		return FeatureSupport.toEditableFeatureModel(getCurrentTarget());
	}

	private IProductModel getValidProductTarget() {
		return ProductSupport.toEditableProductModel(getCurrentTarget());
	}

	private Collection<IFeatureModel> getValidFeatureSources(Object data) {
		return FeatureSupport.toFeatureModels(data);
	}

	private Collection<IPluginModelBase> getValidPluginSources(Object data) {
		return PluginSupport.toPluginModels(data);
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		ISelection selection = getViewer().getSelection();

		if (getValidFeatureTarget() != null) {
			boolean features = !getValidFeatureSources(selection).isEmpty();
			boolean plugins = !getValidPluginSources(selection).isEmpty();
			return features || plugins;
		}

		if (getValidProductTarget() != null) {
			boolean features = !getValidFeatureSources(selection).isEmpty();
			boolean plugins = !getValidPluginSources(selection).isEmpty();
			return features && !plugins;
		}

		return false;
	}

}
