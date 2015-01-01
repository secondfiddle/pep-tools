package uk.org.secondfiddle.pep.projects.templates.manager;

import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;

public interface ProjectTemplateProvider {

	ProjectTemplate getProjectTemplate(String id);

	void shutdown();

}
