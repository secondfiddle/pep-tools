package uk.org.secondfiddle.pep.projects;

import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ProjectTemplateActivator implements BundleActivator {

	public static final String PLUGIN_ID = ProjectTemplateActivator.class.getPackage().getName();

	private ProjectTemplateManager projectTemplateManager;

	@Override
	public void start(BundleContext context) throws Exception {
		projectTemplateManager = ProjectTemplateManager.getInstance();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (projectTemplateManager != null) {
			projectTemplateManager.shutdown();
		}
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
