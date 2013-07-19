package uk.org.secondfiddle.pep.projects.wizard;

import static org.eclipse.pde.ui.templates.AbstractTemplateSection.KEY_PLUGIN_ID;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.WorkbenchImages;

import uk.org.secondfiddle.pep.projects.model.ProjectTemplate;
import uk.org.secondfiddle.pep.projects.model.manager.ProjectTemplateManager;

@SuppressWarnings("restriction")
public class ProjectTemplateWizard extends NewPluginTemplateWizard implements INewWizard, IExecutableExtension {

	private static final String PROJECT_LOCATION = "projectLocation";

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

					String projectName = projectTemplateSection.getValue(KEY_PLUGIN_ID).toString();
					IProject project = workspaceRoot.getProject(projectName);
					IProjectDescription description = workspace.newProjectDescription(projectName);
					Object location = projectTemplateSection.getValue(PROJECT_LOCATION);
					if (location != null) {
						File locationFile = new File(location.toString());
						description.setLocationURI(locationFile.toURI());
					}

					project.create(description, monitor);
					project.open(monitor);

					for (ITemplateSection templateSection : getTemplateSections()) {
						templateSection.execute(project, null, monitor);
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

	@Override
	public ITemplateSection[] createTemplateSections() {
		this.projectTemplateSection = new ProjectTemplateSection();
		return new ITemplateSection[] { projectTemplateSection };
	}

}
