package uk.org.secondfiddle.pep.projects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleContext;

import uk.org.secondfiddle.pep.projects.templates.manager.ProjectTemplateManager;

public class ProjectTemplateActivator extends Plugin {

	public static final String PLUGIN_ID = ProjectTemplateActivator.class.getPackage().getName();

	private static ProjectTemplateActivator plugin;

	private ProjectTemplateManager projectTemplateManager;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		projectTemplateManager = ProjectTemplateManager.getInstance();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);

		if (projectTemplateManager != null) {
			projectTemplateManager.shutdown();
		}
	}

	public static void logWarning(String message) {
		String pluginId = plugin.getBundle().getSymbolicName();
		Status status = new Status(IStatus.WARNING, pluginId, message);
		plugin.getLog().log(status);
	}

	public static void logError(String message) {
		logError(message, null);
	}

	public static void logError(String message, Throwable t) {
		String pluginId = plugin.getBundle().getSymbolicName();
		Status status = new Status(IStatus.ERROR, pluginId, message, t);
		plugin.getLog().log(status);
	}

	public static class ForceActivation implements IStartup {
		@Override
		public void earlyStartup() {
			/*
			 * This will cause the plugin to be activated, necessary to be
			 * notified of project deletion/closure
			 */
		}
	}

}
