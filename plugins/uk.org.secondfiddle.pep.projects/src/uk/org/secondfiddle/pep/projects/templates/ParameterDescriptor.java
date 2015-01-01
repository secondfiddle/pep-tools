package uk.org.secondfiddle.pep.projects.templates;

import java.util.List;

public interface ParameterDescriptor {

	String getName();

	String getLabel();

	String getDefaultValue();

	String getValueFilter();

	ParameterMapping getValueMapping();

	ParameterMapping getDisplayMapping();

	ParameterPreference getPreference();

	List<String> getOptions();

	ParameterType getType();

}
