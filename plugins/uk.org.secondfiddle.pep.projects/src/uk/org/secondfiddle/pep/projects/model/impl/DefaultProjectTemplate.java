package uk.org.secondfiddle.pep.projects.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import uk.org.secondfiddle.pep.projects.model.ParameterDescriptor;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.ProjectTemplateIcon;
import uk.org.secondfiddle.pep.projects.model.manager.ProjectTemplateProvider;

public class DefaultProjectTemplate extends AbstractProjectTemplate {

	private final ProjectTemplateProvider templateProvider;

	private final BaseProjectTemplate delegate;

	public DefaultProjectTemplate(ProjectTemplateProvider templateProvider, IFile templateFile) {
		this.templateProvider = templateProvider;
		this.delegate = new BaseProjectTemplate(templateFile);
	}

	private ProjectTemplate base() {
		if (delegate.getExtends() != null) {
			return templateProvider.getProjectTemplate(delegate.getExtends());
		}
		return null;
	}

	@Override
	public String getId() {
		return delegate.getId();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getGroup() {
		if (delegate.getGroup() == null && base() != null) {
			return base().getGroup();
		} else {
			return delegate.getGroup();
		}
	}

	@Override
	public String getExtends() {
		return delegate.getExtends();
	}

	@Override
	public String getProjectName() {
		return delegate.getProjectName();
	}

	@Override
	public String getPrimaryLocation() {
		return delegate.getPrimaryLocation();
	}

	@Override
	public List<String> getLocations() {
		List<String> locations = new ArrayList<String>();
		if (base() != null) {
			locations.addAll(base().getLocations());
		}
		locations.add(getPrimaryLocation());
		return locations;
	}

	@Override
	public ProjectTemplateIcon getSmallIcon() {
		if (delegate.getSmallIcon().getUrl() == null && base() != null) {
			return base().getSmallIcon();
		} else {
			return delegate.getSmallIcon();
		}
	}

	@Override
	public ProjectTemplateIcon getLargeIcon() {
		if (delegate.getLargeIcon().getUrl() == null && base() != null) {
			return base().getLargeIcon();
		} else {
			return delegate.getLargeIcon();
		}
	}

	@Override
	public List<ParameterDescriptor> getParameters() {
		List<ParameterDescriptor> parameters = new ArrayList<ParameterDescriptor>();
		if (base() != null) {
			parameters.addAll(base().getParameters());
		}
		parameters.addAll(delegate.getParameters());
		return parameters;
	}

}
