package uk.org.secondfiddle.pep.projects.model.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.org.secondfiddle.pep.projects.model.ParameterMapping;

public class DefaultParameterMapping implements ParameterMapping {

	private static final Pattern SPLIT_PATTERN = Pattern.compile("^/(.*)/(.*)/$");

	private final String pattern;

	private final String replacement;

	public DefaultParameterMapping(String mapping) {
		Matcher matcher = SPLIT_PATTERN.matcher(mapping);
		if (matcher.matches()) {
			this.pattern = matcher.group(1);
			this.replacement = matcher.group(2);
		} else {
			throw new IllegalArgumentException("Invalid parameter mapping");
		}
	}

	@Override
	public String getPattern() {
		return pattern;
	}

	@Override
	public String getReplacement() {
		return replacement;
	}

}
