package uk.org.secondfiddle.pep.products.nature.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.launching.IPDELauncherConstants;

@SuppressWarnings({ "restriction" })
public class DummyProductLaunchConfiguration implements ILaunchConfiguration {

	private final IProduct product;

	public DummyProductLaunchConfiguration(IProduct product) {
		this.product = product;
	}

	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) throws CoreException {
		if (attributeName.equals(IPDELauncherConstants.USE_PRODUCT)) {
			return true;
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAttribute(String attributeName, String defaultValue) throws CoreException {
		if (attributeName.equals(IPDELauncherConstants.PRODUCT)) {
			return product.getProductId();
		}
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getAdapter(Class adapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contentsEqual(ILaunchConfiguration configuration) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunchConfigurationWorkingCopy copy(String name) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean exists() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getAttribute(String attributeName, int defaultValue) throws CoreException {
		throw new UnsupportedOperationException();
	}

	// Raw types used for compatibility with older Eclipse versions
	@SuppressWarnings("rawtypes")
	@Override
	public List<String> getAttribute(String attributeName, List defaultValue) throws CoreException {
		throw new UnsupportedOperationException();
	}

	// Raw types used for compatibility with older Eclipse versions
	@SuppressWarnings("rawtypes")
	@Override
	public Set<String> getAttribute(String attributeName, Set defaultValue) throws CoreException {
		throw new UnsupportedOperationException();
	}

	// Raw types used for compatibility with older Eclipse versions
	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, String> getAttribute(String attributeName, Map defaultValue) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getAttributes() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCategory() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IFile getFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IPath getLocation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IResource[] getMappedResources() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMemento() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getModes() throws CoreException {
		throw new UnsupportedOperationException();
	}

	// Raw types used for compatibility with older Eclipse versions
	@SuppressWarnings("rawtypes")
	@Override
	public ILaunchDelegate getPreferredDelegate(Set modes) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunchConfigurationType getType() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunchConfigurationWorkingCopy getWorkingCopy() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasAttribute(String attributeName) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isLocal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isMigrationCandidate() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isWorkingCopy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILaunch launch(String mode, IProgressMonitor monitor, boolean build, boolean register) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void migrate() throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsMode(String mode) throws CoreException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReadOnly() {
		throw new UnsupportedOperationException();
	}

}
