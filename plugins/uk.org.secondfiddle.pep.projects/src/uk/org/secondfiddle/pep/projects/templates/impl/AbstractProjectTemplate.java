package uk.org.secondfiddle.pep.projects.templates.impl;

import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;

public abstract class AbstractProjectTemplate implements ProjectTemplate {

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getProjectName() == null) ? 0 : getProjectName().hashCode());
		return result;
	}

	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BaseProjectTemplate other = (BaseProjectTemplate) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if (getProjectName() == null) {
			if (other.getProjectName() != null)
				return false;
		} else if (!getProjectName().equals(other.getProjectName()))
			return false;
		return true;
	}

}
