package uk.org.secondfiddle.pep.projects.model.impl.workspace;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;

class ProjectTemplateEntry {

	private final ProjectTemplate template;

	private boolean resolved;

	public ProjectTemplateEntry(ProjectTemplate template, boolean resolved) {
		this.template = template;
		this.resolved = resolved;
	}

	public ProjectTemplate getTemplate() {
		return template;
	}

	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

}
