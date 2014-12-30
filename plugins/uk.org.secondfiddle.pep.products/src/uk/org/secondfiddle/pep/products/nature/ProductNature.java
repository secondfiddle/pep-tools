package uk.org.secondfiddle.pep.products.nature;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.core.natures.BaseProject;

@SuppressWarnings("restriction")
public class ProductNature extends BaseProject {

	public static final String ID = ProductNature.class.getName();

	@Override
	public void configure() throws CoreException {
		addToBuildSpec(ProductBuilder.ID);
	}

	@Override
	public void deconfigure() throws CoreException {
		removeFromBuildSpec(ProductBuilder.ID);
	}

}
