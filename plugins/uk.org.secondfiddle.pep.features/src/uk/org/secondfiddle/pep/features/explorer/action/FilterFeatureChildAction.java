package uk.org.secondfiddle.pep.features.explorer.action;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.org.secondfiddle.pep.features.explorer.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.explorer.FeatureExplorerConstants;
import uk.org.secondfiddle.pep.features.explorer.viewer.FeatureChildViewerFilter;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;

public class FilterFeatureChildAction extends ViewerFilterAction {

	public FilterFeatureChildAction(ConfigurableViewer configurableViewer, FeatureIndex featureIndex) {
		super(configurableViewer, new FeatureChildViewerFilter(featureIndex));

		setToolTipText("Show Included Features at Top Level");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(FeatureExplorerConstants.PLUGIN_ID,
				"icons/inher_co.gif"));
	}

}
