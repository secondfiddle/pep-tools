package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;

@SuppressWarnings("restriction")
public class FeatureChildViewerFilter extends ViewerFilter {

	private final FeatureIndex featureIndex;

	public FeatureChildViewerFilter(FeatureIndex featureIndex) {
		this.featureIndex = featureIndex;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (parentElement instanceof FeatureAndProductInput && element instanceof IFeatureModel) {
			IFeatureModel featureModel = (IFeatureModel) element;
			FeatureAndProductInput input = (FeatureAndProductInput) parentElement;
			boolean showProducts = input.isIncludeProducts();

			String featureId = featureModel.getFeature().getId();
			boolean includedInFeature = !featureIndex.getIncludingFeatures(featureId).isEmpty();
			boolean includedInProduct = !featureIndex.getIncludingProducts(featureId).isEmpty();

			return !includedInFeature && (!showProducts || !includedInProduct);
		}

		return true;
	}

}
