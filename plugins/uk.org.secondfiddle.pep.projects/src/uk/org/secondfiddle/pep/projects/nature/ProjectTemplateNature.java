package uk.org.secondfiddle.pep.projects.nature;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import uk.org.secondfiddle.pep.projects.model.impl.workspace.WorkspaceProjectTemplateProvider;

/**
 * This nature simply marks a project as "of interest" to the
 * {@link WorkspaceProjectTemplateProvider}.
 */
public class ProjectTemplateNature implements IProjectNature {

	public static final String ID = ProjectTemplateNature.class.getName();

	private IProject project;

	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

}
