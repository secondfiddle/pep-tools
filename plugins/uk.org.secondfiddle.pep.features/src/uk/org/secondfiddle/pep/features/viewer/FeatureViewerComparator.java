package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;

@SuppressWarnings("restriction")
public class FeatureViewerComparator extends ViewerComparator {

	@Override
	public int category(Object element) {
		if (element instanceof IFeatureChild) {
			element = FeatureSupport.toFeatureModel(element);
			if (element == null) {
				return 4;
			}
		}

		if (element instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) element;
			boolean editable = featureModel.isEditable();
			boolean hasChildren = featureModel.getFeature().getIncludedFeatures().length > 0;

			if (hasChildren && editable) {
				return 0;
			} else if (hasChildren) {
				return 1;
			} else if (editable) {
				return 2;
			} else {
				return 3;
			}
		} else if (element instanceof IFeaturePlugin) {
			return 5;
		}

		return 6;
	}

}
