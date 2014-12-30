package uk.org.secondfiddle.pep.products.nature.impl;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.IPluginModelListener;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.PluginModelDelta;
import org.eclipse.pde.internal.core.builders.FeatureRebuilder;
import org.eclipse.pde.internal.core.iproduct.IProductModel;

import uk.org.secondfiddle.pep.products.model.ProductModelManager;

/**
 * Part of this class is adapted from {@link FeatureRebuilder}.
 */
@SuppressWarnings("restriction")
public class ProductRebuilder implements IFeatureModelListener, IPluginModelListener, IResourceChangeListener {

	private boolean touchProducts;

	public void start() {
		PDECore.getDefault().getFeatureModelManager().addFeatureModelListener(this);
		PDECore.getDefault().getModelManager().addPluginModelListener(this);
		JavaCore.addPreProcessingResourceChangedListener(this, IResourceChangeEvent.PRE_BUILD);
	}

	public void stop() {
		PDECore.getDefault().getFeatureModelManager().removeFeatureModelListener(this);
		PDECore.getDefault().getModelManager().removePluginModelListener(this);
		JavaCore.removePreProcessingResourceChangedListener(this);
	}

	public void modelsChanged(IFeatureModelDelta delta) {
		touchProducts = true;
	}

	public void modelsChanged(PluginModelDelta delta) {
		touchProducts = true;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.PRE_BUILD && touchProducts) {
			touchProducts();
		}
	}

	private void touchProducts() {
		ProductModelManager manager = ProductModelManager.getInstance();
		IProductModel[] models = manager.getModels();
		if (models.length > 0) {
			IProgressMonitor monitor = new NullProgressMonitor();
			monitor.beginTask("", models.length);
			for (int i = 0; i < models.length; i++) {
				try {
					IResource resource = models[i].getUnderlyingResource();
					if (resource != null) {
						resource.touch(new SubProgressMonitor(monitor, 1));
					} else {
						monitor.worked(1);
					}
				} catch (CoreException e) {
				}
			}
		}
		touchProducts = false;
	}

}
