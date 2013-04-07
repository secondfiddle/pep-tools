package uk.org.secondfiddle.pep.features.refactor;

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
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringWizardDialog2;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.util.IdUtil;
import org.eclipse.swt.widgets.Shell;

public class RefactoringSupport {

	public static void addIncludedFeature(final IFeatureModel target, final IFeatureModel featureToInclude) {
		SafeRunnable.run(new SafeRunnable() {
			@Override
			public void run() throws Exception {
				IFeature feature = target.getFeature();
				IFeatureChild child = target.getFactory().createChild();
				child.setId(featureToInclude.getFeature().getId());
				child.setVersion("0.0.0");
				feature.addIncludedFeatures(new IFeatureChild[] { child });
				((IEditableModel) target).save();
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
				String oldName = featureModel.getFeature().getId();

				// rename feature
				featureModel.getFeature().setId(newName);
				((IEditableModel) featureModel).save();

				// update references
				updateFeatureReferences(oldName, newName);

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

	private static void updateFeatureReferences(String oldName, String newName) throws CoreException {
		FeatureModelManager featureModelManager = PDECore.getDefault().getFeatureModelManager();
		for (IFeatureModel workspaceModel : featureModelManager.getWorkspaceModels()) {
			for (IFeatureChild includedFeature : workspaceModel.getFeature().getIncludedFeatures()) {
				if (includedFeature.getId().equals(oldName) && includedFeature instanceof IEditableModel) {
					includedFeature.setId(newName);
					((IEditableModel) includedFeature).save();
				}
			}
		}
	}

	private static IProject getProject(IFeatureModel featureModel) {
		return (IProject) featureModel.getAdapter(IResource.class);
	}

	private static IPath getNewLocation(IProject project, String newName) {
		IPath location = project.getLocation();
		return location.removeLastSegments(1).append(newName);
	}

}
