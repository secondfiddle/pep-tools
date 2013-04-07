package uk.org.secondfiddle.pep.features.action;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import uk.org.secondfiddle.pep.features.FeatureExplorerConstants;
import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.viewer.ChildFeatureViewerFilter;
import uk.org.secondfiddle.pep.features.viewer.FeatureIndex;

public class ChildFilterAction extends ViewerFilterAction {

	public ChildFilterAction(ConfigurableViewer configurableViewer, FeatureIndex featureIndex) {
		super(configurableViewer, new ChildFeatureViewerFilter(featureIndex));

		setToolTipText("Show Included Features at Top Level");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(FeatureExplorerConstants.PLUGIN_ID,
				"icons/inher_co.gif"));
	}

}
