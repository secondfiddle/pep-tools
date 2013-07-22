package uk.org.secondfiddle.pep.projects.wizard;

import static org.eclipse.pde.ui.templates.AbstractTemplateSection.KEY_PLUGIN_ID;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.workingsets.IWorkingSetIDs;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetComparator;
import org.eclipse.jdt.internal.ui.workingsets.WorkingSetModel;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchImages;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.manager.ProjectTemplateManager;

@SuppressWarnings("restriction")
public class ProjectTemplateWizard extends NewPluginTemplateWizard implements INewWizard, IExecutableExtension {

	private static final String KEY_PROJECT_LOCATION = "projectLocation";

	private static final String KEY_WORKING_SET = "workingSet";

	private final ProjectTemplateManager templateManager;

	private ProjectTemplateSection projectTemplateSection;

	private ProjectTemplate projectTemplate;

	public ProjectTemplateWizard() {
		this.templateManager = ProjectTemplateManager.getInstance();
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		String templateId = config.getDeclaringExtension().getUniqueIdentifier();
		this.projectTemplate = templateManager.getProjectTemplate(templateId);
		this.projectTemplateSection.setProjectTemplate(projectTemplate);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.projectTemplateSection.setSelection(selection);

		String imageName = projectTemplate.getLargeIcon().getId();
		setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(imageName));
		setWindowTitle("New " + projectTemplate.getName());
	}

	@Override
	public boolean performFinish() {
		projectTemplateSection.finish();

		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot workspaceRoot = workspace.getRoot();

					String projectName = projectTemplateSection.getValueString(KEY_PLUGIN_ID);
					IProject project = workspaceRoot.getProject(projectName);
					IProjectDescription description = workspace.newProjectDescription(projectName);
					String location = projectTemplateSection.getValueString(KEY_PROJECT_LOCATION);
					if (location != null) {
						File locationFile = new File(location);
						description.setLocationURI(locationFile.toURI());
					}

					project.create(description, monitor);
					project.open(monitor);

					for (ITemplateSection templateSection : getTemplateSections()) {
						templateSection.execute(project, null, monitor);
					}

					String wsName = projectTemplateSection.getValueString(KEY_WORKING_SET);
					if (wsName != null) {
						IWorkingSetManager wsManager = Workbench.getInstance().getWorkingSetManager();
						IWorkingSet workingSet = wsManager.getWorkingSet(wsName);
						if (workingSet == null) {
							workingSet = wsManager.createWorkingSet(wsName, new IAdaptable[] { project });
							workingSet.setId(IWorkingSetIDs.JAVA);
							wsManager.addWorkingSet(workingSet);
							sortWorkingSets();
						} else {
							wsManager.addToWorkingSets(project, new IWorkingSet[] { workingSet });
						}
					}

				}
			});
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// IWorkingSet[] workingSets = fProjectPage.getSelectedWorkingSets();
		// if (workingSets.length > 0) {
		// getWorkbench().getWorkingSetManager().addToWorkingSets(fProjectProvider.getProject(),
		// workingSets);
		// }

		return true;
	}

	private void sortWorkingSets() {
		PackageExplorerPart packageExplorer = PackageExplorerPart.getFromActivePerspective();
		if (packageExplorer == null) {
			return;
		}
		WorkingSetModel wsModel = packageExplorer.getWorkingSetModel();
		if (wsModel != null) {
			IWorkingSet[] workingSets = wsModel.getActiveWorkingSets();
			Comparator<IWorkingSet> wsComparator = new WorkingSetComparator(true);
			Arrays.sort(workingSets, wsComparator);
			wsModel.setActiveWorkingSets(workingSets);
		}
	}

	@Override
	public ITemplateSection[] createTemplateSections() {
		this.projectTemplateSection = new ProjectTemplateSection();
		return new ITemplateSection[] { projectTemplateSection };
	}

}
