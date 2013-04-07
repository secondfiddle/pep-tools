package uk.org.secondfiddle.pep.features;

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
			IFeatureChild[] includedFeatures = featureModel.getFeature().getIncludedFeatures();

			Object[] children = new Object[includedFeatures.length];
			for (int i = 0; i < includedFeatures.length; i++) {
				String featureId = includedFeatures[i].getId();
				IFeatureModel includedFeatureModel = featureModelManager.findFeatureModel(featureId);

				// handle non-existent features
				if (includedFeatureModel == null) {
					children[i] = includedFeatures[i];
				} else {
					children[i] = includedFeatureModel;
				}
			}

			return children;
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
