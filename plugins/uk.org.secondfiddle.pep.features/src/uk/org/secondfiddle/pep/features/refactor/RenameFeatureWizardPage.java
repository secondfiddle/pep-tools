package uk.org.secondfiddle.pep.features.refactor;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.refactoring.GeneralRenameIDWizardPage;
import org.eclipse.pde.internal.ui.refactoring.RefactoringInfo;
import org.eclipse.swt.widgets.Composite;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

public class RenameFeatureWizardPage extends GeneralRenameIDWizardPage {

	private final IFeatureModel model;

	public RenameFeatureWizardPage(String title, FeatureRefactoringInfo info) {
		super(title, info);
		this.model = info.getModel();
	}

	@Override
	protected void createUpdateReferences(Composite composite) {
	}

	@Override
	protected String validateId(String id) {
		return RefactoringSupport.validateRenameFeature(model, id);
	}

}
