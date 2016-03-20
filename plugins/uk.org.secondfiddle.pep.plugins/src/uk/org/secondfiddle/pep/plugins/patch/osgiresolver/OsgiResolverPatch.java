package uk.org.secondfiddle.pep.plugins.patch.osgiresolver;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;
import org.eclipse.pde.internal.core.PluginModelManager;

import uk.org.secondfiddle.pep.plugins.PatchActivator;

/**
 * Workaround for slow recalculation of bundle dependencies.
 * <p>
 * May be fixed in Eclipse 4.6, but no solution suggested yet. See
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=355939 for the current status;
 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=173411 for the cause of the
 * problem.
 */
@SuppressWarnings("restriction")
public class OsgiResolverPatch implements Runnable {

	@Override
	public void run() {
		if (PatchActivator.isDebugEnabled()) {
			enableDebugLogging();
		}

		try {
			State state = PluginModelManager.getInstance().getState().getState();
			Resolver resolver = state.getResolver();

			Field resolverField = state.getClass().getSuperclass().getDeclaredField("resolver");
			resolverField.setAccessible(true);
			resolverField.set(state, new PatchedResolver(resolver));
		} catch (NoSuchFieldException e) {
			PatchActivator.logPatchFailure(e);
		} catch (IllegalAccessException e) {
			PatchActivator.logPatchFailure(e);
		}
	}

	private void enableDebugLogging() {
		try {
			Job job = getUpdateClasspathsJob();
			Field projectsField = job.getClass().getDeclaredField("fProjects");
			projectsField.setAccessible(true);
			projectsField.set(job, new LoggingProjectList());
		} catch (NoSuchFieldException e) {
			PatchActivator.logPatchFailure(e);
		} catch (IllegalAccessException e) {
			PatchActivator.logPatchFailure(e);
		}
	}

	private static Job getUpdateClasspathsJob() {
		try {
			PluginModelManager manager = PluginModelManager.getInstance();
			Field jobField = PluginModelManager.class.getDeclaredField("fUpdateJob");
			jobField.setAccessible(true);
			return (Job) jobField.get(manager);
		} catch (NoSuchFieldException e) {
			PatchActivator.logPatchFailure(e);
		} catch (IllegalAccessException e) {
			PatchActivator.logPatchFailure(e);
		}

		throw new RuntimeException("Unable to obtain UpdateClasspathsJob");
	}

}
