package uk.org.secondfiddle.pep.features.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.products.model.IProductModelListener;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureIndex implements IFeatureModelListener, IProductModelListener {

	private final Map<String, Collection<IFeatureModel>> includingFeatures = new HashMap<String, Collection<IFeatureModel>>();

	private final Map<String, Collection<IProductModel>> includingProducts = new HashMap<String, Collection<IProductModel>>();

	private final FeatureModelManager featureModelManager;

	private final ProductModelManager productModelManager;

	public FeatureIndex(FeatureModelManager featureModelManager, ProductModelManager productModelManager) {
		this.featureModelManager = featureModelManager;
		this.featureModelManager.addFeatureModelListener(this);
		this.productModelManager = productModelManager;
		this.productModelManager.addProductModelListener(this);
		reIndex();
	}

	public Collection<IFeatureModel> getIncludingFeatures(String childId) {
		Collection<IFeatureModel> parents = includingFeatures.get(childId);
		if (parents == null) {
			return Collections.emptySet();
		} else {
			return parents;
		}
	}

	public Collection<IProductModel> getIncludingProducts(String featureId) {
		Collection<IProductModel> products = includingProducts.get(featureId);
		if (products == null) {
			return Collections.emptySet();
		} else {
			return products;
		}
	}

	public void dispose() {
		this.featureModelManager.removeFeatureModelListener(this);
		this.productModelManager.removeProductModelListener(this);
	}

	private void reIndex() {
		includingFeatures.clear();
		for (IFeatureModel parentModel : featureModelManager.getWorkspaceModels()) {
			for (IFeatureChild child : parentModel.getFeature().getIncludedFeatures()) {
				IFeatureModel childModel = featureModelManager.findFeatureModel(child.getId());
				if (childModel != null) {
					index(childModel, parentModel);
				}
			}
		}

		includingProducts.clear();
		for (IProductModel productModel : productModelManager.getModels()) {
			for (IProductFeature productFeature : productModel.getProduct().getFeatures()) {
				IFeatureModel featureModel = featureModelManager.findFeatureModel(productFeature.getId());
				if (featureModel != null) {
					index(featureModel, productModel);
				}
			}
		}
	}

	private void index(IFeatureModel childModel, IFeatureModel parentModel) {
		String childId = childModel.getFeature().getId();

		Collection<IFeatureModel> parents = includingFeatures.get(childId);
		if (parents == null) {
			parents = new HashSet<IFeatureModel>();
			includingFeatures.put(childId, parents);
		}

		parents.add(parentModel);
	}

	private void index(IFeatureModel childModel, IProductModel productModel) {
		String featureId = childModel.getFeature().getId();

		Collection<IProductModel> products = includingProducts.get(featureId);
		if (products == null) {
			products = new HashSet<IProductModel>();
			includingProducts.put(featureId, products);
		}

		products.add(productModel);
	}

	@Override
	public void modelsChanged(IFeatureModelDelta delta) {
		reIndex();
	}

	@Override
	public void modelsChanged() {
		reIndex();
	}

}
