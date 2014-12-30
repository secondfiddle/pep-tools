package uk.org.secondfiddle.pep.features.explorer.viewer;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;
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

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FeatureAndProductInput) {
			FeatureAndProductInput input = (FeatureAndProductInput) inputElement;
			FeatureModelManager featureModelManager = input.getFeatureModelManager();
			IFeatureModel[] featureModels = featureModelManager.getWorkspaceModels();

			if (!input.isIncludeProducts()) {
				return featureModels;
			}

			ProductModelManager productModelManager = input.getProductModelManager();
			IProductModel[] productModels = getFeatureProducts(productModelManager);

			Object[] all = new Object[productModels.length + featureModels.length];
			System.arraycopy(productModels, 0, all, 0, productModels.length);
			System.arraycopy(featureModels, 0, all, productModels.length, featureModels.length);

			return all;
		}

		return new Object[0];
	}

	private IProductModel[] getFeatureProducts(ProductModelManager productModelManager) {
		IProductModel[] productModels = productModelManager.getModels();

		Collection<IProductModel> featureProducts = new ArrayList<IProductModel>();
		for (IProductModel productModel : productModels) {
			if (productModel.getProduct().useFeatures()) {
				featureProducts.add(productModel);
			}
		}

		return featureProducts.toArray(new IProductModel[featureProducts.size()]);
	}

}
