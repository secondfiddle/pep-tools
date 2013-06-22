package uk.org.secondfiddle.pep.features.refactor;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.refactoring.RefactoringInfo;

@SuppressWarnings("restriction")
public class ProductRefactoringInfo extends RefactoringInfo {

	private final IProductModel productModel;

	public ProductRefactoringInfo(IProductModel productModel) {
		this.productModel = productModel;
	}

	@Override
	public String getCurrentValue() {
		return productModel.getProduct().getId();
	}

	public IProductModel getModel() {
		return productModel;
	}

	@Override
	public IPluginModelBase getBase() {
		throw new UnsupportedOperationException("Not a plugin");
	}

}
