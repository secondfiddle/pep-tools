package uk.org.secondfiddle.pep.projects.templates.manager;

import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;

public interface ProjectTemplateProviderListener {

	void templateAdded(ProjectTemplate template);

	void templateRemoved(ProjectTemplate template);

}
