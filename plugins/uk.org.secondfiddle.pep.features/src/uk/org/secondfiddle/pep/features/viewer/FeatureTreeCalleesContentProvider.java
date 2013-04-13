package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

@SuppressWarnings("restriction")
public class FeatureTreeCalleesContentProvider extends AbstractFeatureTreeContentProvider {

	public FeatureTreeCalleesContentProvider(FeatureModelManager featureModelManager) {
		super(featureModelManager);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			Object[] features = featureModel.getFeature().getIncludedFeatures();
			Object[] plugins = featureModel.getFeature().getPlugins();
			Object[] all = new Object[features.length + plugins.length];
			System.arraycopy(features, 0, all, 0, features.length);
			System.arraycopy(plugins, 0, all, features.length, plugins.length);
			return all;
		} else if (parentElement instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) parentElement;
			IFeatureModel featureModel = featureModelManager.findFeatureModel(featureChild.getId());
			return getChildren(featureModel);
		}

		return new Object[0];
	}

}
