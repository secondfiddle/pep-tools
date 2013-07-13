package uk.org.secondfiddle.pep.projects;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class WorkspaceProjectTemplateProvider implements ProjectTemplateProvider, IResourceChangeListener,
		IResourceDeltaVisitor, IResourceVisitor {

	private static final String TEMPLATE_MANIFEST = "template.mf";

	private static final String WORKSPACE_TEMPLATES_KEY = "workspaceTemplates";

	private final Map<String, Collection<ProjectTemplate>> templates = new HashMap<String, Collection<ProjectTemplate>>();

	private final ProjectTemplateProviderListener listener;

	public WorkspaceProjectTemplateProvider(ProjectTemplateProviderListener listener) {
		this.listener = listener;
		initialise();
	}

	private void addTemplate(ProjectTemplate template) {
		Collection<ProjectTemplate> projectTemplates = this.templates.get(template.getProjectName());
		if (projectTemplates == null) {
			projectTemplates = new HashSet<ProjectTemplate>();
			this.templates.put(template.getProjectName(), projectTemplates);
		}
		projectTemplates.add(template);

		this.listener.templateAdded(template);
	}

	private void removeTemplate(ProjectTemplate template) {
		Collection<ProjectTemplate> projectTemplates = this.templates.get(template.getProjectName());
		if (projectTemplates != null) {
			projectTemplates.remove(template);
			if (projectTemplates.isEmpty()) {
				this.templates.remove(template.getProjectName());
			}
		}

		this.listener.templateRemoved(template);
	}

	private void removeTemplates(String project) {
		Collection<ProjectTemplate> projectTemplates = this.templates.remove(project);
		if (projectTemplates != null) {
			for (ProjectTemplate template : projectTemplates) {
				this.listener.templateRemoved(template);
			}
		}
	}

	private void removeTemplate(String project, String location) {
		Collection<ProjectTemplate> projectTemplates = this.templates.get(project);
		if (projectTemplates != null) {
			for (ProjectTemplate template : projectTemplates) {
				if (template.getLocation().equals(location)) {
					removeTemplate(template);
				}
			}
		}
	}

	private void initialise() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			workspaceRoot.accept(this);
		} catch (CoreException e1) {
			throw new RuntimeException(e1);
		}

		int event = IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.POST_CHANGE;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, event);
	}

	@Override
	public void shutdown() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			handleResourceDelta(event.getDelta());
			break;
		case IResourceChangeEvent.PRE_CLOSE:
			IProject project = event.getResource().getProject();
			if (isInterestingProject(project)) {
				removeTemplates(project.getName());
			}
			break;
		}
	}

	private void handleResourceDelta(IResourceDelta delta) {
		try {
			delta.accept(this);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta != null) {
			IResource resource = delta.getResource();
			if (!resource.isDerived()) {
				switch (resource.getType()) {
				case IResource.ROOT:
					return true;
				case IResource.PROJECT:
					return isInterestingProject(resource.getProject());
				case IResource.FOLDER:
					return true;
				case IResource.FILE:
					if (isInterestingFile(resource)) {
						handleFileDelta(delta);
					}
					return false;
				}
			}
		}
		return false;
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (!resource.isDerived()) {
			switch (resource.getType()) {
			case IResource.ROOT:
				return true;
			case IResource.PROJECT:
				return isInterestingProject(resource.getProject());
			case IResource.FOLDER:
				return true;
			case IResource.FILE:
				if (isInterestingFile(resource)) {
					addTemplate(new WorkspaceProjectTemplate((IFile) resource));
				}
				return false;
			}
		}
		return false;
	}

	private boolean isInterestingProject(IProject project) {
		System.err.println("TODO");
		return true;
	}

	private boolean isInterestingFile(IResource resource) {
		return (resource.getName().equals(TEMPLATE_MANIFEST) && resource instanceof IFile);
	}

	private void handleFileDelta(IResourceDelta delta) {
		IFile templateFile = (IFile) delta.getResource();
		String location = templateFile.getLocation().toString();
		IProject project = templateFile.getProject();

		int kind = delta.getKind();
		if (kind == IResourceDelta.REMOVED) {
			removeTemplate(project.getName(), location);
		} else if (kind == IResourceDelta.ADDED) {
			addTemplate(new WorkspaceProjectTemplate(templateFile));
		} else if (kind == IResourceDelta.CHANGED) {
			removeTemplate(project.getName(), location);
			addTemplate(new WorkspaceProjectTemplate(templateFile));
		}
	}

}
