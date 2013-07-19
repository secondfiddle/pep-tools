package uk.org.secondfiddle.pep.projects.model.impl;

import uk.org.secondfiddle.pep.projects.model.ParameterPreference;

public class DefaultParameterPreference implements ParameterPreference {

	private final String pluginId;

	private final String preferenceName;

	public DefaultParameterPreference(String preference) {
		String[] splitPref = preference.split(":");
		if (splitPref.length != 2) {
			throw new IllegalArgumentException("Preferences must be in the form 'plugin:key'");
		}

		this.pluginId = splitPref[0];
		this.preferenceName = splitPref[1];
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getPreferenceName() {
		return preferenceName;
	}

}
