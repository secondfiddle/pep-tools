package uk.org.secondfiddle.pep.projects.model.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

class OrderedManifest extends Manifest {

	private final Collection<String> attributeNames = new LinkedHashSet<String>();

	@Override
	public Attributes getAttributes(String name) {
		attributeNames.add(name);
		return super.getAttributes(name);
	}

	@Override
	public Map<String, Attributes> getEntries() {
		Map<String, Attributes> orderedEntries = new LinkedHashMap<String, Attributes>();
		Map<String, Attributes> entries = super.getEntries();

		for (String attributeName : attributeNames) {
			orderedEntries.put(attributeName, entries.get(attributeName));
		}

		return orderedEntries;
	}

}
