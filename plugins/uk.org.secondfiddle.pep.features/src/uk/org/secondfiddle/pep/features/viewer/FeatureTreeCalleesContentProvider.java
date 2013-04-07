package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

public class FeatureTreeCalleesContentProvider extends AbstractFeatureTreeContentProvider {

	public FeatureTreeCalleesContentProvider(FeatureModelManager featureModelManager) {
		super(featureModelManager);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			return featureModel.getFeature().getIncludedFeatures();
		} else if (parentElement instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) parentElement;
			IFeatureModel featureModel = featureModelManager.findFeatureModel(featureChild.getId());
			return getChildren(featureModel);
		}

		return new Object[0];
	}

}
