package uk.org.secondfiddle.pep.features.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.DeleteResourcesWizard;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.swt.widgets.Shell;

import uk.org.secondfiddle.pep.features.refactor.RenameFeatureWizard;

public class RefactoringSupport {

	private static final String TITLE_DELETE_FEATURE = "Delete Feature";

	private static final String TITLE_DELETE_FEATURES = "Delete Features";

	private static final String MESSAGE_DELETE_FEATURE_OR_REF = "Do you want to delete the feature '%s' or only remove it as an import for feature '%s'?";

	private static final String MESSAGE_DELETE_FEATURES_OR_REFS = "Do you want to delete the selected features or only remove them as imports?";

	private static final String LABEL_REMOVE_IMPORT = "Remove Import";

	private static final String LABEL_DELETE_FEATURE = "Delete Feature";

	private static final String LABEL_REMOVE_IMPORTS = "Remove Imports";

	private static final String LABEL_DELETE_FEATURES = "Delete Features";

	public static void addIncludedFeature(final IFeatureModel parentModel, final IFeatureModel featureModelToInclude) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();
				String featureIdToInclude = featureModelToInclude.getFeature().getId();

				for (IFeatureChild child : feature.getIncludedFeatures()) {
					if (child.getId().equals(featureIdToInclude)) {
						return;
					}
				}

				IFeatureChild child = parentModel.getFactory().createChild();
				child.setId(featureIdToInclude);
				child.setVersion("0.0.0");
				feature.addIncludedFeatures(new IFeatureChild[] { child });

				((IEditableModel) parentModel).save();
			}
		});
	}

	public static void removeIncludedFeature(final IFeatureModel parentModel, final IFeatureModel featureModelToRemove) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = parentModel.getFeature();
				String featureIdToRemove = featureModelToRemove.getFeature().getId();

				for (IFeatureChild child : feature.getIncludedFeatures()) {
					if (child.getId().equals(featureIdToRemove)) {
						feature.removeIncludedFeatures(new IFeatureChild[] { child });
					}
				}

				((IEditableModel) parentModel).save();
			}
		});
	}

	public static void renameFeature(final IFeatureModel featureModel, final Shell shell) {
		RenameFeatureWizard wizard = new RenameFeatureWizard(featureModel);
		Dialog dialog = new RefactoringWizardDialog2(shell, wizard);
		dialog.open();
	}

	public static String validateRenameFeature(IFeatureModel featureModel, String name) {
		if (!IdUtil.isValidCompositeID(name)) {
			return "Invalid ID";
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = getProject(featureModel);
		if (!project.getName().equals(name) && workspace.getRoot().getProject(name).exists()) {
			return "A project already exists with this name";
		}

		IPath newLocation = getNewLocation(project, name);
		IStatus locationValidation = workspace.validateProjectLocation(project, newLocation);
		if (!locationValidation.isOK()) {
			return locationValidation.getMessage();
		}

		return null;
	}

	public static void renameFeature(final IFeatureModel featureModel, final String newName) {
		Job job = new WorkspaceJob("Renaming Feature") {
			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				// update references
				String oldName = featureModel.getFeature().getId();
				updateFeatureReferences(oldName, newName);

				// rename feature
				featureModel.getFeature().setId(newName);
				((IEditableModel) featureModel).save();

				IProject project = getProject(featureModel);
				IPath newLocation = getNewLocation(project, newName);

				// rename/move project
				IProjectDescription description = project.getDescription();
				description.setName(newName);
				description.setLocation(newLocation);
				project.move(description, false, monitor);

				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	public static void deleteFeaturesOrReferences(Collection<IIdentifiable> features, Shell shell) {
		if (features.isEmpty()) {
			return;
		}

		IIdentifiable firstFeature = features.iterator().next();

		if (features.size() > 1) {
			deleteOrRemoveReferences(features, shell);
		} else if (firstFeature instanceof IFeatureChild) {
			IFeatureChild featureChild = (IFeatureChild) firstFeature;
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(featureChild);
			IFeatureModel featureParent = FeatureSupport.toEditableFeatureModel(featureChild.getParent());

			if (featureModel != null && featureParent != null) {
				deleteOrRemoveReference(featureChild, shell);
			} else if (featureModel != null) {
				delete(featureModel, shell);
			} else if (featureParent != null) {
				removeReference(featureChild);
			}
		} else {
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(firstFeature);
			if (featureModel != null) {
				delete(featureModel, shell);
			}
		}
	}

	private static void removeReference(IFeatureChild featureChild) {
		IFeatureModel parentModel = FeatureSupport.toEditableFeatureModel(featureChild.getParent());
		IFeatureModel childModel = FeatureSupport.toFeatureModel(featureChild);
		if (parentModel != null && childModel != null) {
			removeIncludedFeature(parentModel, childModel);
		}
	}

	private static void delete(IFeatureModel featureModel, Shell shell) {
		delete(Collections.singleton(featureModel), shell);
	}

	private static void delete(Collection<IFeatureModel> featureModels, Shell shell) {
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
			op.run(shell, TITLE_DELETE_FEATURE);
		} catch (InterruptedException e) {
			// cancelled
		}
	}

	private static void deleteOrRemoveReference(IFeatureChild featureChild, Shell shell) {
		String featureId = featureChild.getId();
		String parentId = featureChild.getParent().getFeature().getId();
		Dialog dialog = new MessageDialog(shell, TITLE_DELETE_FEATURE, null, String.format(
				MESSAGE_DELETE_FEATURE_OR_REF, featureId, parentId), MessageDialog.QUESTION, new String[] {
				LABEL_REMOVE_IMPORT, LABEL_DELETE_FEATURE, IDialogConstants.CANCEL_LABEL }, 0);
		int returnCode = dialog.open();

		int removeButtonIndex = 0;
		int deleteButtonIndex = 1;
		if (returnCode == removeButtonIndex) {
			removeReference(featureChild);
		} else if (returnCode == deleteButtonIndex) {
			IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(featureChild);
			removeReference(featureChild);
			delete(featureModel, shell);
		}
	}

	private static void deleteOrRemoveReferences(Collection<IIdentifiable> features, Shell shell) {
		Dialog dialog = new MessageDialog(shell, TITLE_DELETE_FEATURES, null, MESSAGE_DELETE_FEATURES_OR_REFS,
				MessageDialog.QUESTION, new String[] { LABEL_REMOVE_IMPORTS, LABEL_DELETE_FEATURES,
						IDialogConstants.CANCEL_LABEL }, 0);
		int returnCode = dialog.open();

		int removeButtonIndex = 0;
		int deleteButtonIndex = 1;
		if (returnCode == removeButtonIndex) {
			for (IIdentifiable feature : features) {
				if (feature instanceof IFeatureChild) {
					removeReference((IFeatureChild) feature);
				}
			}
		} else if (returnCode == deleteButtonIndex) {
			Collection<IFeatureModel> featureModels = new HashSet<IFeatureModel>();
			for (IIdentifiable feature : features) {
				if (feature instanceof IFeatureChild) {
					removeReference((IFeatureChild) feature);
				}
				IFeatureModel featureModel = FeatureSupport.toEditableFeatureModel(feature);
				if (featureModel != null) {
					featureModels.add(featureModel);
				}
			}
			delete(featureModels, shell);
		}
	}

	private static void updateFeatureReferences(String oldName, String newName) throws CoreException {
		FeatureModelManager featureModelManager = FeatureSupport.getManager();
		for (IFeatureModel workspaceModel : featureModelManager.getWorkspaceModels()) {
			for (IFeatureChild includedFeature : workspaceModel.getFeature().getIncludedFeatures()) {
				if (includedFeature.getId().equals(oldName) && workspaceModel instanceof IEditableModel) {
					includedFeature.setId(newName);
					((IEditableModel) workspaceModel).save();
				}
			}
		}
	}

	private static IProject getProject(IFeatureModel featureModel) {
		IResource resource = (IResource) featureModel.getAdapter(IResource.class);
		if (resource instanceof IProject) {
			return (IProject) resource;
		} else {
			return null;
		}
	}

	private static IPath getNewLocation(IProject project, String newName) {
		IPath location = project.getLocation();
		return location.removeLastSegments(1).append(newName);
	}

}
