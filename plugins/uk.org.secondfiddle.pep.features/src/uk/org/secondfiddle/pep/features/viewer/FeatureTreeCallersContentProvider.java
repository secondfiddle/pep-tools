package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureTreeCallersContentProvider extends AbstractFeatureTreeContentProvider {

	private final FeatureIndex featureIndex;

	public FeatureTreeCallersContentProvider(FeatureModelManager featureModelManager,
			ProductModelManager productModelManager, FeatureIndex featureIndex) {
		super(featureModelManager, productModelManager);
		this.featureIndex = featureIndex;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			String featureId = featureModel.getFeature().getId();
			Object[] features = featureIndex.getIncludingFeatures(featureId).toArray();

			if (!input.isIncludeProducts()) {
				return features;
			}

			Object[] products = featureIndex.getIncludingProducts(featureId).toArray();
			Object[] all = new Object[features.length + products.length];
			System.arraycopy(features, 0, all, 0, features.length);
			System.arraycopy(products, 0, all, features.length, products.length);

			return all;
		}

		return new Object[0];
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FeatureAndProductInput) {
			FeatureAndProductInput input = (FeatureAndProductInput) inputElement;
			FeatureModelManager featureModelManager = input.getFeatureModelManager();
			return featureModelManager.getWorkspaceModels();
		}

		return new Object[0];
	}

}
