package uk.org.secondfiddle.pep.products.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.ui.IActionDelegate;

import uk.org.secondfiddle.pep.products.ProductNature;

@SuppressWarnings("restriction")
public class ProductNatureAddAction implements IActionDelegate {

	private IStructuredSelection selection = null;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			this.selection = (IStructuredSelection) selection;
		}
	}

	@Override
	public void run(IAction action) {
		IProject project = getSelectedProject();
		if (project == null) {
			return;
		}

		try {
			CoreUtility.addNatureToProject(project, ProductNature.ID, new NullProgressMonitor());
		} catch (CoreException e) {
			throw new RuntimeException("Failed to add nature", e);
		}
	}

	private IProject getSelectedProject() {
		if (selection == null || selection.isEmpty()) {
			return null;
		}

		Object firstElement = selection.getFirstElement();
		if (firstElement instanceof IResource) {
			return ((IResource) firstElement).getProject();
		}

		return null;
	}

}
