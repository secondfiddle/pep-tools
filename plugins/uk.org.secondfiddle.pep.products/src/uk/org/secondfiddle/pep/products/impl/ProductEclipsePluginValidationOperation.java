package uk.org.secondfiddle.pep.products.impl;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.launching.launcher.EclipsePluginValidationOperation;
import org.eclipse.pde.internal.launching.launcher.ProductValidationOperation;

@SuppressWarnings("restriction")
public class ProductEclipsePluginValidationOperation extends EclipsePluginValidationOperation {

	private final Collection<IPluginModelBase> pluginModels;

	public ProductEclipsePluginValidationOperation(ILaunchConfiguration configuration,
			Collection<IPluginModelBase> pluginModels) {
		super(configuration);
		this.pluginModels = pluginModels;
	}

	@Override
	protected IPluginModelBase[] getModels() throws CoreException {
		return (IPluginModelBase[]) pluginModels.toArray(new IPluginModelBase[pluginModels.size()]);
	}

	@Override
	protected IExecutionEnvironment[] getMatchingEnvironments() throws CoreException {
		return new ProductValidationOperation(null) {
			@Override
			public IExecutionEnvironment[] getMatchingEnvironments() throws CoreException {
				return super.getMatchingEnvironments();
			}
		}.getMatchingEnvironments();
	}

}
