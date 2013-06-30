package uk.org.secondfiddle.pep.features.action;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.ResourceTransfer;

import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;

@SuppressWarnings("restriction")
public class FeatureAndPluginPasteAction extends Action {

	private final ISelectionProvider selectionProvider;

	private final Clipboard clipboard;

	public FeatureAndPluginPasteAction(ISelectionProvider selectionProvider, Clipboard clipboard) {
		super("Paste", Workbench.getInstance().getSharedImages().getImageDescriptor(SharedImages.IMG_TOOL_PASTE));
		this.selectionProvider = selectionProvider;
		this.clipboard = clipboard;
	}

	@Override
	public void runWithEvent(Event event) {
		Object source = clipboard.getContents(ResourceTransfer.getInstance());
		Object target = getSingleTarget();
		if (source instanceof IResource[] && target != null) {
			FeatureTreeDropSupport.performDrop(toSelection(source), target);
		}
	}

	public void revalidate() {
		Object source = clipboard.getContents(ResourceTransfer.getInstance());
		Object target = getSingleTarget();
		if (source instanceof IResource[] && target != null) {
			setEnabled(FeatureTreeDropSupport.validateDrop(toSelection(source), target));
		} else {
			setEnabled(false);
		}
	}

	private ISelection toSelection(Object source) {
		IResource[] resources = (IResource[]) source;
		return new StructuredSelection(resources);
	}

	private Object getSingleTarget() {
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				return structuredSelection.getFirstElement();
			}
		}
		return null;
	}

}
