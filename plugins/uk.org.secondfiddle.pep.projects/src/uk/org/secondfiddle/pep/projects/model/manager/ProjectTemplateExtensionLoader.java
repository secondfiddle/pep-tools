package uk.org.secondfiddle.pep.projects.model.manager;

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
import org.osgi.framework.Bundle;

import uk.org.secondfiddle.pep.projects.ProjectTemplateActivator;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplateIcon;
import uk.org.secondfiddle.pep.projects.wizard.ProjectTemplateWizard;

@SuppressWarnings("restriction")
public class ProjectTemplateExtensionLoader {

	private static final String EXTENSION_POINT_ID = PlatformUI.PLUGIN_ID + "." + IWorkbenchRegistryConstants.PL_NEW;

	private final IContributor contributor;

	private final ExtensionRegistry registry;

	public ProjectTemplateExtensionLoader() {
		Bundle bundle = Platform.getBundle(ProjectTemplateActivator.PLUGIN_ID);
		this.contributor = ContributorFactoryOSGi.createContributor(bundle);
		this.registry = (ExtensionRegistry) Platform.getExtensionRegistry();
	}

	public void addTemplateExtension(ProjectTemplate template) {
		String id = template.getId();
		String label = template.getName();
		ConfigurationElementDescription description = createDescription(template);
		Object token = registry.getTemporaryUserToken();

		registry.addExtension(id, contributor, false, label, EXTENSION_POINT_ID, description, token);
		addImage(template.getSmallIcon());
		addImage(template.getLargeIcon());
	}

	public void removeTemplateExtension(ProjectTemplate template) {
		IExtension extension = registry.getExtension(template.getId());
		Object token = registry.getTemporaryUserToken();

		registry.removeExtension(extension, token);
		removeImage(template.getSmallIcon());
		removeImage(template.getLargeIcon());
	}

	private void addImage(ProjectTemplateIcon icon) {
		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(icon.getUrl());
		WorkbenchImages.declareImage(icon.getId(), imageDescriptor, false);
	}

	private void removeImage(ProjectTemplateIcon icon) {
		WorkbenchImages.getDescriptors().remove(icon.getId());
	}

	private ConfigurationElementDescription createDescription(ProjectTemplate template) {
		ConfigurationElementAttribute[] atts = new ConfigurationElementAttribute[6];
		atts[0] = new ConfigurationElementAttribute("category", "projecttemplate");
		atts[1] = new ConfigurationElementAttribute("class", ProjectTemplateWizard.class.getName());
		atts[2] = new ConfigurationElementAttribute("id", template.getId());
		atts[3] = new ConfigurationElementAttribute("name", template.getName());
		atts[4] = new ConfigurationElementAttribute("icon", template.getSmallIcon().getId());
		atts[5] = new ConfigurationElementAttribute("project", "true");
		return new ConfigurationElementDescription("wizard", atts, null, null);
	}

}
