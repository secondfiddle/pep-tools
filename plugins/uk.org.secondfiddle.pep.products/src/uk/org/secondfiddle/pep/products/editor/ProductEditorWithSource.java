package uk.org.secondfiddle.pep.products.editor;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.pde.internal.ui.editor.product.ProductEditor;
import org.eclipse.pde.internal.ui.editor.product.ProductInputContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.ide.FileStoreEditorInput;

@SuppressWarnings("restriction")
public class ProductEditorWithSource extends ProductEditor {

	public static String ID = "uk.org.secondfiddle.pep.products.editor.productEditorWithSource";

	@Override
	protected String getEditorID() {
		return ID;
	}

	@Override
	protected void addEditorPages() {
		super.addEditorPages();
		addSourcePage(ProductXMLInputContext.CONTEXT_ID);
	}

	@Override
	protected PDESourcePage createSourcePage(PDEFormEditor editor, String title, String name, String contextId) {
		if (contextId.equals(ProductXMLInputContext.CONTEXT_ID))
			return new ProductSourcePage(editor, title, name);
		return super.createSourcePage(editor, title, name, contextId);
	}

	/**
	 * Adapted from {@link ProductEditor}, replacing references to
	 * {@link ProductInputContext}.
	 */
	@Override
	protected void createResourceContexts(InputContextManager manager, IFileEditorInput input) {
		manager.putContext(input, new ProductXMLInputContext(this, input, true));
		manager.monitorFile(input.getFile());
	}

	/**
	 * Adapted from {@link ProductEditor}, replacing references to
	 * {@link ProductInputContext}.
	 */
	@Override
	protected void createSystemFileContexts(InputContextManager manager, FileStoreEditorInput input) {
		File file = new File(input.getURI());
		if (file != null) {
			String name = file.getName();
			if (name.endsWith(".product")) {
				IFileStore store;
				try {
					store = EFS.getStore(file.toURI());
					IEditorInput in = new FileStoreEditorInput(store);
					manager.putContext(in, new ProductXMLInputContext(this, in, true));
				} catch (CoreException e) {
					PDEPlugin.logException(e);
				}
			}
		}
	}

	/**
	 * Adapted from {@link ProductEditor}, replacing references to
	 * {@link ProductInputContext}.
	 */
	@Override
	protected void createStorageContexts(InputContextManager manager, IStorageEditorInput input) {
		if (input.getName().endsWith(".product")) {
			manager.putContext(input, new ProductXMLInputContext(this, input, true));
		}
	}

}
