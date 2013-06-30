package uk.org.secondfiddle.pep.features.action;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.pde.core.IModel;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.ResourceTransfer;

import uk.org.secondfiddle.pep.features.support.FeatureSupport;
import uk.org.secondfiddle.pep.features.support.PluginSupport;
import uk.org.secondfiddle.pep.features.support.ProductSupport;

@SuppressWarnings("restriction")
public class FeatureAndPluginCopyAction extends Action {

	private final ISelectionProvider selectionProvider;

	private final Clipboard clipboard;

	public FeatureAndPluginCopyAction(ISelectionProvider selectionProvider, Clipboard clipboard) {
		super("Copy", Workbench.getInstance().getSharedImages().getImageDescriptor(SharedImages.IMG_TOOL_COPY));
		this.selectionProvider = selectionProvider;
		this.clipboard = clipboard;
	}

	@Override
	public void runWithEvent(Event event) {
		ISelection selection = selectionProvider.getSelection();
		Collection<IProductModel> productModels = ProductSupport.toProductModels(selection);
		Collection<IFeatureModel> featureModels = FeatureSupport.toFeatureModels(selection);
		Collection<IPluginModelBase> pluginModels = PluginSupport.toPluginModels(selection);

		Collection<IProject> projects = new HashSet<IProject>();
		addUnderlyingResources(projects, featureModels);
		addUnderlyingResources(projects, pluginModels);

		String textData = getTextData(productModels, featureModels, pluginModels);

		Object[] data = { projects.toArray(new IResource[projects.size()]), textData };
		Transfer[] dataTypes = { ResourceTransfer.getInstance(), TextTransfer.getInstance() };
		clipboard.setContents(data, dataTypes);
	}

	private void addUnderlyingResources(Collection<IProject> projects, Collection<? extends IModel> models) {
		for (IModel model : models) {
			IResource underlyingResource = model.getUnderlyingResource();
			if (underlyingResource != null) {
				projects.add(underlyingResource.getProject());
			}
		}
	}

	private String getTextData(Collection<IProductModel> productModels, Collection<IFeatureModel> featureModels,
			Collection<IPluginModelBase> pluginModels) {
		StringBuilder textData = new StringBuilder();
		for (IProductModel productModel : productModels) {
			textData.append(productModel.getProduct().getId());
			textData.append("\n");
		}
		for (IFeatureModel featureModel : featureModels) {
			textData.append(featureModel.getFeature().getId());
			textData.append("\n");
		}
		for (IPluginModelBase pluginModel : pluginModels) {
			textData.append(pluginModel.getPluginBase().getId());
			textData.append("\n");
		}
		return textData.toString();
	}

}
