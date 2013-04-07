package uk.org.secondfiddle.pep.features.refactor;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;


public class RenameFeatureWizard extends RefactoringWizard {

	private static final String TITLE = "Rename Feature";

	private final FeatureRefactoringInfo refactoringInfo;

	public RenameFeatureWizard(IFeatureModel featureModel) {
		super(new EmptyRefactoring(), RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.NO_PREVIEW_PAGE);
		setWindowTitle(TITLE);

		this.refactoringInfo = new FeatureRefactoringInfo(featureModel);
	}

	@Override
	public boolean performFinish() {
		RefactoringSupport.renameFeature(refactoringInfo.getModel(), refactoringInfo.getNewValue());
		return true;
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RenameFeatureWizardPage(TITLE, refactoringInfo));
	}

}
