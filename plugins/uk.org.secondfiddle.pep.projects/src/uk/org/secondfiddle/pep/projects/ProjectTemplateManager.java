package uk.org.secondfiddle.pep.projects;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.internal.registry.ExtensionRegistry;
import org.eclipse.core.internal.registry.spi.ConfigurationElementAttribute;
import org.eclipse.core.internal.registry.spi.ConfigurationElementDescription;
import org.eclipse.core.runtime.ContributorFactoryOSGi;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.WorkbenchImages;
import org.osgi.framework.Bundle;

public class ProjectTemplateManager implements ProjectTemplateProviderListener {

	private static final String EXTENSION_POINT_ID = "org.eclipse.ui.newWizards";

	private static ProjectTemplateManager INSTANCE;

	public static synchronized ProjectTemplateManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ProjectTemplateManager();
		}
		return INSTANCE;
	}

	private final Collection<ProjectTemplateProvider> providers = new ArrayList<ProjectTemplateProvider>();

	private final Collection<ProjectTemplate> templates = new HashSet<ProjectTemplate>();

	private ProjectTemplateManager() {
		this.providers.add(new WorkspaceProjectTemplateProvider(this));
	}

	public void shutdown() {
		for (ProjectTemplateProvider provider : providers) {
			provider.shutdown();
		}
	}

	@Override
	public void templateAdded(ProjectTemplate template) {
		templates.add(template);

		ExtensionRegistry registry = (ExtensionRegistry) Platform.getExtensionRegistry();

		String label = template.getName();
		Bundle bundle = Platform.getBundle(ProjectTemplateActivator.PLUGIN_ID);
		IContributor contributor = ContributorFactoryOSGi.createContributor(bundle);
		Object token = registry.getTemporaryUserToken();

		String imageName = getImageName(template);

		ConfigurationElementAttribute[] atts = new ConfigurationElementAttribute[6];
		atts[0] = new ConfigurationElementAttribute("category", "projecttemplate");
		atts[1] = new ConfigurationElementAttribute("class", ProjectTemplateWizard.class.getName());
		atts[2] = new ConfigurationElementAttribute("id", template.getId());
		atts[3] = new ConfigurationElementAttribute("name", template.getName());
		atts[4] = new ConfigurationElementAttribute("icon", imageName);
		atts[5] = new ConfigurationElementAttribute("project", "true");
		ConfigurationElementDescription description = new ConfigurationElementDescription("wizard", atts, null, null);

		registry.addExtension(template.getId(), contributor, false, label, EXTENSION_POINT_ID, description, token);

		ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(template.getIcon());
		WorkbenchImages.declareImage(imageName, imageDescriptor, false);
	}

	private String getImageName(ProjectTemplate template) {
		return ProjectTemplateActivator.PLUGIN_ID + ":" + template.getId();
	}

	@Override
	public void templateRemoved(ProjectTemplate template) {
		templates.remove(template);

		ExtensionRegistry registry = (ExtensionRegistry) Platform.getExtensionRegistry();
		String identifier = template.getProjectName() + "." + template.getLocation();
		IExtension extension = registry.getExtension(identifier);
		Object token = registry.getTemporaryUserToken();
		registry.removeExtension(extension, token);
	}
}
