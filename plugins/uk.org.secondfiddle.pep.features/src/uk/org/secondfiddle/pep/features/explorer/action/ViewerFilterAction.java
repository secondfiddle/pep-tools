package uk.org.secondfiddle.pep.features.explorer.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ViewerFilter;

import uk.org.secondfiddle.pep.features.explorer.ConfigurableViewer;

public abstract class ViewerFilterAction extends Action {

	private final ConfigurableViewer configurableViewer;

	private final ViewerFilter filter;

	public ViewerFilterAction(ConfigurableViewer configurableViewer, ViewerFilter filter) {
		super("", AS_CHECK_BOX);
		this.configurableViewer = configurableViewer;
		this.filter = filter;
		setChecked(configurableViewer.isActive(filter));
	}

	@Override
	public void run() {
		configurableViewer.toggle(filter);
	}

	public ViewerFilter getViewerFilter() {
		return filter;
	}

}
