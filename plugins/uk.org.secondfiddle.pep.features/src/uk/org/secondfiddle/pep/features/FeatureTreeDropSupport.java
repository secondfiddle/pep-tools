package uk.org.secondfiddle.pep.features;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.swt.dnd.TransferData;

import uk.org.secondfiddle.pep.features.refactor.RefactoringSupport;

public class FeatureTreeDropSupport extends ViewerDropAdapter {

	protected FeatureTreeDropSupport(Viewer viewer) {
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
		Object currentTarget = getCurrentTarget();
		if (currentTarget instanceof IFeatureModel && currentTarget instanceof IEditableModel) {
			IEditableModel editable = (IEditableModel) currentTarget;
			if (editable.isEditable()) {
				return (IFeatureModel) currentTarget;
			}
		}
		return null;
	}

	private Collection<IFeatureModel> getValidSources(Object data) {
		IStructuredSelection selection = (IStructuredSelection) data;
		Collection<IFeatureModel> sources = new ArrayList<IFeatureModel>();

		for (Object selectedItem : selection.toList()) {
			if (selectedItem instanceof IFeatureModel) {
				sources.add((IFeatureModel) selectedItem);
			}
		}

		return sources;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		return (getValidTarget() != null);
	}

}
