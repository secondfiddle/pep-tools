package uk.org.secondfiddle.pep.products;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.ui.ide.IDE;

import uk.org.secondfiddle.pep.products.impl.ProductSupport;
import uk.org.secondfiddle.pep.products.ui.ProductEditorWithSource;

@SuppressWarnings("restriction")
public class ProductBuilder extends IncrementalProjectBuilder {

	public static final String ID = ProductBuilder.class.getName();

	private static final String MARKER_TYPE = ProductNatureActivator.PLUGIN_ID + ".problem";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		project.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);

		boolean foundProduct = false;
		for (IResource member : project.members()) {
			if (ProductSupport.isProductFile(member)) {
				validateProduct((IFile) member);
				foundProduct = true;
			}
		}

		if (!foundProduct) {
			IMarker marker = project.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE,
					String.format("Product project '%s' contains no products", project.getName()));
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IDE.EDITOR_ID_ATTR, ProductEditorWithSource.ID);
		}

		return new IProject[0];
	}

	private void validateProduct(IFile productFile) throws CoreException {
		productFile.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);

		IProductModel model = new WorkspaceProductModel(productFile, false);
		model.load();

		Collection<String> errorMessages = ProductValidator.validate(model);
		for (String errorMessage : errorMessages) {
			IMarker marker = productFile.createMarker(MARKER_TYPE);
			marker.setAttribute(IMarker.MESSAGE, errorMessage);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			marker.setAttribute(IDE.EDITOR_ID_ATTR, ProductEditorWithSource.ID);
		}
	}

}
