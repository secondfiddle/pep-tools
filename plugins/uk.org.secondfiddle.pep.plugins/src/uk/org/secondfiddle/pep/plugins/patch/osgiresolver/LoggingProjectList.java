package uk.org.secondfiddle.pep.plugins.patch.osgiresolver;

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaProject;

import uk.org.secondfiddle.pep.plugins.PatchActivator;

class LoggingProjectList extends ArrayList<IJavaProject> {

	private static final long serialVersionUID = 1L;

	@Override
	public boolean add(IJavaProject javaProject) {
		String projectName = javaProject.getProject().getName();
		PatchActivator.logInfo("Adding to projects-to-update list: " + projectName);
		return super.add(javaProject);
	}

	@Override
	public void clear() {
		PatchActivator.logInfo("Clearing projects-to-update list");
		super.clear();
	}

}