package uk.org.secondfiddle.pep.projects;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.ITemplateSection;
import org.eclipse.pde.ui.templates.NewPluginTemplateWizard;
import org.eclipse.pde.ui.templates.OptionTemplateWizardPage;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

public class ProjectTemplateWizard extends NewPluginTemplateWizard implements INewWizard, IExecutableExtension {

	public ProjectTemplateWizard() {
		setDefaultPageImageDescriptor(IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/new_wiz.png"));
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		System.err.println("config: " + config.getName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		System.err.println("selection here!" + selection);
	}

	@Override
	public boolean performFinish() {

		// org.eclipse.ui.wizards.datatransfer.ImportOperation

		try {
			getContainer().run(false, true, new WorkspaceModifyOperation() {

				@Override
				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
						InterruptedException {
					IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
					IProject project = workspaceRoot.getProject("this.is.mine");
					project.create(monitor);
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
		return new ITemplateSection[] { new BaseOptionTemplateSection() {

			private List<WizardPage> pages = new ArrayList<WizardPage>();

			private ArrayList<TemplateOption> options = new ArrayList<TemplateOption>();

			private Map<String, String> defaultReplacements = new HashMap<String, String>();

			@Override
			protected void registerOption(TemplateOption option, Object value, int pageIndex) {
				super.registerOption(option, value, pageIndex);
				options.add(option);
			};

			@Override
			public String getReplacementString(String fileName, String key) {
				String replacement = super.getReplacementString(fileName, key);

				// deliberate identity-check
				if (replacement == key) {
					return defaultReplacements.get(key);
				} else {
					return replacement;
				}
			}

			@Override
			public Object getValue(String name) {
				Object value = super.getValue(name);
				return (value == null ? defaultReplacements.get(name) : value);
			}

			//
			// @Override
			// protected IFolder getSourceFolder(IProgressMonitor monitor) {
			// return project.getFolder("src");
			// }

			@Override
			public URL getTemplateLocation() {
				try {
					return new File("E:\\dev-eclipse-enhancements\\runtime-EclipseIDE.2\\fnarg\\template").toURL();
				} catch (MalformedURLException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public String getLabel() {
				return "label";
			}

			@Override
			public void addPages(Wizard wizard) {
				defaultReplacements.put("testsub", "MyClass");
				defaultReplacements.put("testsrc", "src/fnarg.wizards");
				defaultReplacements.put(KEY_PACKAGE_NAME, "base.pack");
				addOption("test", "Test", "value", 0);
				pages.add(new OptionTemplateWizardPage(this, options, null));
				wizard.addPage(pages.get(0));
			}

			@Override
			public WizardPage getPage(int pageIndex) {
				return pages.get(pageIndex);
			}

			@Override
			public int getPageCount() {
				return 1;
			}

			@Override
			public String getUsedExtensionPoint() {
				throw new UnsupportedOperationException();
			}

			@Override
			public String[] getNewFiles() {
				return new String[0];
			}

			@Override
			public void validateOptions(TemplateOption changed) {
				System.err.println("validateOptions");
			}

			@Override
			protected ResourceBundle getPluginResourceBundle() {
				throw new UnsupportedOperationException();
			}

			@Override
			protected void updateModel(IProgressMonitor monitor) throws CoreException {
				System.err.println("updateModel");
			}

		} };
	}
}
