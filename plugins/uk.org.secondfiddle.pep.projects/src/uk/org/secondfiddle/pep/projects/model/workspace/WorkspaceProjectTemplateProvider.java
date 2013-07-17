package uk.org.secondfiddle.pep.projects.model.workspace;

import java.util.HashMap;
import java.util.Iterator;
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

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.manager.ProjectTemplateProvider;
import uk.org.secondfiddle.pep.projects.model.manager.ProjectTemplateProviderListener;
import uk.org.secondfiddle.pep.projects.nature.ProjectTemplateNature;

public class WorkspaceProjectTemplateProvider implements ProjectTemplateProvider, IResourceChangeListener,
		IResourceDeltaVisitor, IResourceVisitor {

	private static final String TEMPLATE_MANIFEST = "template.mf";

	private final Map<String, ProjectTemplate> templates = new HashMap<String, ProjectTemplate>();

	private final ProjectTemplateProviderListener listener;

	public WorkspaceProjectTemplateProvider(ProjectTemplateProviderListener listener) {
		this.listener = listener;
		initialise();
	}

	@Override
	public ProjectTemplate getProjectTemplate(String id) {
		return templates.get(id);
	}

	private void addTemplate(ProjectTemplate template) {
		this.templates.put(template.getId(), template);
		this.listener.templateAdded(template);
	}

	private void removeTemplates(String projectName) {
		Iterator<ProjectTemplate> it = templates.values().iterator();
		while (it.hasNext()) {
			ProjectTemplate template = it.next();
			if (projectName.equals(template.getProjectName())) {
				it.remove();
				this.listener.templateRemoved(template);
			}
		}
	}

	private void removeTemplate(String projectName, String location) {
		Iterator<ProjectTemplate> it = templates.values().iterator();
		while (it.hasNext()) {
			ProjectTemplate template = it.next();
			if (projectName.equals(template.getProjectName()) && location.equals(template.getLocation())) {
				it.remove();
				this.listener.templateRemoved(template);
				break;
			}
		}
	}

	private void initialise() {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		try {
			workspaceRoot.accept(this);
		} catch (CoreException e) {
			throw new RuntimeException(e);
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
						handleTemplateFileDelta(delta);
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
		try {
			return project.hasNature(ProjectTemplateNature.ID);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isInterestingFile(IResource resource) {
		return (resource.getName().equals(TEMPLATE_MANIFEST) && resource instanceof IFile);
	}

	private void handleTemplateFileDelta(IResourceDelta delta) {
		IFile templateFile = (IFile) delta.getResource();
		String location = templateFile.getParent().getProjectRelativePath().toString();
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
