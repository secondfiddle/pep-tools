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
import org.eclipse.pde.internal.core.builders.PDEMarkerFactory;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;

import uk.org.secondfiddle.pep.products.impl.ProductSupport;

@SuppressWarnings("restriction")
public class ProductBuilder extends IncrementalProjectBuilder {

	public static final String ID = ProductBuilder.class.getName();

	private Long wholeProjectMarkerId;

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();

		if (wholeProjectMarkerId != null) {
			project.getMarker(wholeProjectMarkerId).delete();
			wholeProjectMarkerId = null;
		}

		boolean foundProduct = false;
		for (IResource member : project.members()) {
			if (ProductSupport.isProductFile(member)) {
				validateProduct((IFile) member);
				foundProduct = true;
			}
		}

		if (!foundProduct) {
			IMarker marker = project.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.MESSAGE,
					String.format("Product project '%s' contains no products", project.getName()));
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			wholeProjectMarkerId = marker.getId();
		}

		return new IProject[0];
	}

	private void validateProduct(IFile productFile) throws CoreException {
		productFile.deleteMarkers(PDEMarkerFactory.MARKER_ID, false, IResource.DEPTH_ZERO);

		IProductModel model = new WorkspaceProductModel(productFile, false);
		model.load();

		Collection<String> errorMessages = ProductValidator.validate(model);
		for (String errorMessage : errorMessages) {
			IMarker marker = productFile.createMarker(PDEMarkerFactory.MARKER_ID);
			marker.setAttribute(IMarker.MESSAGE, errorMessage);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		}
	}

}
