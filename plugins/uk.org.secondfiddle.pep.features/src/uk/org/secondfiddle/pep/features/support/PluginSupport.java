package uk.org.secondfiddle.pep.features.support;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

@SuppressWarnings("restriction")
public class PluginSupport {

	private PluginSupport() {
	}

	public static IPluginModelBase toSinglePluginModel(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() != 1) {
			return null;
		}

		Object firstElement = structuredSelection.getFirstElement();
		return toPluginModel(firstElement);
	}

	public static Collection<IPluginModelBase> toPluginModels(Object selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IPluginModelBase> pluginModels = new ArrayList<IPluginModelBase>();

		for (Object selectedItem : structuredSelection.toList()) {
			IPluginModelBase pluginModel = toPluginModel(selectedItem);
			if (pluginModel != null) {
				pluginModels.add(pluginModel);
			}
		}

		return pluginModels;
	}

	public static Collection<IFeaturePlugin> toFeaturePlugins(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IFeaturePlugin> featurePlugins = new ArrayList<IFeaturePlugin>();

		for (Object selectedItem : structuredSelection.toList()) {
			if (selectedItem instanceof IFeaturePlugin) {
				featurePlugins.add((IFeaturePlugin) selectedItem);
			}
		}

		return featurePlugins;
	}

	public static IPluginModelBase toPluginModel(Object obj) {
		if (obj instanceof IPluginModelBase) {
			return (IPluginModelBase) obj;
		} else if (obj instanceof IFeaturePlugin) {
			IFeaturePlugin featurePlugin = (IFeaturePlugin) obj;
			return getManager().findModel(featurePlugin.getId());
		} else if (obj instanceof IProject) {
			return getManager().findModel((IProject) obj);
		} else if (obj instanceof IJavaProject) {
			return getManager().findModel(((IJavaProject) obj).getProject());
		} else {
			return null;
		}
	}

	public static PluginModelManager getManager() {
		return PDECore.getDefault().getModelManager();
	}

}
