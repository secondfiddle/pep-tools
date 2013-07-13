package uk.org.secondfiddle.pep.plugins;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleContext;

import uk.org.secondfiddle.pep.plugins.osgi.resolver.OsgiResolverPatch;
import uk.org.secondfiddle.pep.plugins.pde.job.PluginDependenciesJobPatch;

public class PatchActivator extends Plugin {

	private static PatchActivator plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		safeRun(new OsgiResolverPatch());
		safeRun(new PluginDependenciesJobPatch());
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	private void safeRun(Runnable patch) {
		try {
			patch.run();
		} catch (Throwable t) {
			logPatchFailure(t);
		}
	}

	public static void logPatchFailure(Throwable t) {
		String pluginId = plugin.getBundle().getSymbolicName();
		Status status = new Status(IStatus.ERROR, pluginId, "Failed to apply patch", t);
		plugin.getLog().log(status);
	}

	public static void logInfo(String message) {
		String pluginId = plugin.getBundle().getSymbolicName();
		Status status = new Status(IStatus.INFO, pluginId, message);
		plugin.getLog().log(status);
	}

	public static boolean isDebugEnabled() {
		return plugin.isDebugging();
	}

	public static class ForceActivation implements IStartup {
		@Override
		public void earlyStartup() {
			/*
			 * This will cause the plugin to be activated, necessary to apply
			 * patches on-startup
			 */
		}
	}

}
