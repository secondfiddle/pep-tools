package uk.org.secondfiddle.pep.products.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.util.CoreUtility;

import uk.org.secondfiddle.pep.products.ProductNature;

@SuppressWarnings("restriction")
public class ProductNatureAddAction extends Action {

	private final ISelection selection;

	public ProductNatureAddAction(ISelection selection) {
		super("Add Product Nature");
		this.selection = selection;
		setEnabled(!getRelevantProjects().isEmpty());
	}

	@Override
	public void run() {
		try {
			for (IProject project : getRelevantProjects()) {
				CoreUtility.addNatureToProject(project, ProductNature.ID, new NullProgressMonitor());
			}
		} catch (CoreException e) {
			throw new RuntimeException("Failed to add nature", e);
		}
	}

	private Collection<IProject> getRelevantProjects() {
		if (!(selection instanceof IStructuredSelection)) {
			return Collections.emptySet();
		}

		Collection<IProject> projects = new HashSet<IProject>();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Object selected : structuredSelection.toList()) {
			if (selected instanceof IProductModel) {
				selected = ((IProductModel) selected).getUnderlyingResource();
			}
			if (selected instanceof IResource) {
				IProject project = ((IResource) selected).getProject();
				if (!hasNature(project, ProductNature.ID)) {
					projects.add(project);
				}
			}
		}

		return projects;
	}

	private boolean hasNature(IProject project, String natureId) {
		try {
			return project.hasNature(natureId);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
