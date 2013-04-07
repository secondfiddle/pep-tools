package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.internal.ui.PDEPluginImages;

@SuppressWarnings("restriction")
public class CollapseAllAction extends Action {

	private final TreeViewer viewer;

	public CollapseAllAction(TreeViewer viewer) {
		this.viewer = viewer;

		setToolTipText("Collapse All");
		setImageDescriptor(PDEPluginImages.DESC_COLLAPSE_ALL);
	}

	public void run() {
		viewer.collapseAll();
	}

}