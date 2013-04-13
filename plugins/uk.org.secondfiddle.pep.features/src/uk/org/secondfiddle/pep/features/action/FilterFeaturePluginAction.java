package uk.org.secondfiddle.pep.features.action;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.FeatureExplorerConstants;
import uk.org.secondfiddle.pep.features.viewer.FeaturePluginViewerFilter;

public class FilterFeaturePluginAction extends ViewerFilterAction {

	public FilterFeaturePluginAction(ConfigurableViewer configurableViewer) {
		super(configurableViewer, new FeaturePluginViewerFilter());

		setToolTipText("Show Included Plugins and Fragments");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(FeatureExplorerConstants.PLUGIN_ID,
				"icons/plugins_and_fragments.gif"));
	}

}
