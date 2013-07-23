package uk.org.secondfiddle.pep.projects.model;

import java.util.List;

public interface ProjectTemplate {

	String getId();

	String getName();

	String getGroup();

	String getExtends();

	ProjectTemplateIcon getSmallIcon();

	ProjectTemplateIcon getLargeIcon();

	String getProjectName();

	String getPrimaryLocation();

	List<String> getLocations();

	List<ParameterDescriptor> getParameters();

}
