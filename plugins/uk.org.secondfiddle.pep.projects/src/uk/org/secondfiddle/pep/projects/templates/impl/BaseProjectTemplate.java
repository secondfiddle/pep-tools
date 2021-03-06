package uk.org.secondfiddle.pep.projects.templates.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import uk.org.secondfiddle.pep.projects.ProjectTemplateActivator;
import uk.org.secondfiddle.pep.projects.templates.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.templates.ProjectTemplateIcon;

public class BaseProjectTemplate extends AbstractProjectTemplate {

	private static final String TEMPLATE_NAME = "TemplateName";

	private static final String TEMPLATE_GROUP = "TemplateGroup";

	private static final String TEMPLATE_ICON_SMALL = "TemplateIconSmall";

	private static final String TEMPLATE_ICON_LARGE = "TemplateIconLarge";

	private static final String TEMPLATE_EXTENDS = "TemplateExtends";

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

	private final String group;

	private final URL smallIcon;

	private final URL largeIcon;

	private final String location;

	private final String templateExtends;

	private List<ParameterDescriptor> parameters;

	protected BaseProjectTemplate(IFile templateFile) {
		this.manifest = readManifest(templateFile);
		this.projectName = templateFile.getProject().getName();
		this.location = templateFile.getParent().getLocation().toString();
		this.name = manifest.getMainAttributes().getValue(TEMPLATE_NAME);
		this.group = manifest.getMainAttributes().getValue(TEMPLATE_GROUP);
		this.smallIcon = getIconUrl(TEMPLATE_ICON_SMALL, templateFile, manifest);
		this.largeIcon = getIconUrl(TEMPLATE_ICON_LARGE, templateFile, manifest);
		this.templateExtends = manifest.getMainAttributes().getValue(TEMPLATE_EXTENDS);
	}

	@Override
	public String getExtends() {
		return templateExtends;
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
			ProjectTemplateActivator.logWarning("No icon specified in " + templateFile);
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
	public String getGroup() {
		return group;
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
	public String getPrimaryLocation() {
		return location;
	}

	@Override
	public List<String> getLocations() {
		return Collections.singletonList(getPrimaryLocation());
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
			return BaseProjectTemplate.this.getId() + ":" + icon;
		}

		@Override
		public String getTemplateId() {
			return BaseProjectTemplate.this.getId();
		}

	}

}
