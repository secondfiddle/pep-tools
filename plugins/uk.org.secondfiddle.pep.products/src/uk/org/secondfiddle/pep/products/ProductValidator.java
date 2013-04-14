package uk.org.secondfiddle.pep.products;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelManager;
import org.eclipse.pde.internal.core.TargetPlatformHelper;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.launching.launcher.LaunchValidationOperation;
import org.eclipse.pde.internal.launching.launcher.ProductValidationOperation;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.product.ProductValidateAction;

/**
 * Part of this class is adapted from {@link ProductValidateAction}.
 */
@SuppressWarnings("restriction")
public class ProductValidator {

	public static Collection<String> validate(IProduct product) {
		Collection<String> errorMessages = new ArrayList<String>();
		basicValidation(product, errorMessages);

		if (errorMessages.isEmpty()) {
			extendedValidation(product, errorMessages);
		}

		return errorMessages;
	}

	private static void basicValidation(IProduct product, Collection<String> errorMessages) {
		if (product.useFeatures()) {
			FeatureModelManager featureModelManager = PDECore.getDefault().getFeatureModelManager();
			for (IProductFeature feature : product.getFeatures()) {
				String version = feature.getVersion().length() > 0 ? feature.getVersion() : "0.0.0";
				if (featureModelManager.findFeatureModel(feature.getId(), version) == null) {
					errorMessages.add(String.format("Missing feature: %s (%s)", feature.getId(), version));
				}
			}
		} else {
			PluginModelManager pluginModelManager = PDECore.getDefault().getModelManager();
			for (IProductPlugin plugin : product.getPlugins()) {
				if (pluginModelManager.findModel(plugin.getId()) == null) {
					errorMessages.add(String.format("Missing plugin: %s", plugin.getId()));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static void extendedValidation(IProduct product, Collection<String> errorMessages) {
		HashMap<String, IPluginModelBase> pluginModels = new HashMap<String, IPluginModelBase>();

		if (product.useFeatures()) {
			IFeatureModel[] features = getUniqueFeatures(product.getFeatures());
			for (int i = 0; i < features.length; i++) {
				addFeaturePlugins(features[i].getFeature(), pluginModels);
			}
		} else {
			IProductPlugin[] plugins = product.getPlugins();
			for (int i = 0; i < plugins.length; i++) {
				String id = plugins[i].getId();
				if (id == null || pluginModels.containsKey(id)) {
					continue;
				}
				IPluginModelBase model = PluginRegistry.findModel(id);
				if (model != null && TargetPlatformHelper.matchesCurrentEnvironment(model)) {
					pluginModels.put(id, model);
				}
			}
		}

		try {
			IPluginModelBase[] models = (IPluginModelBase[]) pluginModels.values().toArray(
					new IPluginModelBase[pluginModels.size()]);
			LaunchValidationOperation operation = new ProductValidationOperation(models);
			operation.run(new NullProgressMonitor());
			if (operation.hasErrors()) {
				fillErrorMessageList(operation.getInput(), errorMessages);
			}
		} catch (CoreException e) {
			PDEPlugin.logException(e);
			errorMessages.add(e.getMessage());
		}
	}

	private static void fillErrorMessageList(Map<BundleDescription, Object[]> errors, Collection<String> errorMessages) {
		Map<String, Collection<String>> affectedBundlesByCause = new TreeMap<String, Collection<String>>();
		for (Entry<BundleDescription, Object[]> entry : errors.entrySet()) {
			for (Object cause : entry.getValue()) {
				String causeMessage = cause.toString();
				Collection<String> affectedBundles = affectedBundlesByCause.get(causeMessage);
				if (affectedBundles == null) {
					affectedBundles = new TreeSet<String>();
					affectedBundlesByCause.put(causeMessage, affectedBundles);
				}
				affectedBundles.add(entry.getKey().getSymbolicName());
			}
		}

		for (Entry<String, Collection<String>> entry : affectedBundlesByCause.entrySet()) {
			errorMessages.add(entry.getKey() + ". Affected bundles: " + entry.getValue());
		}
	}

	private static void addFeaturePlugins(IFeature feature, Map<String, IPluginModelBase> pluginModels) {
		IFeaturePlugin[] plugins = feature.getPlugins();
		for (int i = 0; i < plugins.length; i++) {
			String id = plugins[i].getId();
			if (id == null || pluginModels.containsKey(id)) {
				continue;
			}
			IPluginModelBase model = PluginRegistry.findModel(id);
			if (model != null && TargetPlatformHelper.matchesCurrentEnvironment(model)) {
				pluginModels.put(id, model);
			}
		}
	}

	private static IFeatureModel[] getUniqueFeatures(IProductFeature[] features) {
		List<IFeatureModel> featureModels = new ArrayList<IFeatureModel>();
		for (int i = 0; i < features.length; i++) {
			String id = features[i].getId();
			String version = features[i].getVersion();
			addFeatureAndChildren(id, version, featureModels);
		}
		return (IFeatureModel[]) featureModels.toArray(new IFeatureModel[featureModels.size()]);
	}

	private static void addFeatureAndChildren(String id, String version, List<IFeatureModel> featureModels) {
		FeatureModelManager manager = PDECore.getDefault().getFeatureModelManager();
		IFeatureModel model = manager.findFeatureModel(id, version);
		if (model == null || featureModels.contains(model)) {
			return;
		}

		featureModels.add(model);

		IFeatureChild[] children = model.getFeature().getIncludedFeatures();
		for (int i = 0; i < children.length; i++) {
			addFeatureAndChildren(children[i].getId(), children[i].getVersion(), featureModels);
		}
	}

}
