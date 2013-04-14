package uk.org.secondfiddle.pep.features.support;

import org.eclipse.pde.internal.core.FeatureModelManager;

import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureAndProductInput {

	private final FeatureModelManager featureModelManager;

	private final ProductModelManager productModelManager;

	private boolean includeProducts;

	private boolean includePlugins;

	public FeatureAndProductInput(FeatureModelManager featureModelManager, ProductModelManager productModelManager) {
		this.featureModelManager = featureModelManager;
		this.productModelManager = productModelManager;
	}

	public FeatureModelManager getFeatureModelManager() {
		return featureModelManager;
	}

	public ProductModelManager getProductModelManager() {
		return productModelManager;
	}

	public boolean isIncludeProducts() {
		return includeProducts;
	}

	public void setIncludeProducts(boolean includeProducts) {
		this.includeProducts = includeProducts;
	}

	public boolean isIncludePlugins() {
		return includePlugins;
	}

	public void setIncludePlugins(boolean includePlugins) {
		this.includePlugins = includePlugins;
	}

}
