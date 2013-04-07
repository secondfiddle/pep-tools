package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;

public abstract class ContentProviderAction extends Action {

	private final ConfigurableViewer configurableViewer;

	public ContentProviderAction(ConfigurableViewer configurableViewer) {
		super("", AS_RADIO_BUTTON);
		this.configurableViewer = configurableViewer;
	}

	public abstract IContentProvider createContentProvider();

	public void run() {
		if (isChecked()) {
			configurableViewer.setContentProvider(createContentProvider());
		}
	}

}