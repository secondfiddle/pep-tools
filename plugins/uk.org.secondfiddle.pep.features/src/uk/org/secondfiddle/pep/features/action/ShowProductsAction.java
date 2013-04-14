package uk.org.secondfiddle.pep.features.action;

import org.eclipse.jface.action.Action;
import org.eclipse.pde.internal.ui.PDEPluginImages;

import uk.org.secondfiddle.pep.features.ConfigurableViewer;
import uk.org.secondfiddle.pep.features.ViewerInputConfiguration;
import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;

@SuppressWarnings("restriction")
public class ShowProductsAction extends Action implements ViewerInputConfiguration {

	private final ConfigurableViewer configurableViewer;

	public ShowProductsAction(ConfigurableViewer configurableViewer) {
		super("", AS_CHECK_BOX);
		this.configurableViewer = configurableViewer;

		setToolTipText("Show Products");
		setImageDescriptor(PDEPluginImages.DESC_PRODUCT_DEFINITION);
	}

	@Override
	public void run() {
		configurableViewer.configureContentProvider(this);
	}

	@Override
	public void configure(Object inputElement) {
		FeatureAndProductInput input = (FeatureAndProductInput) inputElement;
		input.setIncludeProducts(isChecked());
	}

}