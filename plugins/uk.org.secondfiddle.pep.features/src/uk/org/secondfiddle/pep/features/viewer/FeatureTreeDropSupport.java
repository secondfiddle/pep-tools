package uk.org.secondfiddle.pep.features.viewer;

import java.util.Collection;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TransferData;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.PluginSupport;
import uk.org.secondfiddle.pep.features.support.ProductSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class FeatureTreeDropSupport extends ViewerDropAdapter {

	public FeatureTreeDropSupport(Viewer viewer) {
		super(viewer);
		setFeedbackEnabled(false);
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
		if (validateDrop(selection, getCurrentTarget())) {
			overrideOperation(DND.DROP_COPY);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean performDrop(Object data) {
		return performDrop((ISelection) data, getCurrentTarget());
	}

	private static IFeatureModel getValidFeatureTarget(Object target) {
		return FeatureSupport.toEditableFeatureModel(target);
	}

	private static IProductModel getValidProductTarget(Object target) {
		return ProductSupport.toEditableProductModel(target);
	}

	private static Collection<IFeatureModel> getValidFeatureSources(Object data) {
		return FeatureSupport.toFeatureModels(data);
	}

	private static Collection<IPluginModelBase> getValidPluginSources(Object data) {
		return PluginSupport.toPluginModels(data);
	}

	public static boolean validateDrop(ISelection source, Object target) {
		if (getValidFeatureTarget(target) != null) {
			boolean features = !getValidFeatureSources(source).isEmpty();
			boolean plugins = !getValidPluginSources(source).isEmpty();
			return features || plugins;
		}

		if (getValidProductTarget(target) != null) {
			boolean features = !getValidFeatureSources(source).isEmpty();
			boolean plugins = !getValidPluginSources(source).isEmpty();
			return features && !plugins;
		}

		return false;
	}

	public static boolean performDrop(ISelection source, Object target) {
		IFeatureModel featureTarget = getValidFeatureTarget(target);
		if (featureTarget != null) {
			Collection<IFeatureModel> featureSources = getValidFeatureSources(source);
			featureSources.remove(featureTarget);
			RefactoringSupport.addIncludedFeatures(featureTarget, featureSources);

			Collection<IPluginModelBase> pluginSources = getValidPluginSources(source);
			RefactoringSupport.addIncludedPlugins(featureTarget, pluginSources);

			return true;
		}

		IProductModel productTarget = getValidProductTarget(target);
		if (productTarget != null) {
			Collection<IFeatureModel> featureSources = getValidFeatureSources(source);
			RefactoringSupport.addProductFeatures(productTarget, featureSources);

			return true;
		}

		return false;
	}

}
