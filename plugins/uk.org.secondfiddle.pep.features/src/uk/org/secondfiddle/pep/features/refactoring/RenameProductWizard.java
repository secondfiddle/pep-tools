package uk.org.secondfiddle.pep.features.refactoring;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class RenameProductWizard extends RefactoringWizard {

	private static final String TITLE = "Rename Product";

	private final ProductRefactoringInfo refactoringInfo;

	public RenameProductWizard(IProductModel productModel) {
		super(new EmptyRefactoring(), RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.NO_PREVIEW_PAGE);
		setWindowTitle(TITLE);

		this.refactoringInfo = new ProductRefactoringInfo(productModel);
	}

	@Override
	public boolean performFinish() {
		RefactoringSupport.renameProduct(refactoringInfo.getModel(), refactoringInfo.getNewValue());
		return true;
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RenameProductWizardPage(TITLE, refactoringInfo));
	}

}
