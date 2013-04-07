package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import uk.org.secondfiddle.pep.features.support.FeatureIndex;

@SuppressWarnings("restriction")
public class ChildFeatureViewerFilter extends ViewerFilter {

	private final FeatureIndex featureIndex;

	public ChildFeatureViewerFilter(FeatureIndex featureIndex) {
		this.featureIndex = featureIndex;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (parentElement instanceof FeatureModelManager) {
			IFeatureModel featureModel = (IFeatureModel) element;
			return featureIndex.getIncludingFeatures(featureModel.getFeature().getId()).isEmpty();
		}
		return true;
	}

}
