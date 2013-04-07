package uk.org.secondfiddle.pep.features;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

public class FeatureViewerComparator extends ViewerComparator {

	@Override
	public int category(Object element) {
		if (element instanceof IFeatureChild) {
			return 3;
		}

		IFeatureModel featureModel = (IFeatureModel) element;
		if (featureModel.getFeature().getIncludedFeatures().length > 0) {
			return 0;
		} else if (featureModel.isEditable()) {
			return 1;
		} else {
			return 2;
		}
	}

}
