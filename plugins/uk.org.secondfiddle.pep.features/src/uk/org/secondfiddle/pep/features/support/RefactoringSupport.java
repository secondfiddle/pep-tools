package uk.org.secondfiddle.pep.features.support;

import static java.util.Collections.singleton;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.LABEL_DELETE_FEATURE;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.LABEL_DELETE_FEATURES;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.LABEL_REMOVE_IMPORT;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.LABEL_REMOVE_IMPORTS;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.MESSAGE_DELETE_FEATURES_OR_REFS;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.MESSAGE_DELETE_FEATURE_OR_REF;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.TITLE_DELETE_FEATURE;
import static uk.org.secondfiddle.pep.features.FeatureExplorerConstants.TITLE_DELETE_FEATURES;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.eclipse.core.internal.resources.LinkDescription;
import org.eclipse.core.internal.resources.Project;
import org.eclipse.core.internal.utils.FileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.ModelEntry;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.bundle.BundlePluginBase;
import org.eclipse.pde.internal.core.feature.FeaturePlugin;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.util.CoreUtility;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.pde.internal.ui.util.ModelModification;
import org.eclipse.pde.internal.ui.util.PDEModelUtility;
import org.eclipse.swt.widgets.Shell;

import uk.org.secondfiddle.pep.features.refactor.RenameFeatureWizard;
import uk.org.secondfiddle.pep.features.refactor.RenamePluginWizard;
import uk.org.secondfiddle.pep.features.refactor.RenameProductWizard;

@SuppressWarnings("restriction")
public class RefactoringSupport {

	private static final Comparator<IIdentifiable> IDENTIFIABLE_COMPARATOR = new Comparator<IIdentifiable>() {
		@Override
		public int compare(IIdentifiable i1, IIdentifiable i2) {
			return i1.getId().compareTo(i2.getId());
		}
	};

	private static final Comparator<IProductFeature> PRODUCT_FEATURE_COMPARATOR = new Comparator<IProductFeature>() {
		@Override
		public int compare(IProductFeature i1, IProductFeature i2) {
			return i1.getId().compareTo(i2.getId());
		}
	};

	public static void addIncludedFeatures(final IFeatureModel parentModel,
			final Collection<IFeatureModel> featureModelsToInclude) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();

				Collection<IFeatureChild> includes = new TreeSet<IFeatureChild>(IDENTIFIABLE_COMPARATOR);
				includes.addAll(Arrays.asList(feature.getIncludedFeatures()));

				feature.removeIncludedFeatures(feature.getIncludedFeatures());
				for (IFeatureModel featureModelToInclude : featureModelsToInclude) {
					includes.add(createInclude(parentModel, featureModelToInclude));
				}

				feature.addIncludedFeatures(includes.toArray(new IFeatureChild[includes.size()]));
				((IEditableModel) parentModel).save();
			}
		});
	}

	private static IFeatureChild createInclude(final IFeatureModel parentModel, IFeatureModel featureModelToInclude)
			throws CoreException {
		IFeatureChild child = parentModel.getFactory().createChild();
		child.setId(featureModelToInclude.getFeature().getId());
		child.setVersion("0.0.0");
		return child;
	}

	public static void addIncludedPlugins(final IFeatureModel parentModel,
			final Collection<IPluginModelBase> pluginModelsToInclude) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();

				Collection<IFeaturePlugin> includes = new TreeSet<IFeaturePlugin>(IDENTIFIABLE_COMPARATOR);
				includes.addAll(Arrays.asList(feature.getPlugins()));

				feature.removePlugins(feature.getPlugins());
				for (IPluginModelBase pluginModelToInclude : pluginModelsToInclude) {
					includes.add(createInclude(parentModel, pluginModelToInclude));
				}

				feature.addPlugins(includes.toArray(new IFeaturePlugin[includes.size()]));
				((IEditableModel) parentModel).save();
			}
		});
	}

	private static IFeaturePlugin createInclude(final IFeatureModel parentModel,
			final IPluginModelBase pluginModelToInclude) throws CoreException {
		FeaturePlugin plugin = (FeaturePlugin) parentModel.getFactory().createPlugin();
		plugin.setId(pluginModelToInclude.getPluginBase().getId());
		plugin.setVersion("0.0.0");
		plugin.setFragment(pluginModelToInclude.isFragmentModel());
		plugin.setUnpack(CoreUtility.guessUnpack(pluginModelToInclude.getBundleDescription()));
		return plugin;
	}

	public static void addProductFeatures(final IProductModel productModel,
			final Collection<IFeatureModel> featureModelsToInclude) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IProduct product = productModel.getProduct();

				Collection<IProductFeature> includes = new TreeSet<IProductFeature>(PRODUCT_FEATURE_COMPARATOR);
				includes.addAll(Arrays.asList(product.getFeatures()));

				product.removeFeatures(product.getFeatures());
				for (IFeatureModel featureModelToInclude : featureModelsToInclude) {
					includes.add(createInclude(productModel, featureModelToInclude));
				}

				product.addFeatures(includes.toArray(new IProductFeature[includes.size()]));
				((IEditableModel) productModel).save();
			}
		});
	}

	private static IProductFeature createInclude(IProductModel productModel, IFeatureModel featureModelToInclude) {
		IProductFeature productFeature = productModel.getFactory().createFeature();
		productFeature.setId(featureModelToInclude.getFeature().getId());
		return productFeature;
	}

	public static void removeIncludedFeatures(final IFeatureModel parentModel,
			final Collection<IFeatureModel> featureModelsToRemove) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();

				Collection<String> featureModelIdsToRemove = new HashSet<String>();
				for (IFeatureModel featureModelToRemove : featureModelsToRemove) {
					featureModelIdsToRemove.add(featureModelToRemove.getFeature().getId());
				}

				Collection<IFeatureChild> removals = new ArrayList<IFeatureChild>();
				for (IFeatureChild child : feature.getIncludedFeatures()) {
					if (featureModelIdsToRemove.contains(child.getId())) {
						removals.add(child);
					}
				}

				feature.removeIncludedFeatures(removals.toArray(new IFeatureChild[removals.size()]));
				((IEditableModel) parentModel).save();
			}
		});
	}

	public static void removeIncludedPlugins(final IFeatureModel parentModel,
			final Collection<IFeaturePlugin> pluginsToRemove) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();
				feature.removePlugins(pluginsToRemove.toArray(new IFeaturePlugin[pluginsToRemove.size()]));
				((IEditableModel) parentModel).save();
			}
		});
	}

	public static void removeProductFeatures(final IProductModel productModel,
			final Collection<IProductFeature> featuresToRemove) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IProduct product = productModel.getProduct();

				Collection<String> featureIdsToRemove = new HashSet<String>();
				for (IProductFeature featureToRemove : featuresToRemove) {
					featureIdsToRemove.add(featureToRemove.getId());
				}

				Collection<IProductFeature> removals = new ArrayList<IProductFeature>();
				for (IProductFeature feature : product.getFeatures()) {
					if (featureIdsToRemove.contains(feature.getId())) {
						removals.add(feature);
					}
				}

				product.removeFeatures(removals.toArray(new IProductFeature[removals.size()]));
				((IEditableModel) productModel).save();
			}
		});
	}

	public static void renameProduct(IProductModel productModel, Shell shell) {
		RenameProductWizard wizard = new RenameProductWizard(productModel);
		Dialog dialog = new RefactoringWizardDialog2(shell, wizard);
		dialog.open();
	}

	public static void renameProduct(final IProductModel productModel, final String newId) {
		Job job = new WorkspaceJob("Renaming Product") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				productModel.getProduct().setId(newId);
				((IEditableModel) productModel).save();

				IResource productFile = productModel.getUnderlyingResource();
				productFile.move(getNewLocation(productFile, newId + ".product"), false, monitor);

				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	public static String validateRenameProduct(IProductModel productModel, String id) {
		if (!IdUtil.isValidCompositeID(id)) {
			return "Invalid ID";
		}

		if (id.endsWith(".product")) {
			return "Product ID should not end with '.product'";
		}

		IResource productFile = productModel.getUnderlyingResource();
		IWorkspace workspace = productFile.getWorkspace();
		IStatus nameValidation = workspace.validateName(id + ".product", IResource.FILE);
		if (!nameValidation.isOK()) {
			return nameValidation.getMessage();
		}

		return null;
	}

	public static void renamePlugin(IPluginModelBase pluginModel, Shell shell) {
		RenamePluginWizard wizard = new RenamePluginWizard(pluginModel);
		Dialog dialog = new RefactoringWizardDialog2(shell, wizard);
		dialog.open();
	}

	public static String validateRenamePlugin(IPluginModelBase pluginModel, String name) {
		IProject project = getProject(pluginModel);
		return validateRenameProject(project, name);
	}

	public static void renameFeature(IFeatureModel featureModel, Shell shell) {
		RenameFeatureWizard wizard = new RenameFeatureWizard(featureModel);
		Dialog dialog = new RefactoringWizardDialog2(shell, wizard);
		dialog.open();
	}

	public static String validateRenameFeature(IFeatureModel featureModel, String name) {
		IProject project = getProject(featureModel);
		return validateRenameProject(project, name);
	}

	private static String validateRenameProject(IProject project, String name) {
		if (project.getName().equals(name)) {
			return null;
		}

		if (!IdUtil.isValidCompositeID(name)) {
			return "Invalid ID";
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.getRoot().getProject(name).exists()) {
			return "A project already exists with this name";
		}

		IStatus nameValidation = workspace.validateName(name, IResource.PROJECT);
		if (!nameValidation.isOK()) {
			return nameValidation.getMessage();
		}

		IPath newLocation = getNewLocation(project, name);
		if (newLocation != null) {
			if (newLocation.toFile().exists()) {
				return "A folder already exists at this location";
			}

			IStatus locationValidation = workspace.validateProjectLocation(project, newLocation);
			if (!locationValidation.isOK()) {
				return locationValidation.getMessage();
			}
		}

		return null;
	}

	public static void renamePlugin(final IPluginModelBase pluginModel, final String newName) {
		/*
		 * Wait until the plugin model has been updated post-project-rename so
		 * that it isn't stale.
		 */
		final PluginModelManager modelManager = PluginSupport.getManager();
		modelManager.addPluginModelListener(new IPluginModelListener() {
			@Override
			public void modelsChanged(PluginModelDelta delta) {
				IPluginModelBase replacement = getReplacement(delta, pluginModel);
				if (replacement != null) {
					internalRenamePlugin(replacement, newName);
					modelManager.removePluginModelListener(this);
				}
			}
		});

		IProject project = getProject(pluginModel);
		renameProject(project, newName);
	}

	private static IPluginModelBase getReplacement(PluginModelDelta delta, IPluginModelBase pluginModel) {
		boolean foundRemoved = false;
		for (ModelEntry removed : delta.getRemovedEntries()) {
			if (removed.getId().equals(pluginModel.getPluginBase().getId())) {
				foundRemoved = true;
			}
		}

		if (foundRemoved) {
			for (ModelEntry added : delta.getAddedEntries()) {
				if (added.getId().equals(pluginModel.getPluginBase().getId())) {
					return added.getModel();
				}
			}
		}

		return null;
	}

	public static void internalRenamePlugin(final IPluginModelBase pluginModel, final String newName) {
		Job renamePluginJob = new WorkspaceJob("Renaming Plugin") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				// rename plugin
				setPluginId(pluginModel, newName);

				// update references
				String oldName = pluginModel.getPluginBase().getId();
				updatePluginReferences(oldName, newName);

				return Status.OK_STATUS;
			}
		};
		renamePluginJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		renamePluginJob.schedule();
	}

	private static void setPluginId(IPluginModelBase pluginModel, final String newName) {
		PDEModelUtility.modifyModel(new ModelModification(getProject(pluginModel)) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginModelBase modelBase = (IPluginModelBase) model;
				modelBase.getPluginBase().setId(newName);
			}
		}, new NullProgressMonitor());
	}

	private static void setFragmentHostId(IFragmentModel fragmentModel, final String newName) {
		PDEModelUtility.modifyModel(new ModelModification(getProject(fragmentModel)) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IFragmentModel fragmentModel = (IFragmentModel) model;
				fragmentModel.getFragment().setPluginId(newName);
			}
		}, new NullProgressMonitor());
	}

	private static void setPluginImportId(final IPluginImport importToModify, final String newName) {
		PDEModelUtility.modifyModel(new ModelModification(getProject(importToModify.getPluginModel())) {
			@Override
			protected void modifyModel(IBaseModel model, IProgressMonitor monitor) throws CoreException {
				IPluginModelBase modelBase = (IPluginModelBase) model;
				if (!(modelBase.getPluginBase() instanceof BundlePluginBase)) {
					return;
				}

				BundlePluginBase bundlePluginBase = (BundlePluginBase) modelBase.getPluginBase();
				int index = 0;

				for (IPluginImport pluginImport : bundlePluginBase.getImports()) {
					if (pluginImport.getId().equals(importToModify.getId())) {
						IPluginImport replacement = modelBase.getPluginFactory().createImport();
						replacement.setId(newName);
						replacement.setMatch(pluginImport.getMatch());
						replacement.setOptional(pluginImport.isOptional());
						replacement.setReexported(pluginImport.isReexported());
						replacement.setVersion(pluginImport.getVersion());
						bundlePluginBase.remove(pluginImport);
						bundlePluginBase.add(replacement, index);
						break;
					}
					index++;
				}
			}
		}, new NullProgressMonitor());
	}

	public static void renameFeature(final IFeatureModel featureModel, final String newName) {
		/*
		 * Wait until the feature model has been updated post-project-rename so
		 * that it isn't stale.
		 */
		final FeatureModelManager modelManager = FeatureSupport.getManager();
		modelManager.addFeatureModelListener(new IFeatureModelListener() {
			@Override
			public void modelsChanged(IFeatureModelDelta delta) {
				IFeatureModel replacement = getReplacement(delta, featureModel);
				if (replacement != null) {
					internalRenameFeature(replacement, newName);
					modelManager.removeFeatureModelListener(this);
				}
			}
		});

		IProject project = getProject(featureModel);
		renameProject(project, newName);
	}

	private static IFeatureModel getReplacement(IFeatureModelDelta delta, IFeatureModel featureModel) {
		boolean foundRemoved = false;
		for (IFeatureModel removed : delta.getRemoved()) {
			if (removed.getFeature().getId().equals(featureModel.getFeature().getId())) {
				foundRemoved = true;
			}
		}

		if (foundRemoved) {
			for (IFeatureModel added : delta.getAdded()) {
				if (added.getFeature().getId().equals(featureModel.getFeature().getId())) {
					return added;
				}
			}
		}

		return null;
	}

	public static void internalRenameFeature(final IFeatureModel featureModel, final String newName) {
		Job renameFeatureJob = new WorkspaceJob("Renaming Feature") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				// update references
				String oldName = featureModel.getFeature().getId();
				updateFeatureReferences(oldName, newName);

				// rename feature
				featureModel.getFeature().setId(newName);
				((IEditableModel) featureModel).save();

				return Status.OK_STATUS;
			}
		};
		renameFeatureJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		renameFeatureJob.schedule();
	}

	private static void renameProject(final IProject project, final String newName) {
		Job renameProjectJob = new WorkspaceJob("Renaming Project") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				IPath newLocation = getNewLocation(project, newName);

				// update linked resources
				updateLinkedResources(project, newName);

				// rename/move project
				IProjectDescription description = project.getDescription();
				description.setName(newName);

				if (newLocation != null) {
					description.setLocation(newLocation);
				}

				project.move(description, IResource.FORCE | IResource.SHALLOW, monitor);

				return Status.OK_STATUS;
			}
		};
		renameProjectJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
		renameProjectJob.schedule();
	}

	private static void updateLinkedResources(final IProject project, final String newName) {
		/*
		 * IProject#getDescription() returns a clone of the ProjectDescription
		 * which is missing some components, such as linked resources.
		 */
		Project projectImpl = ((Project) project);
		Map<IPath, LinkDescription> links = projectImpl.internalGetDescription().getLinks();
		if (links == null) {
			return;
		}

		try {
			for (LinkDescription link : links.values()) {
				String oldLinkLocation = link.getLocationURI().toString();
				String newLinkLocation = oldLinkLocation.replace(project.getName(), newName);
				link.setLocationURI(new URI(newLinkLocation));
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean deleteFeaturesOrReferences(Collection<IIdentifiable> features, Shell shell) {
		if (features.isEmpty()) {
			return true;
		}

		IIdentifiable firstFeature = features.iterator().next();

		if (features.size() > 1) {
			return deleteOrRemoveReferences(features, shell);
		} else if (firstFeature instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) firstFeature;
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(featureChild);
			IFeatureModel featureParent = FeatureSupport.toEditableFeatureModel(featureChild.getParent());

			if (featureModel != null && featureParent != null) {
				return deleteOrRemoveReference(featureChild, shell);
			} else if (featureModel != null) {
				return deleteFeature(featureModel, shell);
			} else if (featureParent != null) {
				removeReference(featureChild);
			}
		} else {
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(firstFeature);
			if (featureModel != null) {
				return deleteFeature(featureModel, shell);
			}
		}

		return true;
	}

	public static void deletePluginReferences(Collection<IFeaturePlugin> featurePluginsToDelete) {
		Map<IFeatureModel, Collection<IFeaturePlugin>> references = new HashMap<IFeatureModel, Collection<IFeaturePlugin>>();

		for (IFeaturePlugin featurePlugin : featurePluginsToDelete) {
			IFeatureModel parentModel = FeatureSupport.toEditableFeatureModel(featurePlugin.getParent());
			if (parentModel != null) {
				Collection<IFeaturePlugin> featurePlugins = references.get(parentModel);
				if (featurePlugins == null) {
					featurePlugins = new HashSet<IFeaturePlugin>();
					references.put(parentModel, featurePlugins);
				}
				featurePlugins.add(featurePlugin);
			}
		}

		for (Entry<IFeatureModel, Collection<IFeaturePlugin>> entry : references.entrySet()) {
			removeIncludedPlugins(entry.getKey(), entry.getValue());
		}
	}

	public static void deleteProductFeatures(Collection<IProductFeature> productFeaturesToDelete) {
		Map<IProductModel, Collection<IProductFeature>> references = new HashMap<IProductModel, Collection<IProductFeature>>();

		for (IProductFeature productFeature : productFeaturesToDelete) {
			IProductModel productModel = ProductSupport.toEditableProductModel(productFeature.getProduct());
			if (productModel != null) {
				Collection<IProductFeature> productFeatures = references.get(productModel);
				if (productFeatures == null) {
					productFeatures = new HashSet<IProductFeature>();
					references.put(productModel, productFeatures);
				}
				productFeatures.add(productFeature);
			}
		}

		for (Entry<IProductModel, Collection<IProductFeature>> entry : references.entrySet()) {
			removeProductFeatures(entry.getKey(), entry.getValue());
		}
	}

	private static void removeReference(IFeatureChild featureChild) {
		removeReferences(singleton(featureChild));
	}

	private static void removeReferences(Collection<IFeatureChild> featureChildren) {
		Map<IFeatureModel, Collection<IFeatureModel>> references = new HashMap<IFeatureModel, Collection<IFeatureModel>>();

		for (IFeatureChild featureChild : featureChildren) {
			IFeatureModel parentModel = FeatureSupport.toEditableFeatureModel(featureChild.getParent());
			IFeatureModel childModel = FeatureSupport.toFeatureModel(featureChild);
			if (parentModel != null && childModel != null) {
				Collection<IFeatureModel> children = references.get(parentModel);
				if (children == null) {
					children = new HashSet<IFeatureModel>();
					references.put(parentModel, children);
				}
				children.add(childModel);
			}
		}

		for (Entry<IFeatureModel, Collection<IFeatureModel>> entry : references.entrySet()) {
			removeIncludedFeatures(entry.getKey(), entry.getValue());
		}
	}

	private static boolean deleteFeature(IFeatureModel featureModel, Shell shell) {
		return deleteFeatures(Collections.singleton(featureModel), shell);
	}

	private static boolean deleteFeatures(Collection<IFeatureModel> featureModels, Shell shell) {
		try {
			Collection<IResource> projects = new HashSet<IResource>();
			for (IFeatureModel featureModel : featureModels) {
				IResource project = getProject(featureModel);
				if (project != null) {
					projects.add(project);
				}
			}

			DeleteResourcesWizard refactoringWizard = new DeleteResourcesWizard(projects.toArray(new IResource[0]));
			RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(refactoringWizard);
			int returnCode = op.run(shell, TITLE_DELETE_FEATURE);
			return (returnCode != IDialogConstants.CANCEL_ID);
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}
	}

	private static boolean deleteOrRemoveReference(IFeatureChild featureChild, Shell shell) {
		int deleteButtonIndex = 1;
		String featureId = featureChild.getId();
		String parentId = featureChild.getParent().getFeature().getId();
		Dialog dialog = new MessageDialog(shell, TITLE_DELETE_FEATURE, null, String.format(
				MESSAGE_DELETE_FEATURE_OR_REF, featureId, parentId), MessageDialog.QUESTION, new String[] {
				LABEL_REMOVE_IMPORT, LABEL_DELETE_FEATURE, IDialogConstants.CANCEL_LABEL }, 0);

		int returnCode = dialog.open();
		if (returnCode == IDialogConstants.CANCEL_ID) {
			return false;
		}

		removeReference(featureChild);

		// for deletions, delete the feature project
		if (returnCode == deleteButtonIndex) {
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(featureChild);
			return deleteFeature(featureModel, shell);
		}

		return true;
	}

	private static boolean deleteOrRemoveReferences(Collection<IIdentifiable> features, Shell shell) {
		int deleteButtonIndex = 1;
		Dialog dialog = new MessageDialog(shell, TITLE_DELETE_FEATURES, null, MESSAGE_DELETE_FEATURES_OR_REFS,
				MessageDialog.QUESTION, new String[] { LABEL_REMOVE_IMPORTS, LABEL_DELETE_FEATURES,
						IDialogConstants.CANCEL_LABEL }, 0);

		int returnCode = dialog.open();
		if (returnCode == IDialogConstants.CANCEL_ID) {
			return false;
		}

		// remove references for deletions or import removals
		Collection<IFeatureChild> referenceRemovals = new HashSet<IFeatureChild>();
		for (IIdentifiable feature : features) {
			if (feature instanceof IFeatureChild) {
				referenceRemovals.add((IFeatureChild) feature);
			}
		}
		removeReferences(referenceRemovals);

		// for deletions, delete the feature projects
		if (returnCode == deleteButtonIndex) {
			Collection<IFeatureModel> featureDeletions = new HashSet<IFeatureModel>();
			for (IIdentifiable feature : features) {
				IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(feature);
				if (featureModel != null) {
					featureDeletions.add(featureModel);
				}
			}
			return deleteFeatures(featureDeletions, shell);
		}

		return true;
	}

	private static void updatePluginReferences(String oldName, String newName) throws CoreException {
		PluginModelManager pluginModelManager = PluginSupport.getManager();
		for (IPluginModelBase workspaceModel : pluginModelManager.getWorkspaceModels()) {
			if (workspaceModel.isFragmentModel()) {
				IFragmentModel fragmentModel = (IFragmentModel) workspaceModel;
				if (fragmentModel.getFragment().getPluginId().equals(oldName)) {
					setFragmentHostId(fragmentModel, newName);
				}
			}
			for (IPluginImport pluginImport : workspaceModel.getPluginBase().getImports()) {
				if (pluginImport.getId().equals(oldName)) {
					setPluginImportId(pluginImport, newName);
					break;
				}
			}
		}

		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		for (IFeatureModel workspaceModel : featureModelManager.getWorkspaceModels()) {
			if (!(workspaceModel instanceof IEditableModel)) {
				continue;
			}
			IFeature feature = workspaceModel.getFeature();
			if (feature.getPlugin() != null && feature.getPlugin().equals(oldName)) {
				feature.setPlugin(newName);
			}
			for (IFeaturePlugin featurePlugin : feature.getPlugins()) {
				if (featurePlugin.getId().equals(oldName)) {
					featurePlugin.setId(newName);
					((IEditableModel) workspaceModel).save();
					break;
				}
			}
		}
	}

	private static void updateFeatureReferences(String oldName, String newName) throws CoreException {
		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		for (IFeatureModel workspaceModel : featureModelManager.getWorkspaceModels()) {
			if (!(workspaceModel instanceof IEditableModel)) {
				continue;
			}
			for (IFeatureChild includedFeature : workspaceModel.getFeature().getIncludedFeatures()) {
				if (includedFeature.getId().equals(oldName)) {
					includedFeature.setId(newName);
					((IEditableModel) workspaceModel).save();
					break;
				}
			}
		}
	}

	private static IProject getProject(IAdaptable adaptable) {
		IResource resource = (IResource) adaptable.getAdapter(IResource.class);
		if (resource instanceof IProject) {
			return (IProject) resource;
		} else {
			return null;
		}
	}

	/**
	 * Returns the new project location, or {@code null} if the project is in
	 * (and will remain in) the default location.
	 * 
	 * @see IProjectDescription#getLocationURI()
	 * 
	 * @param project
	 *            the project to get the new location for
	 * @param newName
	 *            the new name of the project
	 * @return
	 */
	private static IPath getNewLocation(IProject project, String newName) {
		try {
			URI locationUri = project.getDescription().getLocationURI();
			if (locationUri == null) {
				return null;
			}

			IPath location = FileUtil.toPath(locationUri);
			return location.removeLastSegments(1).append(newName);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

	private static IPath getNewLocation(IResource resource, String newName) {
		IPath location = resource.getFullPath();
		return location.removeLastSegments(1).append(newName);
	}

}
