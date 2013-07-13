package uk.org.secondfiddle.pep.projects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.ui.internal.ide.dialogs.ProjectReferencePage;

public class ProjectTemplateManager implements ProjectTemplateProviderListener {

	private static ProjectTemplateManager INSTANCE;

	public static synchronized ProjectTemplateManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ProjectTemplateManager();
		}
		return INSTANCE;
	}

	private final Collection<ProjectTemplateProvider> providers = new ArrayList<ProjectTemplateProvider>();

	private final Collection<ProjectTemplate> templates = new HashSet<ProjectTemplate>();

	private ProjectTemplateManager() {
		this.providers.add(new WorkspaceProjectTemplateProvider(this));
	}

	public void shutdown() {
		for (ProjectTemplateProvider provider : providers) {
			provider.shutdown();
		}
	}

	@Override
	public void templateAdded(ProjectTemplate template) {
		System.err.println("added " + template.getName());
		templates.add(template);
	}

	@Override
	public void templateRemoved(ProjectTemplate template) {
		System.err.println("removed " + template.getName());
		templates.remove(template);
	}

}
