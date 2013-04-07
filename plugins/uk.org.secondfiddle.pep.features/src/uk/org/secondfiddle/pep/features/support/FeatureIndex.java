package uk.org.secondfiddle.pep.features.support;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.pde.internal.core.FeatureModelManager;
import org.eclipse.pde.internal.core.IFeatureModelDelta;
import org.eclipse.pde.internal.core.IFeatureModelListener;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;

@SuppressWarnings("restriction")
public class FeatureIndex implements IFeatureModelListener {

	private final Map<String, Collection<IFeatureModel>> includingFeatures = new HashMap<String, Collection<IFeatureModel>>();

	private final FeatureModelManager featureModelManager;

	public FeatureIndex(FeatureModelManager featureModelManager) {
		this.featureModelManager = featureModelManager;
		this.featureModelManager.addFeatureModelListener(this);
		reIndex();
	}

	public Collection<IFeatureModel> getIncludingFeatures(String childId) {
		Collection<IFeatureModel> parents = includingFeatures.get(childId);
		if (parents == null) {
			return Collections.emptySet();
		} else {
			return parents;
		}
	}

	public void dispose() {
		this.featureModelManager.removeFeatureModelListener(this);
	}

	private void reIndex() {
		includingFeatures.clear();

		for (IFeatureModel parentModel : featureModelManager.getWorkspaceModels()) {
			for (IFeatureChild child : parentModel.getFeature().getIncludedFeatures()) {
				IFeatureModel childModel = featureModelManager.findFeatureModel(child.getId());
				if (childModel != null && childModel.isEditable()) {
					index(childModel, parentModel);
				}
			}
		}
	}

	private void index(IFeatureModel childModel, IFeatureModel parentModel) {
		String childId = childModel.getFeature().getId();

		Collection<IFeatureModel> parents = includingFeatures.get(childId);
		if (parents == null) {
			parents = new HashSet<IFeatureModel>();
			includingFeatures.put(childId, parents);
		}

		parents.add(parentModel);
	}

	@Override
	public void modelsChanged(IFeatureModelDelta delta) {
		reIndex();
	}

}
