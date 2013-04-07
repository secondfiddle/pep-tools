package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

public class FeatureTreeContentProvider implements ITreeContentProvider, IFeatureModelListener {

	private final FeatureModelManager featureModelManager;

	private Viewer viewer;

	public FeatureTreeContentProvider(FeatureModelManager featureModelManager) {
		this.featureModelManager = featureModelManager;
		this.featureModelManager.addFeatureModelListener(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof FeatureModelManager) {
			FeatureModelManager featureModelManager = (FeatureModelManager) inputElement;
			return featureModelManager.getWorkspaceModels();
		}

		return new Object[0];
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) parentElement;
			return featureModel.getFeature().getIncludedFeatures();
		} else if (parentElement instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) parentElement;
			IFeatureModel featureModel = featureModelManager.findFeatureModel(featureChild.getId());
			return getChildren(featureModel);
		}

		return new Object[0];
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
	}

	@Override
	public void modelsChanged(IFeatureModelDelta delta) {
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
