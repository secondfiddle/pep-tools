package uk.org.secondfiddle.pep.features.explorer.viewer;

import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;

@SuppressWarnings("restriction")
public class FeatureViewerComparator extends ViewerComparator {

	@Override
	public int category(Object element) {
		if (element instanceof IFeatureChild || element instanceof IProductFeature) {
			element = FeatureSupport.toFeatureModel(element);
			if (element == null) {
				return 5;
			}
		}

		if (element instanceof IProductModel) {
			return 0;
		} else if (element instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) element;
			boolean editable = featureModel.isEditable();
			boolean hasChildren = featureModel.getFeature().getIncludedFeatures().length > 0;

			if (hasChildren && editable) {
				return 1;
			} else if (hasChildren) {
				return 2;
			} else if (editable) {
				return 3;
			} else {
				return 4;
			}
		} else if (element instanceof IFeaturePlugin) {
			return 6;
		}

		return 7;
	}

}
