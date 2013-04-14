package uk.org.secondfiddle.pep.features.viewer;

import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.util.VersionUtil;
import org.eclipse.pde.internal.ui.PDELabelProvider;
import org.eclipse.pde.internal.ui.PDEPluginImages;
import org.eclipse.swt.graphics.Image;

@SuppressWarnings("restriction")
public class FeatureTreeLabelProvider extends PDELabelProvider {

	@Override
	public String getText(Object obj) {
		if (obj instanceof IProductModel) {
			return getObjectText((IProductModel) obj);
		}
		return super.getText(obj);
	}

	@Override
	public Image getImage(Object obj) {
		if (obj instanceof IProductModel) {
			return getObjectImage((IProductModel) obj);
		}
		return super.getImage(obj);
	}

	private String getObjectText(IProductModel productModel) {
		IProduct product = productModel.getProduct();
		String name = preventNull(product.getId());
		if (VersionUtil.isEmptyVersion(product.getVersion())) {
			return name;
		}
		return name + ' ' + formatVersion(product.getVersion());
	}

	private Image getObjectImage(IProductModel productModel) {
		return get(PDEPluginImages.DESC_PRODUCT_DEFINITION);
	}

	private String preventNull(String text) {
		return text != null ? text : "";
	}

}
