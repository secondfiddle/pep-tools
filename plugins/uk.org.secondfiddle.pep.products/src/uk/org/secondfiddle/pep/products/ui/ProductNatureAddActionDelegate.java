package uk.org.secondfiddle.pep.products.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;

public class ProductNatureAddActionDelegate implements IActionDelegate {

	private ISelection selection = null;

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

	@Override
	public void run(IAction action) {
		new ProductNatureAddAction(selection).run();
	}

}
