package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

public class FeatureTreeCallersContentProvider extends AbstractFeatureTreeContentProvider {

	private final FeatureIndex featureIndex;

	public FeatureTreeCallersContentProvider(FeatureModelManager featureModelManager, FeatureIndex featureIndex) {
		super(featureModelManager);
		this.featureIndex = featureIndex;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			String featureId = featureModel.getFeature().getId();
			return featureIndex.getIncludingFeatures(featureId).toArray();
		}

		return new Object[0];
	}

}
