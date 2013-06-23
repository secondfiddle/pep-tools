package uk.org.secondfiddle.pep.features.refactor;

import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.ui.refactoring.RefactoringPluginInfo;

import uk.org.secondfiddle.pep.features.support.RefactoringSupport;

@SuppressWarnings("restriction")
public class RenamePluginWizard extends RefactoringWizard {

	private static final String TITLE = "Rename Plugin";

	private final RefactoringPluginInfo refactoringInfo;

	public RenamePluginWizard(IPluginModelBase pluginModel) {
		super(new EmptyRefactoring(), RefactoringWizard.DIALOG_BASED_USER_INTERFACE | RefactoringWizard.NO_PREVIEW_PAGE);
		setWindowTitle(TITLE);

		this.refactoringInfo = new RefactoringPluginInfo();
		this.refactoringInfo.setSelection(pluginModel);
	}

	@Override
	public boolean performFinish() {
		RefactoringSupport.renamePlugin(refactoringInfo.getBase(), refactoringInfo.getNewValue());
		return true;
	}

	@Override
	protected void addUserInputPages() {
		addPage(new RenamePluginWizardPage(TITLE, refactoringInfo));
	}

}
