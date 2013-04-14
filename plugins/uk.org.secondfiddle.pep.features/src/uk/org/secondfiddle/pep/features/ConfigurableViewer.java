package uk.org.secondfiddle.pep.features;

import org.eclipse.jface.viewers.ViewerFilter;

import uk.org.secondfiddle.pep.features.action.ContentProviderAction;

public interface ConfigurableViewer {

	void toggle(ViewerFilter filter);

	boolean isActive(ViewerFilter filter);

	void setContentProvider(ContentProviderAction contentProviderAction);

	void configureContentProvider(ViewerInputConfiguration configuration);

}
