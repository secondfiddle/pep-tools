package uk.org.secondfiddle.pep.plugins.patch.osgiresolver;

import java.lang.reflect.Array;
import java.util.HashSet;

import uk.org.secondfiddle.pep.plugins.PatchActivator;

class UnresolvedBundleSet extends HashSet<Object> {

	private static final long serialVersionUID = 1L;

	private boolean claimEmptyOnce;

	public void claimNoUnresolvedBundlesOnce() {
		claimEmptyOnce = true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		if (claimEmptyOnce) {
			claimEmptyOnce = false;

			if (PatchActivator.isDebugEnabled()) {
				PatchActivator.logInfo("Claiming no unresolved bundles");
			}

			return (T[]) Array.newInstance(a.getClass().getComponentType(), 0);
		} else {
			return super.toArray(a);
		}
	}

}