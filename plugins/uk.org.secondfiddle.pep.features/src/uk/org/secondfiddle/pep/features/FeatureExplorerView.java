package uk.org.secondfiddle.pep.features;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.internal.core.FeatureModelManager;
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

import uk.org.secondfiddle.pep.features.action.ChildFilterAction;
import uk.org.secondfiddle.pep.features.action.CollapseAllAction;
import uk.org.secondfiddle.pep.features.action.ContentProviderAction;
import uk.org.secondfiddle.pep.features.action.ShowCalleesContentProviderAction;
import uk.org.secondfiddle.pep.features.action.ShowCallersContentProviderAction;
import uk.org.secondfiddle.pep.features.action.ViewerFilterAction;
import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureIndex;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDragSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureViewerComparator;

public class FeatureExplorerView extends ViewPart implements ConfigurableViewer {

	private final Collection<ViewerFilter> viewerFilters = new ArrayList<ViewerFilter>();

	private TreeViewer viewer;

	private FeatureIndex featureIndex;

	@Override
	public void createPartControl(Composite parent) {
		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		this.featureIndex = new FeatureIndex(featureModelManager);
		this.viewer = createViewer(parent);

		registerGlobalActions();
		contributeToActionBar();

		initialiseViewer(featureModelManager);
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		featureIndex.dispose();
	}

	private TreeViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setLabelProvider(new PDELabelProvider());
		viewer.setComparator(new FeatureViewerComparator());

		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDragSupport(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDropSupport(viewer));

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick();
			}
		});

		return viewer;
	}

	private void initialiseViewer(FeatureModelManager featureModelManager) {
		resetViewerFilters();
		viewer.setInput(featureModelManager);
	}

	private void resetViewerFilters() {
		viewer.setFilters(viewerFilters.toArray(new ViewerFilter[viewerFilters.size()]));
	}

	@Override
	public void toggle(ViewerFilter filter) {
		if (viewerFilters.contains(filter)) {
			viewerFilters.remove(filter);
		} else {
			viewerFilters.add(filter);
		}
		resetViewerFilters();
	}

	@Override
	public boolean isActive(ViewerFilter filter) {
		return viewerFilters.contains(filter);
	}

	@Override
	public void setContentProvider(IContentProvider contentProvider) {
		viewer.setContentProvider(contentProvider);
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

	private void contributeToActionBar() {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		toolBarManager.add(new CollapseAllAction(viewer));
		toolBarManager.add(new Separator());

		ContentProviderAction calleesAction = new ShowCalleesContentProviderAction(this);
		calleesAction.setChecked(true);
		registerContentProviderAction(calleesAction);
		toolBarManager.add(calleesAction);

		ContentProviderAction callersAction = new ShowCallersContentProviderAction(this, featureIndex);
		registerContentProviderAction(callersAction);
		toolBarManager.add(callersAction);
		toolBarManager.add(new Separator());

		ViewerFilterAction childFilterAction = new ChildFilterAction(this, featureIndex);
		childFilterAction.setChecked(true);
		registerFilterAction(childFilterAction);
		toolBarManager.add(childFilterAction);

		actionBars.updateActionBars();
	}

	private void registerFilterAction(ViewerFilterAction filterAction) {
		if (!filterAction.isChecked()) {
			viewerFilters.add(filterAction.getViewerFilter());
		}
	}

	private void registerContentProviderAction(ContentProviderAction contentProviderAction) {
		if (contentProviderAction.isChecked()) {
			setContentProvider(contentProviderAction.createContentProvider());
		}
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
