package uk.org.secondfiddle.pep.features.viewer;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;
import uk.org.secondfiddle.pep.products.model.IProductModelListener;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public abstract class AbstractFeatureTreeContentProvider implements ITreeContentProvider, IFeatureModelListener,
		IProductModelListener {

	protected final FeatureModelManager featureModelManager;

	protected final ProductModelManager productModelManager;

	protected FeatureAndProductInput input;

	private Viewer viewer;

	public AbstractFeatureTreeContentProvider(FeatureModelManager featureModelManager,
			ProductModelManager productModelManager) {
		this.featureModelManager = featureModelManager;
		this.featureModelManager.addFeatureModelListener(this);
		this.productModelManager = productModelManager;
		this.productModelManager.addProductModelListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;

		if (newInput instanceof FeatureAndProductInput) {
			this.input = (FeatureAndProductInput) newInput;
		}
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

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	@Override
	public void dispose() {
		featureModelManager.removeFeatureModelListener(this);
		productModelManager.removeProductModelListener(this);
	}

	@Override
	public void modelsChanged(IFeatureModelDelta delta) {
		refreshViewer();
	}

	@Override
	public void modelsChanged() {
		refreshViewer();
	}

	private void refreshViewer() {
		if (viewer.getControl().isDisposed()) {
			return;
		}

		viewer.getControl().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer.getControl().isDisposed()) {
					return;
				}
				viewer.refresh();
			}
		});
	}

}
