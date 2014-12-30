package uk.org.secondfiddle.pep.plugins.patch.osgiresolver;

import java.util.HashSet;

import org.eclipse.osgi.internal.module.ResolverBundle;

import uk.org.secondfiddle.pep.plugins.PatchActivator;

@SuppressWarnings("restriction")
class UnresolvedBundleSet extends HashSet<ResolverBundle> {

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

			return (T[]) new ResolverBundle[0];
		} else {
			return super.toArray(a);
		}
	}

}