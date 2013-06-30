package uk.org.secondfiddle.pep.features.support;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.internal.core.ICoreConstants;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import uk.org.secondfiddle.pep.products.model.ProductModelManager;
import uk.org.secondfiddle.pep.products.ui.ProductEditorWithSource;

@SuppressWarnings("restriction")
public class ProductSupport {

	private ProductSupport() {
	}

	public static void openProductEditor(IProductModel productModel) {
		IResource resource = productModel.getUnderlyingResource();
		try {
			IEditorInput input = null;
			if (resource != null) {
				input = new FileEditorInput((IFile) resource);
			} else {
				File file = new File(productModel.getInstallLocation(), ICoreConstants.FEATURE_FILENAME_DESCRIPTOR);
				IFileStore store = EFS.getStore(file.toURI());
				input = new FileStoreEditorInput(store);
			}
			IDE.openEditor(PDEPlugin.getActivePage(), input, ProductEditorWithSource.ID, true);
		} catch (PartInitException e) {
			PDEPlugin.logException(e);
		} catch (CoreException e) {
			PDEPlugin.logException(e);
		}
	}

	public static IProductModel toEditableProductModel(Object modelObj) {
		IProductModel productModel = toProductModel(modelObj);
		return (productModel != null && productModel.isEditable() ? productModel : null);
	}

	public static IProductModel toSingleProductModel(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() != 1) {
			return null;
		}

		Object firstElement = structuredSelection.getFirstElement();
		return toProductModel(firstElement);
	}

	public static Collection<IProductModel> toProductModels(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IProductModel> productModels = new ArrayList<IProductModel>();

		for (Object selectedItem : structuredSelection.toList()) {
			IProductModel productModel = toProductModel(selectedItem);
			if (productModel != null) {
				productModels.add(productModel);
			}
		}

		return productModels;
	}

	public static IProductModel toProductModel(Object obj) {
		if (obj instanceof IProduct) {
			IProduct product = (IProduct) obj;
			return getManager().findProductModel(product.getId());
		} else if (obj instanceof IProductModel) {
			return (IProductModel) obj;
		} else {
			return null;
		}
	}

	public static Collection<IProductFeature> toProductFeatures(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IProductFeature> productFeatures = new ArrayList<IProductFeature>();

		for (Object selectedItem : structuredSelection.toList()) {
			if (selectedItem instanceof IProductFeature) {
				productFeatures.add((IProductFeature) selectedItem);
			}
		}

		return productFeatures;
	}

	public static ProductModelManager getManager() {
		return ProductModelManager.getInstance();
	}

}
