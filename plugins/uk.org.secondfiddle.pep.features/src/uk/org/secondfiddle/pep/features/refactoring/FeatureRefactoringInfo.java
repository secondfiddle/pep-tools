package uk.org.secondfiddle.pep.features.refactoring;

import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.ui.refactoring.RefactoringInfo;

@SuppressWarnings("restriction")
public class FeatureRefactoringInfo extends RefactoringInfo {

	private final IFeatureModel featureModel;

	public FeatureRefactoringInfo(IFeatureModel featureModel) {
		this.featureModel = featureModel;
	}

	@Override
	public String getCurrentValue() {
		return featureModel.getFeature().getId();
	}

	public IFeatureModel getModel() {
		return featureModel;
	}

	@Override
	public IPluginModelBase getBase() {
		throw new UnsupportedOperationException("Not a plugin");
	}

}
