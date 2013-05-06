package uk.org.secondfiddle.pep.products.ui;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.ProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.context.XMLInputContext;
import org.eclipse.pde.internal.ui.editor.feature.FeatureInputContext;
import org.eclipse.pde.internal.ui.editor.product.ProductInputContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IURIEditorInput;

/**
 * Adapted from {@link ProductInputContext} and {@link FeatureInputContext}.
 */
@SuppressWarnings("restriction")
public class ProductXMLInputContext extends XMLInputContext {

	public static final String CONTEXT_ID = ProductInputContext.CONTEXT_ID;

	public ProductXMLInputContext(PDEFormEditor editor, IEditorInput input, boolean primary) {
		super(editor, input, primary);
		create();
	}

	@Override
	public String getId() {
		return CONTEXT_ID;
	}

	@Override
	protected IBaseModel createModel(IEditorInput input) throws CoreException {
		IProductModel model = null;
		if (input instanceof IStorageEditorInput) {
			try {
				if (input instanceof IFileEditorInput) {
					IFile file = ((IFileEditorInput) input).getFile();
					model = new EditableWorkspaceProductModel(file);
					model.load();
				} else if (input instanceof IStorageEditorInput) {
					InputStream is = new BufferedInputStream(((IStorageEditorInput) input).getStorage().getContents());
					model = new ProductModel();
					model.load(is, false);
				}
			} catch (CoreException e) {
				PDEPlugin.logException(e);
				return null;
			}
		} else if (input instanceof IURIEditorInput) {
			IFileStore store = EFS.getStore(((IURIEditorInput) input).getURI());
			InputStream is = store.openInputStream(EFS.CACHE, new NullProgressMonitor());
			model = new ProductModel();
			model.load(is, false);
		}
		return model;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void addTextEditOperation(ArrayList ops, IModelChangedEvent event) {
	}

	@Override
	protected void flushModel(IDocument doc) {
		if (!(getModel() instanceof IEditable)) {
			return;
		}
		IEditable editableModel = (IEditable) getModel();
		if (editableModel.isDirty() == false) {
			return;
		}
		try {
			StringWriter swriter = new StringWriter();
			PrintWriter writer = new PrintWriter(swriter);
			editableModel.save(writer);
			writer.flush();
			swriter.close();
			doc.set(swriter.toString());
		} catch (IOException e) {
			PDEPlugin.logException(e);
		}
	}

	@Override
	protected boolean synchronizeModel(IDocument doc) {
		IProductModel model = (IProductModel) getModel();

		boolean cleanModel = true;
		String text = doc.get();
		try {
			InputStream stream = new ByteArrayInputStream(text.getBytes("UTF8"));
			try {
				model.reload(stream, false);
			} catch (CoreException e) {
				cleanModel = false;
			}
			try {
				stream.close();
			} catch (IOException e) {
			}
		} catch (UnsupportedEncodingException e) {
			PDEPlugin.logException(e);
		}
		return cleanModel;
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void reorderInsertEdits(ArrayList ops) {
	}

	@Override
	protected String getPartitionName() {
		return "___prod_partition";
	}

}
