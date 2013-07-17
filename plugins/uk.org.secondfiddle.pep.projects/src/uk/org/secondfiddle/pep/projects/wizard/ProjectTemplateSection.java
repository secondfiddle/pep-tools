package uk.org.secondfiddle.pep.projects.wizard;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.OptionTemplateWizardPage;
import org.eclipse.pde.ui.templates.TemplateOption;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;

public class ProjectTemplateSection extends BaseOptionTemplateSection {

	private static final String TEMPLATE_DIRECTORY_NAME = "template";

	private final List<WizardPage> pages = new ArrayList<WizardPage>();

	/**
	 * {@link TemplateOption} fields required for
	 * {@link OptionTemplateWizardPage}.
	 */
	private final ArrayList<TemplateOption> options = new ArrayList<TemplateOption>();

	/**
	 * Replacement values to fill out the template.
	 */
	private final Map<String, String> replacementStrings = new HashMap<String, String>();

	private final ProjectTemplate template;

	public ProjectTemplateSection(ProjectTemplate template) {
		this.template = template;
	}

	@Override
	protected void registerOption(TemplateOption option, Object value, int pageIndex) {
		super.registerOption(option, value, pageIndex);
		options.add(option);
	}

	@Override
	public String getReplacementString(String fileName, String key) {
		/*
		 * Returns "key" if no replacement found - therefore the identity check
		 * below is deliberate
		 */
		String replacement = super.getReplacementString(fileName, key);
		return (replacement == key ? replacementStrings.get(key) : replacement);
	}

	@Override
	public Object getValue(String name) {
		/*
		 * Returns null if no replacement found
		 */
		Object value = super.getValue(name);
		return (value == null ? replacementStrings.get(name) : value);
	}

	@Override
	public URL getTemplateLocation() {
		try {
			return new File(template.getLocation(), TEMPLATE_DIRECTORY_NAME).toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getLabel() {
		return "label";
	}

	@Override
	public void addPages(Wizard wizard) {
		replacementStrings.put("testsub", "MyClass");
		replacementStrings.put("testsrc", "src/fnarg.wizards");
		replacementStrings.put(KEY_PACKAGE_NAME, "base.pack");
		addOption("test", "Test", "value", 0);
		pages.add(new OptionTemplateWizardPage(this, options, null));
		wizard.addPage(pages.get(0));
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
	public void validateOptions(TemplateOption changed) {
		System.err.println("validateOptions");
	}

	@Override
	protected ResourceBundle getPluginResourceBundle() {
		return null;
	}

	@Override
	protected void updateModel(IProgressMonitor monitor) throws CoreException {
		System.err.println("updateModel");
	}

}
