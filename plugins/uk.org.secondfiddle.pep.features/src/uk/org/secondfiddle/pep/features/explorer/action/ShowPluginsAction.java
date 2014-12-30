package uk.org.secondfiddle.pep.features.explorer.action;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.org.secondfiddle.pep.features.explorer.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.explorer.FeatureExplorerConstants;
import uk.org.secondfiddle.pep.features.explorer.ViewerInputConfiguration;
import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;

public class ShowPluginsAction extends Action implements ViewerInputConfiguration {

	private final ConfigurableViewer configurableViewer;

	public ShowPluginsAction(ConfigurableViewer configurableViewer) {
		super("", AS_CHECK_BOX);
		this.configurableViewer = configurableViewer;

		setToolTipText("Show Included Plugins and Fragments");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(FeatureExplorerConstants.PLUGIN_ID,
				"icons/plugins_and_fragments.gif"));
	}

	@Override
	public void run() {
		configurableViewer.configureContentProvider(this);
	}

	@Override
	public void configure(Object inputElement) {
		FeatureAndProductInput input = (FeatureAndProductInput) inputElement;
		input.setIncludePlugins(isChecked());
	}

}
