package uk.org.secondfiddle.pep.features.refactoring;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.refactoring.GeneralRenameIDWizardPage;
import org.eclipse.pde.internal.ui.refactoring.RefactoringPluginInfo;
import org.eclipse.swt.widgets.Composite;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class RenamePluginWizardPage extends GeneralRenameIDWizardPage {

	private final IPluginModelBase model;

	public RenamePluginWizardPage(String title, RefactoringPluginInfo info) {
		super(title, info);
		this.model = info.getBase();
	}

	@Override
	protected void createUpdateReferences(Composite composite) {
		// Not relevant
	}

	@Override
	protected String validateId(String id) {
		return RefactoringSupport.validateRenamePlugin(model, id);
	}

}
