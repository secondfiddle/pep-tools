package uk.org.secondfiddle.pep.projects.model.manager;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;

public interface ProjectTemplateProvider {

	ProjectTemplate getProjectTemplate(String id);

	void shutdown();

}
