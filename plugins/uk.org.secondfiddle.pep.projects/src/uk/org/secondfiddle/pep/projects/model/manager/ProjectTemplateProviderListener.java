package uk.org.secondfiddle.pep.projects.model.manager;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;

public interface ProjectTemplateProviderListener {

	void templateAdded(ProjectTemplate template);

	void templateRemoved(ProjectTemplate template);

}
