package uk.org.secondfiddle.pep.products.nature.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class ProductSupport {

	private ProductSupport() {
	}

	public static IFile[] getProductFiles(IProject project) {
		Collection<IFile> products = null;

		for (IResource member : getMembers(project)) {
			if (isProductFile(member)) {
				if (products == null) {
					products = new ArrayList<IFile>();
				}
				products.add((IFile) member);
			}
		}

		return (products == null) ? new IFile[0] : products.toArray(new IFile[products.size()]);
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
