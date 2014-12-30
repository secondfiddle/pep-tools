package uk.org.secondfiddle.pep.products.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;

/**
 * This class alters the behaviour of
 * {@link WorkspaceProductModel#fireModelChanged(IModelChangedEvent)} to match
 * {@link WorkspaceFeatureModel}{@link #fireModelChanged(IModelChangedEvent)}.
 * 
 * Without this change the model's dirty status is not updated correctly - the
 * model becomes dirty just by opening the editor source page (though the editor
 * state is not updated to reflect this).
 */
@SuppressWarnings("restriction")
public class EditableWorkspaceProductModel extends WorkspaceProductModel {

	private static final long serialVersionUID = 1L;

	private boolean ignoreNextDirty = false;

	public EditableWorkspaceProductModel(IFile file) {
		super(file, true);
	}

	@Override
	public void setDirty(boolean dirty) {
		if (ignoreNextDirty) {
			ignoreNextDirty = false;
		} else {
			super.setDirty(dirty);
		}
	}

	@Override
	public void fireModelChanged(IModelChangedEvent event) {
		setDirty(event.getChangeType() != IModelChangedEvent.WORLD_CHANGED);
		ignoreNextDirty = true;
		super.fireModelChanged(event);
	}

}
