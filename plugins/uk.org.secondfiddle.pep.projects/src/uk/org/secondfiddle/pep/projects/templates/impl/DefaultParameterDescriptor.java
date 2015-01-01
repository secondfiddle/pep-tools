package uk.org.secondfiddle.pep.projects.templates.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.org.secondfiddle.pep.projects.templates.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.templates.ParameterMapping;
import uk.org.secondfiddle.pep.projects.templates.ParameterPreference;
import uk.org.secondfiddle.pep.projects.templates.ParameterType;

public class DefaultParameterDescriptor implements ParameterDescriptor {

	private final String name;

	private final String label;

	private final ParameterType type;

	private final String defaultValue;

	private final String valueFilter;

	private final List<String> options;

	private final ParameterMapping displayMapping;

	private final ParameterMapping valueMapping;

	private ParameterPreference preference;

	public DefaultParameterDescriptor(String name, String label, String type, String defaultValue, String preference,
			String valueFilter, String valueMapping, String displayMapping, String options) {
		this.name = name;
		this.label = label;
		this.defaultValue = defaultValue;
		this.type = ParameterType.valueOf(type.toUpperCase());
		this.valueFilter = (valueFilter == null ? ".*" : StringUtils.substringBetween(valueFilter, "/"));
		this.displayMapping = new DefaultParameterMapping(displayMapping == null ? "/^$//" : displayMapping);
		this.valueMapping = new DefaultParameterMapping(valueMapping == null ? "/^$//" : valueMapping);
		this.options = new ArrayList<String>();

		if (options != null) {
			this.options.addAll(Arrays.asList(options.split("\\s*,\\s*")));
		}
		if (preference != null) {
			this.preference = new DefaultParameterPreference(preference);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getValueFilter() {
		return valueFilter;
	}

	@Override
	public ParameterMapping getValueMapping() {
		return valueMapping;
	}

	@Override
	public ParameterMapping getDisplayMapping() {
		return displayMapping;
	}

	@Override
	public ParameterPreference getPreference() {
		return preference;
	}

	@Override
	public List<String> getOptions() {
		return options;
	}

	@Override
	public ParameterType getType() {
		return type;
	}

}
