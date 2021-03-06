package uk.org.secondfiddle.pep.projects.templates.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.ComboChoiceOption;
import org.eclipse.pde.ui.templates.StringOption;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.org.secondfiddle.pep.projects.templates.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.templates.ParameterMapping;
import uk.org.secondfiddle.pep.projects.templates.ParameterPreference;
import uk.org.secondfiddle.pep.projects.templates.ParameterType;
import uk.org.secondfiddle.pep.projects.templates.wizard.option.DirectoryOption;
import uk.org.secondfiddle.pep.projects.templates.wizard.option.EditableComboChoiceOption;
import uk.org.secondfiddle.pep.projects.templates.wizard.option.HiddenTemplateOption;

@SuppressWarnings("restriction")
public class TemplateOptionFactory {

	private IStructuredSelection selection;

	public void setSelection(IStructuredSelection selection) {
		this.selection = selection;
	}

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
		String defaultValue = descriptor.getDefaultValue();
		String selectionValue = getFromSelection(descriptor);
		if (preferenceValue != null) {
			templateOption.setValue(preferenceValue);
		} else if (defaultValue != null) {
			templateOption.setValue(defaultValue);
		} else if (selectionValue != null) {
			templateOption.setValue(selectionValue);
		}

		templateOption.setRequired(true);

		return templateOption;
	}

	private String getFromSelection(ParameterDescriptor descriptor) {
		if (selection == null) {
			return null;
		}

		if (descriptor.getType() == ParameterType.WORKINGSET) {
			for (Object selected : selection.toArray()) {
				if (selected instanceof IWorkingSet) {
					IWorkingSet workingSet = (IWorkingSet) selected;
					if (!workingSet.isAggregateWorkingSet() && IWorkingSetIDs.JAVA.equals(workingSet.getId())) {
						ParameterMapping mapping = descriptor.getDisplayMapping();
						Pattern labelPattern = Pattern.compile(mapping.getPattern());
						Matcher matcher = labelPattern.matcher(workingSet.getName());
						return matcher.replaceAll(mapping.getReplacement());
					}
				}
			}
		}

		return null;
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
