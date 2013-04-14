package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureTreeCalleesContentProvider extends AbstractFeatureTreeContentProvider {

	public FeatureTreeCalleesContentProvider(FeatureModelManager featureModelManager,
			ProductModelManager productModelManager) {
		super(featureModelManager, productModelManager);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IProductModel) {
			IProductModel productModel = (IProductModel) parentElement;
			return productModel.getProduct().getFeatures();
		} else if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			Object[] features = featureModel.getFeature().getIncludedFeatures();

			if (!input.isIncludePlugins()) {
				return features;
			}

			Object[] plugins = featureModel.getFeature().getPlugins();
			Object[] all = new Object[features.length + plugins.length];
			System.arraycopy(features, 0, all, 0, features.length);
			System.arraycopy(plugins, 0, all, features.length, plugins.length);

			return all;
		} else if (parentElement instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) parentElement;
			IFeatureModel featureModel = featureModelManager.findFeatureModel(featureChild.getId());
			return getChildren(featureModel);
		} else if (parentElement instanceof IProductFeature) {
			IProductFeature productFeature = (IProductFeature) parentElement;
			IFeatureModel featureModel = featureModelManager.findFeatureModel(productFeature.getId());
			return getChildren(featureModel);
		}

		return new Object[0];
	}

}
