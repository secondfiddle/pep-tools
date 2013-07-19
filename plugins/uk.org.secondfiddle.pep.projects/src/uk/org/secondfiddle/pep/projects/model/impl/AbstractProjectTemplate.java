package uk.org.secondfiddle.pep.projects.model.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import uk.org.secondfiddle.pep.projects.ProjectTemplateActivator;
import uk.org.secondfiddle.pep.projects.model.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplateIcon;

public abstract class AbstractProjectTemplate implements ProjectTemplate {

	private static final String TEMPLATE_NAME = "TemplateName";

	private static final String TEMPLATE_ICON_SMALL = "TemplateIconSmall";

	private static final String TEMPLATE_ICON_LARGE = "TemplateIconLarge";

	private static final String PARAMETER_LABEL = "Label";

	private static final String PARAMETER_TYPE = "Type";

	private static final String PARAMETER_DEFAULT_VALUE = "DefaultValue";

	private static final String PARAMETER_PREFERENCE = "Preference";

	private static final String PARAMETER_VALUE_FILTER = "ValueFilter";

	private static final String PARAMETER_VALUE_MAPPING = "ValueMapping";

	private static final String PARAMETER_DISPLAY_MAPPING = "DisplayMapping";

	private static final String PARAMETER_OPTIONS = "Options";

	private final Manifest manifest;

	private final String projectName;

	private final String name;

	private final URL smallIcon;

	private final URL largeIcon;

	private final String location;

	private List<ParameterDescriptor> parameters;

	protected AbstractProjectTemplate(IFile templateFile) {
		this.manifest = readManifest(templateFile);
		this.projectName = templateFile.getProject().getName();
		this.location = templateFile.getParent().getLocation().toString();
		this.name = manifest.getMainAttributes().getValue(TEMPLATE_NAME);
		this.smallIcon = getIconUrl(TEMPLATE_ICON_SMALL, templateFile, manifest);
		this.largeIcon = getIconUrl(TEMPLATE_ICON_LARGE, templateFile, manifest);
	}

	private Manifest readManifest(IFile templateFile) {
		InputStream manifestStream = null;

		try {
			manifestStream = templateFile.getContents();
			Manifest manifest = new OrderedManifest();
			manifest.read(manifestStream);
			return manifest;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(manifestStream);
		}
	}

	private URL getIconUrl(String iconAttribute, IFile templateFile, Manifest manifest) {
		String icon = manifest.getMainAttributes().getValue(iconAttribute);
		if (icon == null) {
			ProjectTemplateActivator.logError("No icon specified in " + templateFile);
			return null;
		}

		URL iconUrl = null;
		IPath templateLocation = templateFile.getParent().getLocation();
		IPath iconPath = templateLocation.append(icon);
		File iconFile = iconPath.toFile();

		if (!iconFile.exists()) {
			ProjectTemplateActivator.logError("Missing icon " + icon);
			return null;
		}

		try {
			iconUrl = iconFile.toURI().toURL();
		} catch (MalformedURLException e) {
			ProjectTemplateActivator.logError("Invalid icon " + icon, e);
		}

		return iconUrl;
	}

	@Override
	public String getId() {
		return projectName + ":" + name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ProjectTemplateIcon getSmallIcon() {
		return new DefaultProjectTemplateIcon(smallIcon);
	}

	@Override
	public ProjectTemplateIcon getLargeIcon() {
		return new DefaultProjectTemplateIcon(largeIcon);
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getLocation() {
		return location;
	}

	@Override
	public List<ParameterDescriptor> getParameters() {
		if (parameters == null) {
			parameters = readParameters();
		}
		return parameters;
	}

	private List<ParameterDescriptor> readParameters() {
		List<ParameterDescriptor> parameters = new ArrayList<ParameterDescriptor>();
		for (Entry<String, Attributes> paramEntry : manifest.getEntries().entrySet()) {
			String name = paramEntry.getKey();
			String label = paramEntry.getValue().getValue(PARAMETER_LABEL);
			String type = paramEntry.getValue().getValue(PARAMETER_TYPE);
			String defaultValue = paramEntry.getValue().getValue(PARAMETER_DEFAULT_VALUE);
			String preference = paramEntry.getValue().getValue(PARAMETER_PREFERENCE);
			String valueFilter = paramEntry.getValue().getValue(PARAMETER_VALUE_FILTER);
			String valueMapping = paramEntry.getValue().getValue(PARAMETER_VALUE_MAPPING);
			String labelMapping = paramEntry.getValue().getValue(PARAMETER_DISPLAY_MAPPING);
			String options = paramEntry.getValue().getValue(PARAMETER_OPTIONS);
			parameters.add(new DefaultParameterDescriptor(name, label, type, defaultValue, preference, valueFilter,
					valueMapping, labelMapping, options));
		}
		return parameters;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractProjectTemplate other = (AbstractProjectTemplate) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (projectName == null) {
			if (other.projectName != null)
				return false;
		} else if (!projectName.equals(other.projectName))
			return false;
		return true;
	}

	private class DefaultProjectTemplateIcon implements ProjectTemplateIcon {

		private final URL icon;

		public DefaultProjectTemplateIcon(URL icon) {
			this.icon = icon;
		}

		@Override
		public URL getUrl() {
			return icon;
		}

		@Override
		public String getId() {
			return AbstractProjectTemplate.this.getId() + ":" + icon;
		}

	}

}
