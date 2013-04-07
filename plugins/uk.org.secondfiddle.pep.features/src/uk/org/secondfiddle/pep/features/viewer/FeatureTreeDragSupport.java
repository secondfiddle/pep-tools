package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class FeatureTreeDragSupport implements DragSourceListener {

	private final ISelectionProvider selectionProvider;

	public FeatureTreeDragSupport(ISelectionProvider selectionProvider) {
		this.selectionProvider = selectionProvider;
	}

	@Override
	public void dragFinished(DragSourceEvent event) {
		LocalSelectionTransfer.getTransfer().setSelection(null);
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(0);
	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		event.data = LocalSelectionTransfer.getTransfer().getSelection();
	}

	@Override
	public void dragStart(DragSourceEvent event) {
		ISelection selection = selectionProvider.getSelection();
		LocalSelectionTransfer.getTransfer().setSelection(selection);
		LocalSelectionTransfer.getTransfer().setSelectionSetTime(event.time & 0xFFFF);
		event.doit = isDraggable(selection);
	}

	private boolean isDraggable(ISelection selection) {
		return true;
	}

}
