package uk.org.secondfiddle.pep.features;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerFilter;

public interface ConfigurableViewer {

	void toggle(ViewerFilter filter);

	boolean isActive(ViewerFilter filter);

	void setContentProvider(IContentProvider contentProvider);

}
