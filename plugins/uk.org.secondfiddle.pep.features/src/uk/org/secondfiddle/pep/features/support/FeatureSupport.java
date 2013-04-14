package uk.org.secondfiddle.pep.features.support;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.pde.core.IIdentifiable;
import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;

@SuppressWarnings("restriction")
public class FeatureSupport {

	private FeatureSupport() {
	}

	public static IFeatureModel toSingleFeatureModel(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		if (structuredSelection.size() != 1) {
			return null;
		}

		Object firstElement = structuredSelection.getFirstElement();
		return toFeatureModel(firstElement);
	}

	public static IFeatureModel toEditableFeatureModel(Object modelObj) {
		IFeatureModel featureModel = toFeatureModel(modelObj);
		return (featureModel != null && featureModel.isEditable() ? featureModel : null);
	}

	public static Collection<IFeatureModel> toFeatureModels(Object selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IFeatureModel> featureModels = new ArrayList<IFeatureModel>();

		for (Object selectedItem : structuredSelection.toList()) {
			IFeatureModel featureModel = toFeatureModel(selectedItem);
			if (featureModel != null) {
				featureModels.add(featureModel);
			}
		}

		return featureModels;
	}

	public static Collection<IIdentifiable> toFeaturesOrChildren(ISelection selection) {
		if (!(selection instanceof IStructuredSelection)) {
			return null;
		}

		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		Collection<IIdentifiable> featuresOrChildren = new ArrayList<IIdentifiable>();

		for (Object selectedItem : structuredSelection.toList()) {
			if (selectedItem instanceof IFeatureModel) {
				IFeatureModel featureModel = (IFeatureModel) selectedItem;
				featuresOrChildren.add(featureModel.getFeature());
			} else if (selectedItem instanceof IFeatureChild) {
				featuresOrChildren.add((IFeatureChild) selectedItem);
			}
		}

		return featuresOrChildren;
	}

	public static IFeatureModel toFeatureModel(Object obj) {
		if (obj instanceof IFeatureModel) {
			return (IFeatureModel) obj;
		} else if (obj instanceof IIdentifiable) {
			IIdentifiable featureOrChild = (IIdentifiable) obj;
			return getManager().findFeatureModel(featureOrChild.getId());
		} else if (obj instanceof IProject) {
			return getManager().getFeatureModel((IProject) obj);
		} else if (obj instanceof IProductFeature) {
			IProductFeature productFeature = (IProductFeature) obj;
			return getManager().findFeatureModel(productFeature.getId());
		} else {
			return null;
		}
	}

	public static FeatureModelManager getManager() {
		return PDECore.getDefault().getFeatureModelManager();
	}

}
