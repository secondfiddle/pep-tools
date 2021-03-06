package uk.org.secondfiddle.pep.projects.templates.wizard;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.OptionTemplateWizardPage;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import uk.org.secondfiddle.pep.projects.ProjectTemplateActivator;
import uk.org.secondfiddle.pep.projects.templates.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.templates.ParameterMapping;
import uk.org.secondfiddle.pep.projects.templates.ParameterPreference;
import uk.org.secondfiddle.pep.projects.templates.ProjectTemplate;

public class ProjectTemplateSection extends BaseOptionTemplateSection {

	private static final String PLACEHOLDER_FILE = "empty";

	private static final String UNMAPPED_VALUE_SUFFIX = ":unmapped";

	private static final String UPPER_CAMEL_CASE_SUFFIX = ":uppercamelcase";

	private static final String LOWER_CAMEL_CASE_SUFFIX = ":lowercamelcase";

	private static final String TEMPLATE_DIRECTORY_NAME = "template";

	private final List<WizardPage> pages = new ArrayList<WizardPage>();

	private final Map<TemplateOption, ParameterDescriptor> options = new LinkedHashMap<TemplateOption, ParameterDescriptor>();

	/**
	 * Replacement values to fill out the template.
	 */
	private final Map<String, String> replacementStrings = new HashMap<String, String>();

	private final TemplateOptionFactory templateOptionFactory = new TemplateOptionFactory();

	private ProjectTemplate template;

	public void setProjectTemplate(ProjectTemplate template) {
		this.template = template;
	}

	@Override
	protected void registerOption(TemplateOption option, Object value, int pageIndex) {
		super.registerOption(option, value, pageIndex);
		options.put(option, null);
	}

	@Override
	public String getReplacementString(String fileName, String key) {
		/*
		 * Returns "key" if no replacement found - therefore the identity check
		 * below is deliberate
		 */
		String replacement = super.getReplacementString(fileName, key);
		return (replacement == key ? getValueString(key) : replacement);
	}

	@Override
	public Object getValue(String name) {
		/*
		 * Returns null if no replacement found
		 */
		Object value = super.getValue(name);
		return (value == null ? replacementStrings.get(name) : value);
	}

	public String getValueString(String name) {
		Object value = getValue(name);
		return (value == null ? null : value.toString());
	}

	@Override
	protected void generateFiles(IProgressMonitor monitor) throws CoreException {
		for (String location : template.getLocations()) {
			generateFiles(monitor, toTemplateLocationURL(location));
		}
	}

	@Override
	protected boolean isOkToCreateFile(File sourceFile) {
		return !PLACEHOLDER_FILE.equals(sourceFile.getName());
	}

	private URL toTemplateLocationURL(String location) {
		try {
			File templateDir = new File(location, TEMPLATE_DIRECTORY_NAME);
			return templateDir.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addPages(Wizard wizard) {
		addOptions();

		WizardPage page = new OptionTemplateWizardPage(this, new ArrayList<TemplateOption>(options.keySet()), null);
		page.setTitle(template.getName());
		pages.add(page);
		wizard.addPage(page);

		validateOptions(null);
	}

	private void addOptions() {
		addOption(KEY_PLUGIN_ID, "Project name:", "", 0);
		for (ParameterDescriptor param : template.getParameters()) {
			TemplateOption option = templateOptionFactory.createTemplateOption(this, param);
			registerOption(option, option.getValue(), 0);
			options.put(option, param);
		}
	}

	public void setSelection(IStructuredSelection selection) {
		this.templateOptionFactory.setSelection(selection);
	}

	public void finish() {
		replacementStrings.put(KEY_PACKAGE_NAME, String.valueOf(getValue(KEY_PLUGIN_ID)));

		for (Entry<TemplateOption, ParameterDescriptor> entry : options.entrySet()) {
			TemplateOption templateOption = entry.getKey();
			ParameterDescriptor descriptor = entry.getValue();
			if (descriptor == null) {
				continue;
			}

			Object value = templateOption.getValue();
			if (!(value instanceof String)) {
				continue;
			}

			String valueString = (String) value;
			replacementStrings.put(templateOption.getName() + UNMAPPED_VALUE_SUFFIX, valueString);
			ParameterMapping valueMapping = descriptor.getValueMapping();
			String newValue = valueString.replaceAll(valueMapping.getPattern(), valueMapping.getReplacement());
			templateOption.setValue(newValue);

			addStringMappings(templateOption.getName(), valueString);

			ParameterPreference preference = descriptor.getPreference();
			if (preference != null) {
				IPersistentPreferenceStore preferences = new ScopedPreferenceStore(InstanceScope.INSTANCE,
						preference.getPluginId());
				preferences.setValue(preference.getPreferenceName(), valueString);
				try {
					preferences.save();
				} catch (IOException e) {
					ProjectTemplateActivator.logError("Failed to save preference", e);
				}
			}
		}
	}

	private void addStringMappings(String name, String value) {
		String capitalized = WordUtils.capitalizeFully(value);
		String upperCamel = StringUtils.deleteWhitespace(capitalized);
		String lowerCamel = StringUtils.uncapitalize(upperCamel);
		replacementStrings.put(name + LOWER_CAMEL_CASE_SUFFIX, lowerCamel);
		replacementStrings.put(name + UPPER_CAMEL_CASE_SUFFIX, upperCamel);
	}

	@Override
	public void validateOptions(TemplateOption changed) {
		WizardPage page = getPage(0);

		for (TemplateOption option : options.keySet()) {
			if (option.isRequired() && option.isEmpty()) {
				page.setPageComplete(false);
				page.setErrorMessage("Required information is missing");
				return;
			}
		}

		page.setPageComplete(true);
		page.setErrorMessage(null);
	}

	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public WizardPage getPage(int pageIndex) {
		return pages.get(pageIndex);
	}

	@Override
	public int getPageCount() {
		return 1;
	}

	@Override
	public String getUsedExtensionPoint() {
		return null;
	}

	@Override
	public String[] getNewFiles() {
		return new String[0];
	}

	@Override
	protected ResourceBundle getPluginResourceBundle() {
		return null;
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
	}

}
