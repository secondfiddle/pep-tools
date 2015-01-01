package uk.org.secondfiddle.pep.projects.templates.impl.workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.templates.impl.DefaultProjectTemplate;
import uk.org.secondfiddle.pep.projects.templates.manager.ProjectTemplateProvider;
import uk.org.secondfiddle.pep.projects.templates.manager.ProjectTemplateProviderListener;
import uk.org.secondfiddle.pep.projects.templates.nature.ProjectTemplateNature;

public class WorkspaceProjectTemplateProvider implements ProjectTemplateProvider, IResourceChangeListener,
		IResourceDeltaVisitor, IResourceVisitor {

	private static final String TEMPLATE_MANIFEST = "template.mf";

	private final Map<String, ProjectTemplateEntry> templates = new HashMap<String, ProjectTemplateEntry>();

	private final ProjectTemplateProviderListener listener;

	public WorkspaceProjectTemplateProvider(ProjectTemplateProviderListener listener) {
		this.listener = listener;
		initialise();
	}

	@Override
	public ProjectTemplate getProjectTemplate(String id) {
		ProjectTemplateEntry entry = templates.get(id);
		return (entry == null ? null : entry.getTemplate());
	}

	private void addTemplate(ProjectTemplate template) {
		if (isValid(template)) {
			ProjectTemplateEntry templateEntry = new ProjectTemplateEntry(template, isResolved(template));
			this.templates.put(template.getId(), templateEntry);
			postAdd(templateEntry);
		}
	}

	private boolean isResolved(ProjectTemplate template) {
		if (StringUtils.isEmpty(template.getExtends())) {
			return true;
		}
		return templates.containsKey(template.getExtends());
	}

	private boolean isValid(ProjectTemplate template) {
		return !StringUtils.isEmpty(template.getId()) && !StringUtils.isEmpty(template.getName())
				&& !templates.containsKey(template.getId());
	}

	private void removeTemplates(String projectName) {
		Iterator<ProjectTemplateEntry> it = templates.values().iterator();
		while (it.hasNext()) {
			ProjectTemplateEntry templateEntry = it.next();
			ProjectTemplate template = templateEntry.getTemplate();
			if (projectName.equals(template.getProjectName())) {
				it.remove();
				postRemove(templateEntry);
			}
		}
	}

	private void removeTemplate(String projectName, String location) {
		Iterator<ProjectTemplateEntry> it = templates.values().iterator();
		while (it.hasNext()) {
			ProjectTemplateEntry templateEntry = it.next();
			ProjectTemplate template = templateEntry.getTemplate();
			if (projectName.equals(template.getProjectName()) && location.equals(template.getPrimaryLocation())) {
				it.remove();
				postRemove(templateEntry);
				break;
			}
		}
	}

	private void postAdd(ProjectTemplateEntry addedEntry) {
		if (addedEntry.isResolved()) {
			this.listener.templateAdded(addedEntry.getTemplate());
			resolveExtensions(addedEntry);
		}
	}

	private void resolveExtensions(ProjectTemplateEntry resolved) {
		for (ProjectTemplateEntry entry : templates.values()) {
			if (!entry.isResolved() && resolved.getTemplate().getId().equals(entry.getTemplate().getExtends())) {
				entry.setResolved(true);
				this.listener.templateAdded(entry.getTemplate());
				resolveExtensions(entry);
			}
		}
	}

	private void postRemove(ProjectTemplateEntry removedEntry) {
		this.listener.templateRemoved(removedEntry.getTemplate());
		unresolveExtensions(removedEntry);
	}

	private void unresolveExtensions(ProjectTemplateEntry unresolved) {
		for (ProjectTemplateEntry entry : templates.values()) {
			if (entry.isResolved() && unresolved.getTemplate().getId().equals(entry.getTemplate().getExtends())) {
				entry.setResolved(false);
				this.listener.templateRemoved(entry.getTemplate());
				unresolveExtensions(entry);
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
					addTemplate(new DefaultProjectTemplate(this, (IFile) resource));
				}
				return false;
			}
		}
		return false;
	}

	private boolean isInterestingProject(IProject project) {
		try {
			return project.isOpen() && project.hasNature(ProjectTemplateNature.ID);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private boolean isInterestingFile(IResource resource) {
		return (resource.getName().equals(TEMPLATE_MANIFEST) && resource instanceof IFile);
	}

	private void handleTemplateFileDelta(IResourceDelta delta) {
		IFile templateFile = (IFile) delta.getResource();
		String location = templateFile.getParent().getLocation().toString();
		IProject project = templateFile.getProject();

		int kind = delta.getKind();
		if (kind == IResourceDelta.REMOVED) {
			removeTemplate(project.getName(), location);
		} else if (kind == IResourceDelta.ADDED) {
			addTemplate(new DefaultProjectTemplate(this, templateFile));
		} else if (kind == IResourceDelta.CHANGED) {
			removeTemplate(project.getName(), location);
			addTemplate(new DefaultProjectTemplate(this, templateFile));
		}
	}

}
