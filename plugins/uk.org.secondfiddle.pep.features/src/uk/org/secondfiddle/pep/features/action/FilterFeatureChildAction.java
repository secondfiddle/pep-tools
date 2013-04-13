package uk.org.secondfiddle.pep.features.action;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.FeatureExplorerConstants;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;
import uk.org.secondfiddle.pep.features.viewer.FeatureChildViewerFilter;

public class FilterFeatureChildAction extends ViewerFilterAction {

	public FilterFeatureChildAction(ConfigurableViewer configurableViewer, FeatureIndex featureIndex) {
		super(configurableViewer, new FeatureChildViewerFilter(featureIndex));

		setToolTipText("Show Included Features at Top Level");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(FeatureExplorerConstants.PLUGIN_ID,
				"icons/inher_co.gif"));
	}

}
