package uk.org.secondfiddle.pep.features;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.pde.internal.ui.PDEUIMessages;
import org.eclipse.pde.internal.ui.editor.feature.FeatureEditor;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;
import org.eclipse.pde.internal.ui.views.dependencies.OpenPluginDependenciesAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
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
import uk.org.secondfiddle.pep.features.viewer.FeatureElementComparer;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDragSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;
import uk.org.secondfiddle.pep.features.viewer.FeatureTreeLabelProvider;
import uk.org.secondfiddle.pep.features.viewer.RootElementsFilteredTree;
import uk.org.secondfiddle.pep.products.model.ProductModelManager;

@SuppressWarnings("restriction")
public class FeatureExplorerView extends ViewPart implements ConfigurableViewer {

	private Action openAction = new Action("Open") {
		@Override
		public void run() {
			handleOpen();
		}
	};

	private Action renameAction = new Action("Rename...") {
		public void run() {
			handleRename();
		}
	};

	private Action deleteAction = new Action("Delete") {
		@Override
		public void run() {
			handleDelete();
		}
	};

	private final Collection<ViewerFilterAction> viewerFilterActions = new ArrayList<ViewerFilterAction>();

	private final Collection<ViewerFilter> viewerFilters = new ArrayList<ViewerFilter>();

	private PatternFilter patternFilter;

	private Action showPluginsAction;

	private TreeViewer viewer;

	private FeatureIndex featureIndex;

	@Override
	public void createPartControl(Composite parent) {
		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		ProductModelManager productModelManager = ProductSupport.getManager();
		FeatureAndProductInput input = new FeatureAndProductInput(featureModelManager, productModelManager);
		this.featureIndex = new FeatureIndex(featureModelManager, productModelManager);

		FilteredTree filteredTree = createFilteredTree(parent);
		this.viewer = filteredTree.getViewer();
		this.patternFilter = filteredTree.getPatternFilter();
		this.viewerFilters.add(patternFilter);

		registerGlobalActions();
		contributeToActionBar(featureModelManager, productModelManager);
		hookContextMenu();

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

	private FilteredTree createFilteredTree(Composite parent) {
		FilteredTree filteredTree = new RootElementsFilteredTree(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		filteredTree.setInitialText("search for feature");

		PatternFilter patternFilter = filteredTree.getPatternFilter();
		patternFilter.setIncludeLeadingWildcard(true);

		TreeViewer viewer = filteredTree.getViewer();
		viewer.setComparer(new FeatureElementComparer());
		viewer.setLabelProvider(new FeatureTreeLabelProvider());

		viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDragSupport(viewer));
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, new Transfer[] { LocalSelectionTransfer.getTransfer() },
				new FeatureTreeDropSupport(viewer));

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				handleOpen();
			}
		});

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Collection<?> selection = getViewerSelection();
				openAction.setEnabled(!selection.isEmpty());
				renameAction.setEnabled(selection.size() == 1);
				deleteAction.setEnabled(!selection.isEmpty());
			}
		});

		return filteredTree;
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
			viewer.setFilters(new ViewerFilter[] { patternFilter });
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
		actionBars.setGlobalActionHandler(ActionFactory.RENAME.getId(), renameAction);
		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), deleteAction);
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

	private void hookContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openAction);

		IPluginModelBase selectedPluginModel = getSelectedPluginModel();
		if (selectedPluginModel != null) {
			Action dependenciesAction = new OpenPluginDependenciesAction(selectedPluginModel);
			manager.add(dependenciesAction);
			dependenciesAction.setText(PDEUIMessages.PluginsView_openDependencies);
			dependenciesAction.setImageDescriptor(PDEPluginImages.DESC_CALLEES);
		}

		manager.add(new Separator());
		manager.add(renameAction);
		manager.add(new Separator());

		Collection<?> selection = getViewerSelection();
		deleteAction.setText("Delete...");
		if (selection.size() > 1) {
			deleteAction.setText("Delete / Remove Inclusions");
		} else if (selection.size() == 1 && !canDelete(selection.iterator().next())) {
			deleteAction.setText("Delete (Remove Inclusion)");
		}

		manager.add(deleteAction);
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

	private void handleOpen() {
		for (Object selection : getViewerSelection()) {
			IFeatureModel featureModel = FeatureSupport.toFeatureModel(selection);
			if (featureModel != null) {
				FeatureEditor.openFeatureEditor(featureModel);
				continue;
			}

			IPluginModelBase pluginModel = PluginSupport.toPluginModel(selection);
			if (pluginModel != null) {
				ManifestEditor.openPluginEditor(pluginModel);
				continue;
			}

			IProductModel productModel = ProductSupport.toProductModel(selection);
			if (productModel != null) {
				ProductSupport.openProductEditor(productModel);
			}
		}
	}

	private void handleRename() {
		IFeatureModel selectedFeatureModel = getSelectedEditableFeatureModel();
		if (selectedFeatureModel != null) {
			RefactoringSupport.renameFeature(selectedFeatureModel, getSite().getShell());
		}

		IPluginModelBase selectedPluginModel = getSelectedEditablePluginModel();
		if (selectedPluginModel != null) {
			RefactoringSupport.renamePlugin(selectedPluginModel, getSite().getShell());
		}

		IProductModel selectedProductModel = getSelectedEditableProductModel();
		if (selectedProductModel != null) {
			RefactoringSupport.renameProduct(selectedProductModel, getSite().getShell());
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

			Collection<IProductModel> products = getSelectedProductModels();
			RefactoringSupport.deleteProducts(products, getSite().getShell());
		}
	}

	private Collection<?> getViewerSelection() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		} else {
			return Collections.emptyList();
		}
	}

	private boolean canDelete(Object selection) {
		return (selection instanceof IProductModel || selection instanceof IFeatureModel);
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

	private IPluginModelBase getSelectedEditablePluginModel() {
		return PluginSupport.toEditablePluginModel(getSelectedPluginModel());
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

	private Collection<IProductModel> getSelectedProductModels() {
		return ProductSupport.toProductModels(viewer.getSelection());
	}

	private IProductModel getSelectedEditableProductModel() {
		return ProductSupport.toEditableProductModel(getSelectedProductModel());
	}

}
