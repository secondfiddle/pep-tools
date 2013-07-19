package uk.org.secondfiddle.pep.projects.model;

import java.util.List;

public interface ProjectTemplate {

	String getId();

	String getName();

	ProjectTemplateIcon getSmallIcon();

	ProjectTemplateIcon getLargeIcon();

	String getProjectName();

	String getLocation();

	List<ParameterDescriptor> getParameters();

}
