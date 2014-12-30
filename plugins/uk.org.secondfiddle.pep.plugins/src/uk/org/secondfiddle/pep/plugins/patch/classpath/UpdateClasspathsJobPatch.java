package uk.org.secondfiddle.pep.plugins.patch.classpath;

import java.lang.reflect.Field;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.pde.internal.core.PluginModelManager;

import uk.org.secondfiddle.pep.plugins.PatchActivator;

/**
 * Fix for concurrency issues, null-pointers in {@link PluginModelManager}'s
 * UpdateClasspathsJob.
 * <p>
 * Fix due in Eclipse 4.3 or soon after. See
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=354993 and
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399995 for more details.
 */
@SuppressWarnings("restriction")
public class UpdateClasspathsJobPatch implements Runnable {

	@Override
	public void run() {
		Job job = getUpdateClasspathsJob();
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
	}

	public static Job getUpdateClasspathsJob() {
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
