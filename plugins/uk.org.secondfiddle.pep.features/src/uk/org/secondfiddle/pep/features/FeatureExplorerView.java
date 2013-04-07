package uk.org.secondfiddle.pep.features;

import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.editor.feature.FeatureEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeContentProvider;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDragSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureViewerComparator;

public class FeatureExplorerView extends ViewPart {

	private TreeViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
		viewer = createViewer(parent);
		registerGlobalActions();
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private TreeViewer createViewer(Composite parent) {
		FeatureModelManager featureModelManager = PDECore.getDefault().getFeatureModelManager();

		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setContentProvider(new FeatureTreeContentProvider(featureModelManager));
		viewer.setLabelProvider(new PDELabelProvider());
		viewer.setInput(featureModelManager);

		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDragSupport(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDropSupport(viewer));

		viewer.setComparator(new FeatureViewerComparator());

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

		return viewer;
	}

	private void registerGlobalActions() {
		IActionBars actionBars = getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), new Action() {
			public void run() {
				handleRename();
			}
		});
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new Action() {
			@Override
			public void run() {
				handleDelete();
			}
		});
	}

	private void handleDoubleClick() {
		IFeatureModel selectedFeatureModel = getSelectedFeatureModel();
		if (selectedFeatureModel != null) {
			FeatureEditor.openFeatureEditor(selectedFeatureModel);
		}
	}

	private void handleRename() {
		IFeatureModel selectedFeatureModel = getSelectedEditableFeatureModel();
		if (selectedFeatureModel != null) {
			RefactoringSupport.renameFeature(selectedFeatureModel, getSite().getShell());
		}
	}

	protected void handleDelete() {
		Collection<IIdentifiable> features = getSelectedFeaturesOrChildren();
		RefactoringSupport.deleteFeaturesOrReferences(features, getSite().getShell());
	}

	private IFeatureModel getSelectedFeatureModel() {
		return FeatureSupport.toSingleFeatureModel(viewer.getSelection());
	}

	private IFeatureModel getSelectedEditableFeatureModel() {
		return FeatureSupport.toEditableFeatureModel(getSelectedFeatureModel());
	}

	private Collection<IIdentifiable> getSelectedFeaturesOrChildren() {
		return FeatureSupport.toFeaturesOrChildren(viewer.getSelection());
	}

}
