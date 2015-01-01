package uk.org.secondfiddle.pep.projects.templates.impl.workspace;

import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;

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
