package uk.org.secondfiddle.pep.products.nature;

import static java.util.Collections.singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
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
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.launching.launcher.LaunchValidationOperation;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.product.ProductValidateAction;

import uk.org.secondfiddle.pep.products.nature.impl.DummyProductLaunchConfiguration;
import uk.org.secondfiddle.pep.products.nature.impl.ProductEclipsePluginValidationOperation;

/**
 * Part of this class is adapted from {@link ProductValidateAction}.
 */
@SuppressWarnings("restriction")
public class ProductValidator {

	public static Collection<String> validate(IProductModel productModel) {
		if (!productModel.isValid()) {
			return singleton("Invalid product file");
		}

		Collection<String> errorMessages = new ArrayList<String>();
		IProduct product = productModel.getProduct();

		basicValidation(product, errorMessages);

		if (errorMessages.isEmpty()) {
			extendedValidation(product, errorMessages);
		}

		return errorMessages;
	}

	private static void basicValidation(IProduct product, Collection<String> errorMessages) {
		IResource productFile = product.getModel().getUnderlyingResource();
		String baseName = FilenameUtils.getBaseName(productFile.getName());
		if (!baseName.equals(product.getId())) {
			errorMessages.add(String.format("Mismatching product ID and filename - expected filename %s.%s",
					product.getId(), productFile.getFileExtension()));
		}

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

	private static void extendedValidation(IProduct product, Collection<String> errorMessages) {
		Map<String, IPluginModelBase> pluginModels = new HashMap<String, IPluginModelBase>();

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
			ILaunchConfiguration configuration = new DummyProductLaunchConfiguration(product);
			LaunchValidationOperation operation = new ProductEclipsePluginValidationOperation(configuration,
					pluginModels.values());
			operation.run(new NullProgressMonitor());
			if (operation.hasErrors()) {
				fillErrorMessageList(operation.getInput(), errorMessages);
			}
		} catch (CoreException e) {
			PDEPlugin.logException(e);
			errorMessages.add(e.getMessage());
		}
	}

	private static void fillErrorMessageList(Map<Object, Object[]> errors, Collection<String> errorMessages) {
		Map<String, Collection<String>> affectedBundlesByCause = new TreeMap<String, Collection<String>>();
		for (Entry<Object, Object[]> entry : errors.entrySet()) {
			if (entry.getValue().length == 0) {
				errorMessages.add(entry.getKey().toString());
				continue;
			}
			for (Object cause : entry.getValue()) {
				String causeMessage = cause.toString();
				Collection<String> affectedBundles = affectedBundlesByCause.get(causeMessage);
				if (affectedBundles == null) {
					affectedBundles = new TreeSet<String>();
					affectedBundlesByCause.put(causeMessage, affectedBundles);
				}
				affectedBundles.add(entry.getKey().toString());
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
