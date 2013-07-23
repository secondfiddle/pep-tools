package uk.org.secondfiddle.pep.projects.model.manager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.spi.ConfigurationElementAttribute;
import org.eclipse.core.internal.registry.spi.ConfigurationElementDescription;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.wizards.NewWizardRegistry;
import org.eclipse.ui.wizards.IWizardCategory;
import org.osgi.framework.Bundle;

import uk.org.secondfiddle.pep.projects.ProjectTemplateActivator;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplateIcon;
import uk.org.secondfiddle.pep.projects.wizard.ProjectTemplateWizard;

@SuppressWarnings("restriction")
public class ProjectTemplateExtensionLoader {

	private static final String EXTENSION_POINT_ID = PlatformUI.PLUGIN_ID + "." + IWorkbenchRegistryConstants.PL_NEW;

	private final IContributor contributor;

	private final ExtensionRegistry extensionRegistry;

	private final NewWizardRegistry wizardRegistry;

	public ProjectTemplateExtensionLoader() {
		Bundle bundle = Platform.getBundle(ProjectTemplateActivator.PLUGIN_ID);
		this.contributor = ContributorFactoryOSGi.createContributor(bundle);
		this.extensionRegistry = (ExtensionRegistry) Platform.getExtensionRegistry();
		this.wizardRegistry = NewWizardRegistry.getInstance();
	}

	public void addTemplateExtension(ProjectTemplate template) {
		ensureGroupExists(template);

		String id = toExtensionId(template.getId());
		String label = template.getName();
		ConfigurationElementDescription description = createTemplateDescription(template);
		addExtension(id, label, description);

		addImage(template.getSmallIcon(), template);
		addImage(template.getLargeIcon(), template);
	}

	private void ensureGroupExists(ProjectTemplate template) {
		if (template.getGroup() == null) {
			return;
		}

		IWizardCategory category = wizardRegistry.findCategory(template.getGroup());
		if (category == null) {
			String id = "project.template." + template.getGroup();
			String label = template.getGroup();
			ConfigurationElementDescription description = createGroupDescription(template);
			addExtension(id, label, description);
		}
	}

	public void removeTemplateExtension(ProjectTemplate template) {
		String id = toExtensionId(template.getId());
		removeExtension(id);

		removeImage(template.getSmallIcon(), template);
		removeImage(template.getLargeIcon(), template);
	}

	private void addExtension(String id, String label, ConfigurationElementDescription description) {
		Object token = extensionRegistry.getTemporaryUserToken();
		extensionRegistry.addExtension(id, contributor, false, label, EXTENSION_POINT_ID, description, token);
	}

	private void removeExtension(String id) {
		IExtension extension = extensionRegistry.getExtension(id);
		Object token = extensionRegistry.getTemporaryUserToken();
		extensionRegistry.removeExtension(extension, token);
	}

	private void addImage(ProjectTemplateIcon icon, ProjectTemplate template) {
		if (icon.getUrl() != null && template.getId().equals(icon.getTemplateId())) {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(icon.getUrl());
			WorkbenchImages.declareImage(icon.getId(), imageDescriptor, false);
		}
	}

	private void removeImage(ProjectTemplateIcon icon, ProjectTemplate template) {
		if (icon.getUrl() != null && template.getId().equals(icon.getTemplateId())) {
			WorkbenchImages.getDescriptors().remove(icon.getId());
		}
	}

	private ConfigurationElementDescription createGroupDescription(ProjectTemplate template) {
		ConfigurationElementAttribute[] atts = new ConfigurationElementAttribute[2];
		atts[0] = new ConfigurationElementAttribute("id", template.getGroup());
		atts[1] = new ConfigurationElementAttribute("name", template.getGroup());
		return new ConfigurationElementDescription(IWorkbenchRegistryConstants.TAG_CATEGORY, atts, null, null);
	}

	private ConfigurationElementDescription createTemplateDescription(ProjectTemplate template) {
		List<ConfigurationElementAttribute> atts = new ArrayList<ConfigurationElementAttribute>();
		atts.add(new ConfigurationElementAttribute("class", ProjectTemplateWizard.class.getName()));
		atts.add(new ConfigurationElementAttribute("id", template.getId()));
		atts.add(new ConfigurationElementAttribute("name", template.getName()));
		atts.add(new ConfigurationElementAttribute("icon", template.getSmallIcon().getId()));
		atts.add(new ConfigurationElementAttribute("project", "true"));
		if (template.getGroup() != null) {
			atts.add(new ConfigurationElementAttribute("category", template.getGroup()));
		}

		ConfigurationElementAttribute[] attsArray = atts.toArray(new ConfigurationElementAttribute[atts.size()]);
		return new ConfigurationElementDescription(IWorkbenchRegistryConstants.TAG_WIZARD, attsArray, null, null);
	}

	public static String toExtensionId(String templateId) {
		return ProjectTemplateActivator.PLUGIN_ID + "." + templateId;
	}

	public static String toTemplateId(String extensionId) {
		return extensionId.replace(ProjectTemplateActivator.PLUGIN_ID + ".", "");
	}

}
