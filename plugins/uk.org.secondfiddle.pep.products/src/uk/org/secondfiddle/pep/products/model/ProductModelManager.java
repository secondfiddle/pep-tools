package uk.org.secondfiddle.pep.products.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.IModelProviderEvent;
import org.eclipse.pde.core.IModelProviderListener;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

@SuppressWarnings("restriction")
public class ProductModelManager {

	private static final ProductModelManager INSTANCE = new ProductModelManager();

	public static synchronized ProductModelManager getInstance() {
		return INSTANCE;
	}

	private final Map<String, IProductModel> productModels = new HashMap<String, IProductModel>();

	private final Collection<IProductModelListener> listeners = new ArrayList<IProductModelListener>();

	private final WorkspaceProductModelManager workspaceProductModelManager;

	private ProductModelManager() {
		this.workspaceProductModelManager = new WorkspaceProductModelManager();
		this.workspaceProductModelManager.addModelProviderListener(new IModelProviderListener() {
			@Override
			public void modelsChanged(IModelProviderEvent event) {
				handleModelProviderChange(event);
			}
		});
	}

	private void handleModelProviderChange(IModelProviderEvent event) {
		for (IModel model : event.getRemovedModels()) {
			IProductModel productModel = (IProductModel) model;
			productModels.remove(productModel.getProduct().getId());
		}
		for (IModel model : event.getAddedModels()) {
			IProductModel productModel = (IProductModel) model;
			productModels.put(productModel.getProduct().getId(), productModel);
		}
		for (IModel model : event.getChangedModels()) {
			IProductModel productModel = (IProductModel) model;
			productModels.put(productModel.getProduct().getId(), productModel);
		}
		for (IProductModelListener listener : listeners) {
			listener.modelsChanged();
		}
	}

	public IProductModel findProductModel(String id) {
		return productModels.get(id);
	}

	public IProductModel[] getModels() {
		return workspaceProductModelManager.getProductModels();
	}

	public void addProductModelListener(IProductModelListener listener) {
		listeners.add(listener);
	}

	public void removeProductModelListener(IProductModelListener listener) {
		listeners.remove(listener);
	}

	public synchronized void shutdown() {
		workspaceProductModelManager.shutdown();
	}

}
