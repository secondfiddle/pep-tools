package uk.org.secondfiddle.pep.projects;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public abstract class AbstractProjectTemplate implements ProjectTemplate {

	private static final String TEMPLATE_NAME = "TemplateName";

	private static final String TEMPLATE_ICON = "TemplateIcon";

	private final Manifest manifest;

	private final String projectName;

	private final String name;

	private final String icon;

	private final String location;

	protected AbstractProjectTemplate(IFile templateFile) {
		this.manifest = readManifest(templateFile);
		this.projectName = templateFile.getProject().getName();
		this.location = templateFile.getLocation().toString();
		this.name = manifest.getMainAttributes().getValue(TEMPLATE_NAME);
		this.icon = manifest.getMainAttributes().getValue(TEMPLATE_ICON);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getIcon() {
		return icon;
	}

	@Override
	public String getProjectName() {
		return projectName;
	}

	@Override
	public String getLocation() {
		return location;
	}

	private Manifest readManifest(IFile templateFile) {
		InputStream manifestStream = null;

		try {
			manifestStream = templateFile.getContents();
			return new Manifest(manifestStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(manifestStream);
		}
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

}
