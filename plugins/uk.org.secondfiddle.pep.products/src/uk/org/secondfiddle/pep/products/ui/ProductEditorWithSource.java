package uk.org.secondfiddle.pep.products.ui;

import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.product.ProductEditor;
import org.eclipse.pde.internal.ui.editor.product.ProductInputContext;
import org.eclipse.ui.PartInitException;

@SuppressWarnings("restriction")
public class ProductEditorWithSource extends ProductEditor {

	public static String ID = "uk.org.secondfiddle.pep.products.productEditorWithSource";

	@Override
	protected void addEditorPages() {
		super.addEditorPages();

		try {
			ProductSourcePage sourcePage = new ProductSourcePage(this, ProductInputContext.CONTEXT_ID,
					ProductInputContext.CONTEXT_ID);
			InputContext inputContext = fInputContextManager.findContext(sourcePage.getId());
			sourcePage.setInputContext(inputContext);
			addPage(sourcePage, inputContext.getInput());
		} catch (PartInitException e) {
			PDEPlugin.logException(e);
		}
	}

}
