package uk.org.secondfiddle.pep.products.impl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ProductSupport {

	private ProductSupport() {
	}

	public static IFile[] getProductFiles(IProject project) {
		IFile product = null;

		for (IResource member : getMembers(project)) {
			if (isProductFile(member)) {
				if (product == null) {
					product = (IFile) member;
				} else {
					throw new UnsupportedOperationException("Multiple products in one project not supported");
				}
			}
		}

		return (product == null) ? new IFile[0] : new IFile[] { product };
	}

	public static boolean isProductFile(IResource resource) {
		return resource instanceof IFile && "product".equals(resource.getFileExtension());
	}

	public static boolean isInterestingProject(IProject project) {
		if (!project.isOpen()) {
			return false;
		}

		for (IResource member : getMembers(project)) {
			if (isProductFile(member)) {
				return true;
			}
		}

		return false;
	}

	private static IResource[] getMembers(IProject project) {
		try {
			return project.members();
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}

}
