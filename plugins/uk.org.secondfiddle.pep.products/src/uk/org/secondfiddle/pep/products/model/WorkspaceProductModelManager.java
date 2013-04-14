package uk.org.secondfiddle.pep.products.model;

import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.pde.core.IModel;
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
	@SuppressWarnings("unchecked")
	protected void createModel(IProject project, boolean notify) {
		for (IFile product : ProductSupport.getProductFiles(project)) {
			IProductModel model = new WorkspaceProductModel(product, true);
			loadModel(model, false);

			if (fModels == null) {
				fModels = new HashMap<IProject, IModel>();
			}

			fModels.put(project, model);

			if (notify) {
				addChange(model, IModelProviderEvent.MODELS_ADDED);
			}
		}
	}

	@Override
	protected void handleFileDelta(IResourceDelta delta) {
		IFile file = (IFile) delta.getResource();
		IProject project = file.getProject();

		for (IFile product : ProductSupport.getProductFiles(project)) {
			Object model = getModel(project);
			int kind = delta.getKind();
			if (kind == IResourceDelta.REMOVED && model != null) {
				removeModel(project);
			} else if (kind == IResourceDelta.ADDED || model == null) {
				createModel(product.getProject(), true);
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
		return (IProductModel[]) fModels.values().toArray(new IProductModel[fModels.size()]);
	}

	protected IProductModel getProductModel(IProject project) {
		return (IProductModel) getModel(project);
	}

}
