package uk.org.secondfiddle.pep.products.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.pde.core.IModelProviderEvent;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.WorkspaceFeatureModelManager;
import org.eclipse.pde.internal.core.WorkspaceModelManager;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;

import uk.org.secondfiddle.pep.products.impl.ProductSupport;

/**
 * Part of this class is adapted from {@link WorkspaceFeatureModelManager}.
 */
@SuppressWarnings("restriction")
public class WorkspaceProductModelManager extends WorkspaceModelManager {

	@Override
	protected boolean isInterestingProject(IProject project) {
		return ProductSupport.isInterestingProject(project);
	}

	@Override
	protected void createModel(IProject project, boolean notify) {
		for (IFile product : ProductSupport.getProductFiles(project)) {
			createSingleModel(project, product, notify);
		}
	}

	@SuppressWarnings("unchecked")
	private void createSingleModel(IProject project, IFile product, boolean notify) {
		IProductModel model = new WorkspaceProductModel(product, true);
		loadModel(model, false);

		if (fModels == null) {
			fModels = new HashMap<IProject, Collection<IProductModel>>();
		}

		Collection<IProductModel> models = (Collection<IProductModel>) fModels.get(project);
		if (models == null) {
			models = new ArrayList<IProductModel>();
			fModels.put(project, models);
		}

		models.add(model);

		if (notify) {
			addChange(model, IModelProviderEvent.MODELS_ADDED);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object removeModel(IProject project) {
		Object models = fModels != null ? fModels.remove(project) : null;
		if (models != null) {
			for (IProductModel model : (Collection<IProductModel>) models) {
				addChange(model, IModelProviderEvent.MODELS_REMOVED);
			}
		}
		return models;
	}

	@SuppressWarnings("unchecked")
	private Object removeSingleModel(IProject project, Object model) {
		Object models = fModels != null ? fModels.get(project) : null;
		if (models != null) {
			Collection<IProductModel> modelsCollection = (Collection<IProductModel>) models;
			if (modelsCollection.remove(model)) {
				addChange(model, IModelProviderEvent.MODELS_REMOVED);
			}
			if (modelsCollection.isEmpty()) {
				fModels.remove(project);
			}
		}
		return models;
	}

	@SuppressWarnings("unchecked")
	private IProductModel getSingleModel(IFile productFile) {
		Collection<IProductModel> models = (Collection<IProductModel>) getModel(productFile.getProject());
		if (models != null) {
			for (IProductModel model : models) {
				if (model.getUnderlyingResource().equals(productFile)) {
					return model;
				}
			}
		}
		return null;
	}

	@Override
	protected void handleFileDelta(IResourceDelta delta) {
		IFile file = (IFile) delta.getResource();
		IProject project = file.getProject();

		if (ProductSupport.isProductFile(file)) {
			Object model = getSingleModel(file);
			int kind = delta.getKind();
			if (kind == IResourceDelta.REMOVED && model != null) {
				removeSingleModel(project, model);
			} else if (kind == IResourceDelta.ADDED && model == null) {
				createSingleModel(project, file, true);
			} else if (kind == IResourceDelta.CHANGED && (IResourceDelta.CONTENT & delta.getFlags()) != 0) {
				loadModel((IProductModel) model, true);
				addChange(model, IModelProviderEvent.MODELS_CHANGED);
			}
		}
	}

	@Override
	protected void addListeners() {
		int event = IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_CHANGE;
		PDECore.getWorkspace().addResourceChangeListener(this, event);
	}

	@Override
	protected void removeListeners() {
		PDECore.getWorkspace().removeResourceChangeListener(this);
		super.removeListeners();
	}

	@SuppressWarnings("unchecked")
	protected IProductModel[] getProductModels() {
		initialize();

		Collection<IProductModel> flattenedModels = new ArrayList<IProductModel>();
		for (Collection<IProductModel> models : (Collection<Collection<IProductModel>>) fModels.values()) {
			flattenedModels.addAll(models);
		}

		return (IProductModel[]) flattenedModels.toArray(new IProductModel[flattenedModels.size()]);
	}

}
