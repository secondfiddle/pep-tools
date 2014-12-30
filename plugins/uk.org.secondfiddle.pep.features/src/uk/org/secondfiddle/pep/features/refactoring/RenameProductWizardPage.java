package uk.org.secondfiddle.pep.features.refactoring;

import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.refactoring.GeneralRenameIDWizardPage;
import org.eclipse.swt.widgets.Composite;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class RenameProductWizardPage extends GeneralRenameIDWizardPage {

	private final IProductModel model;

	public RenameProductWizardPage(String title, ProductRefactoringInfo info) {
		super(title, info);
		this.model = info.getModel();
	}

	@Override
	protected void createUpdateReferences(Composite composite) {
		// Not relevant
	}

	@Override
	protected String validateId(String id) {
		return RefactoringSupport.validateRenameProduct(model, id);
	}

}
