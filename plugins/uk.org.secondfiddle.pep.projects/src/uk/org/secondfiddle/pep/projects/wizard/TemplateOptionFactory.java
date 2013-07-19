package uk.org.secondfiddle.pep.projects.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.ComboChoiceOption;
import org.eclipse.pde.ui.templates.StringOption;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.org.secondfiddle.pep.projects.model.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.model.ParameterMapping;
import uk.org.secondfiddle.pep.projects.model.ParameterPreference;
import uk.org.secondfiddle.pep.projects.model.ParameterType;

@SuppressWarnings("restriction")
public class TemplateOptionFactory {

	public TemplateOption createTemplateOption(BaseOptionTemplateSection section, ParameterDescriptor descriptor) {
		String name = descriptor.getName();
		String label = descriptor.getLabel() + ":";
		ParameterType type = descriptor.getType();

		TemplateOption templateOption;
		if (type == ParameterType.STRING) {
			templateOption = new StringOption(section, name, label);
		} else if (type == ParameterType.DIRECTORY) {
			templateOption = new DirectoryOption(section, name, label);
		} else if (type == ParameterType.SELECT) {
			templateOption = new ComboChoiceOption(section, name, label, getOptions(descriptor));
		} else if (type == ParameterType.COMBO) {
			templateOption = new EditableComboChoiceOption(section, name, label, getOptions(descriptor));
		} else if (type == ParameterType.WORKINGSET) {
			templateOption = new EditableComboChoiceOption(section, name, label, getWorkingSetOptions(descriptor));
		} else if (type == ParameterType.HIDDEN) {
			templateOption = new HiddenTemplateOption(section, name);
		} else {
			throw new UnsupportedOperationException("Unsupported parameter type: " + type);
		}

		String preferenceValue = getPreferenceValue(descriptor);
		if (preferenceValue == null) {
			templateOption.setValue(descriptor.getDefaultValue());
		} else {
			templateOption.setValue(preferenceValue);
		}

		return templateOption;
	}

	private List<String> getWorkingSetNames() {
		List<String> workingSets = new ArrayList<String>();
		for (IWorkingSet workingSet : Workbench.getInstance().getWorkingSetManager().getAllWorkingSets()) {
			if (IWorkingSetIDs.JAVA.equals(workingSet.getId())) {
				workingSets.add(workingSet.getLabel());
			}
		}
		return workingSets;
	}

	private String[][] getWorkingSetOptions(ParameterDescriptor descriptor) {
		return getOptions(descriptor, getWorkingSetNames());
	}

	private String[][] getOptions(ParameterDescriptor descriptor) {
		return getOptions(descriptor, descriptor.getOptions());
	}

	private String[][] getOptions(ParameterDescriptor descriptor, List<String> originalOptions) {
		List<String[]> options = new ArrayList<String[]>();
		ParameterMapping displayMapping = descriptor.getDisplayMapping();
		Pattern labelPattern = Pattern.compile(displayMapping.getPattern());
		Pattern filterPattern = Pattern.compile(descriptor.getValueFilter());

		for (String option : originalOptions) {
			if (filterPattern.matcher(option).matches()) {
				String replacement = displayMapping.getReplacement();
				String label = labelPattern.matcher(option).replaceAll(replacement);
				options.add(new String[] { label, label });
			}
		}

		return options.toArray(new String[options.size()][]);
	}

	private String getPreferenceValue(ParameterDescriptor descriptor) {
		ParameterPreference preference = descriptor.getPreference();
		if (preference == null) {
			return null;
		}

		IPreferenceStore preferences = new ScopedPreferenceStore(InstanceScope.INSTANCE, preference.getPluginId());
		return preferences.getString(preference.getPreferenceName());
	}

}
