package uk.org.secondfiddle.pep.features.viewer;

import java.util.Collection;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.swt.dnd.TransferData;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

public class FeatureTreeDropSupport extends ViewerDropAdapter {

	public FeatureTreeDropSupport(Viewer viewer) {
		super(viewer);
	}

	@Override
	public boolean performDrop(Object data) {
		IFeatureModel target = getValidTarget();
		if (target == null) {
			return false;
		}

		Collection<IFeatureModel> sources = getValidSources(data);
		for (IFeatureModel source : sources) {
			RefactoringSupport.addIncludedFeature(target, source);
		}

		return true;
	}

	private IFeatureModel getValidTarget() {
		return FeatureSupport.toEditableFeatureModel(getCurrentTarget());
	}

	private Collection<IFeatureModel> getValidSources(Object data) {
		return FeatureSupport.toFeatureModels(data);
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return (getValidTarget() != null);
	}

}
