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
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.editor.feature.FeatureEditor;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.ViewPart;

import uk.org.secondfiddle.pep.features.action.CollapseAllAction;
import uk.org.secondfiddle.pep.features.action.ContentProviderAction;
import uk.org.secondfiddle.pep.features.action.FilterFeatureChildAction;
import uk.org.secondfiddle.pep.features.action.ShowCalleesContentProviderAction;
import uk.org.secondfiddle.pep.features.action.ShowCallersContentProviderAction;
import uk.org.secondfiddle.pep.features.action.ShowPluginsAction;
import uk.org.secondfiddle.pep.features.action.ShowProductsAction;
import uk.org.secondfiddle.pep.features.action.ViewerFilterAction;
import uk.org.secondfiddle.pep.features.support.FeatureAndProductInput;
import uk.org.secondfiddle.pep.features.support.FeatureIndex;
import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.PluginSupport;
import uk.org.secondfiddle.pep.features.support.ProductSupport;
import uk.org.secondfiddle.pep.features.support.RefactoringSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDragSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeLabelProvider;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureExplorerView extends ViewPart implements ConfigurableViewer {

	private final Collection<ViewerFilterAction> viewerFilterActions = new ArrayList<ViewerFilterAction>();

	private final Collection<ViewerFilter> viewerFilters = new ArrayList<ViewerFilter>();

	private Action showPluginsAction;

	private TreeViewer viewer;

	private FeatureIndex featureIndex;

	@Override
	public void createPartControl(Composite parent) {
		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		ProductModelManager productModelManager = ProductSupport.getManager();
		FeatureAndProductInput input = new FeatureAndProductInput(featureModelManager, productModelManager);

		this.featureIndex = new FeatureIndex(featureModelManager, productModelManager);
		this.viewer = createViewer(parent);

		registerGlobalActions();
		contributeToActionBar(featureModelManager, productModelManager);

		initialiseViewer(input);
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
		viewer.setLabelProvider(new FeatureTreeLabelProvider());

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

	private void initialiseViewer(FeatureAndProductInput input) {
		resetViewerFilters();
		viewer.setInput(input);
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

	private void setContentProvider(ViewerComparator viewerComparator, IContentProvider contentProvider,
			boolean supportsFilters, boolean supportsPlugins) {
		viewer.setComparator(viewerComparator);
		viewer.setContentProvider(contentProvider);

		setViewerFilterActionsEnabled(supportsFilters);
		showPluginsAction.setEnabled(supportsPlugins);

		if (supportsFilters) {
			resetViewerFilters();
		} else {
			viewer.setFilters(new ViewerFilter[0]);
		}
	}

	private void setViewerFilterActionsEnabled(boolean supportsFilters) {
		for (ViewerFilterAction viewerFilterAction : viewerFilterActions) {
			viewerFilterAction.setEnabled(supportsFilters);
		}
	}

	@Override
	public void configureContentProvider(ViewerInputConfiguration configuration) {
		configuration.configure(viewer.getInput());
		viewer.refresh();
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

	private void contributeToActionBar(FeatureModelManager featureModelManager, ProductModelManager productModelManager) {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		toolBarManager.add(new CollapseAllAction(viewer));
		toolBarManager.add(new Separator());

		ContentProviderAction calleesAction = new ShowCalleesContentProviderAction(this, featureModelManager,
				productModelManager);
		calleesAction.setChecked(true);
		toolBarManager.add(calleesAction);

		ContentProviderAction callersAction = new ShowCallersContentProviderAction(this, featureModelManager,
				productModelManager, featureIndex);
		toolBarManager.add(callersAction);
		toolBarManager.add(new Separator());

		ViewerFilterAction filterFeatureChildAction = new FilterFeatureChildAction(this, featureIndex);
		toolBarManager.add(filterFeatureChildAction);

		showPluginsAction = new ShowPluginsAction(this);
		toolBarManager.add(showPluginsAction);
		Action showProductsAction = new ShowProductsAction(this);
		toolBarManager.add(showProductsAction);

		setContentProvider(calleesAction);
		setContentProvider(callersAction);
		registerFilterAction(filterFeatureChildAction);

		actionBars.updateActionBars();
	}

	private void registerFilterAction(ViewerFilterAction filterAction) {
		viewerFilterActions.add(filterAction);
		if (!filterAction.isChecked()) {
			viewerFilters.add(filterAction.getViewerFilter());
		}
	}

	@Override
	public void setContentProvider(ContentProviderAction contentProviderAction) {
		if (contentProviderAction.isChecked()) {
			setContentProvider(contentProviderAction.createViewerComparator(),
					contentProviderAction.createContentProvider(), contentProviderAction.isSupportsFilters(),
					contentProviderAction.isSupportsPlugins());
		}
	}

	private void handleDoubleClick() {
		IFeatureModel selectedFeatureModel = getSelectedFeatureModel();
		if (selectedFeatureModel != null) {
			FeatureEditor.openFeatureEditor(selectedFeatureModel);
			return;
		}

		IPluginModelBase selectedPluginModel = getSelectedPluginModel();
		if (selectedPluginModel != null) {
			ManifestEditor.openPluginEditor(selectedPluginModel);
			return;
		}

		IProductModel selectedProductModel = getSelectedProductModel();
		if (selectedProductModel != null) {
			ProductSupport.openProductEditor(selectedProductModel);
		}
	}

	private void handleRename() {
		IFeatureModel selectedFeatureModel = getSelectedEditableFeatureModel();
		if (selectedFeatureModel != null) {
			RefactoringSupport.renameFeature(selectedFeatureModel, getSite().getShell());
		}
	}

	private void handleDelete() {
		Collection<IIdentifiable> features = getSelectedFeaturesOrChildren();
		boolean featureDeletionCompleted = RefactoringSupport
				.deleteFeaturesOrReferences(features, getSite().getShell());

		if (featureDeletionCompleted) {
			Collection<IFeaturePlugin> plugins = getSelectedPlugins();
			RefactoringSupport.deletePluginReferences(plugins);

			Collection<IProductFeature> productFeatures = getSelectedProductFeatures();
			RefactoringSupport.deleteProductFeatures(productFeatures);
		}
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

	private IPluginModelBase getSelectedPluginModel() {
		return PluginSupport.toSinglePluginModel(viewer.getSelection());
	}

	private Collection<IFeaturePlugin> getSelectedPlugins() {
		return PluginSupport.toFeaturePlugins(viewer.getSelection());
	}

	private Collection<IProductFeature> getSelectedProductFeatures() {
		return ProductSupport.toProductFeatures(viewer.getSelection());
	}

	private IProductModel getSelectedProductModel() {
		return ProductSupport.toSingleProductModel(viewer.getSelection());
	}

}
