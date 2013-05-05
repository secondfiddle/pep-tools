package uk.org.secondfiddle.pep.products.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.pde.internal.ui.editor.ISortableContentOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.XMLSourcePage;
import org.eclipse.pde.internal.ui.editor.product.ProductOutlinePage;

@SuppressWarnings("restriction")
public class ProductSourcePage extends XMLSourcePage {

	public ProductSourcePage(PDEFormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	@Override
	protected ISortableContentOutlinePage createOutlinePage() {
		return new ProductOutlinePage((PDEFormEditor) getEditor());
	}

	@Override
	public ILabelProvider createOutlineLabelProvider() {
		return null;
	}

	@Override
	public ITreeContentProvider createOutlineContentProvider() {
		return null;
	}

	@Override
	public ViewerComparator createOutlineComparator() {
		return null;
	}

	@Override
	public void updateSelection(SelectionChangedEvent e) {
		// do nothing
	}

	@Override
	public void updateSelection(Object object) {
		// do nothing
	}

	@Override
	public boolean isQuickOutlineEnabled() {
		return false;
	}

}
