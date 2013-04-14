package uk.org.secondfiddle.pep.products;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleContext;

import uk.org.secondfiddle.pep.products.impl.ProductRebuilder;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

public class ProductNatureActivator extends Plugin {

	private ProductRebuilder productRebuilder;

	private ProductModelManager productModelManager;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		productRebuilder = new ProductRebuilder();
		productRebuilder.start();

		productModelManager = ProductModelManager.getInstance();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);

		if (productRebuilder != null) {
			productRebuilder.stop();
			productRebuilder = null;
		}

		if (productModelManager != null) {
			productModelManager.shutdown();
			productModelManager = null;
		}
	}

	public static class ForceActivation implements IStartup {
		@Override
		public void earlyStartup() {
			/*
			 * This will cause the plugin to be activated, necessary to register
			 * for PDE model change events
			 */
		}
	}

}
