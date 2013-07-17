package uk.org.secondfiddle.pep.projects.model.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.workspace.WorkspaceProjectTemplateProvider;

public class ProjectTemplateManager implements ProjectTemplateProviderListener {

	private static ProjectTemplateManager INSTANCE;

	public static synchronized ProjectTemplateManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ProjectTemplateManager();
		}
		return INSTANCE;
	}

	private final ProjectTemplateExtensionLoader extensionLoader = new ProjectTemplateExtensionLoader();

	private final Collection<ProjectTemplateProvider> providers = new ArrayList<ProjectTemplateProvider>();

	private final Collection<ProjectTemplate> templates = new HashSet<ProjectTemplate>();

	private ProjectTemplateManager() {
		this.providers.add(new WorkspaceProjectTemplateProvider(this));
	}

	public ProjectTemplate getProjectTemplate(String id) {
		for (ProjectTemplateProvider provider : providers) {
			ProjectTemplate template = provider.getProjectTemplate(id);
			if (template != null) {
				return template;
			}
		}
		return null;
	}

	public void shutdown() {
		for (ProjectTemplateProvider provider : providers) {
			provider.shutdown();
		}
	}

	@Override
	public void templateAdded(ProjectTemplate template) {
		templates.add(template);
		extensionLoader.addTemplateExtension(template);
	}

	@Override
	public void templateRemoved(ProjectTemplate template) {
		templates.remove(template);
		extensionLoader.removeTemplateExtension(template);
	}

}
