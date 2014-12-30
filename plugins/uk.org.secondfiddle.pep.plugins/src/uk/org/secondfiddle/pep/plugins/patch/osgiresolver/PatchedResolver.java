package uk.org.secondfiddle.pep.plugins.patch.osgiresolver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;

import org.eclipse.osgi.internal.module.ResolverBundle;
import org.eclipse.osgi.service.resolver.BaseDescription;
import org.eclipse.osgi.service.resolver.BundleDescription;
import org.eclipse.osgi.service.resolver.ExportPackageDescription;
import org.eclipse.osgi.service.resolver.Resolver;
import org.eclipse.osgi.service.resolver.State;

@SuppressWarnings("restriction")
class PatchedResolver implements Resolver {

	private final UnresolvedBundleSet unresolvedBundleSet = new UnresolvedBundleSet();

	private final Resolver realResolver;

	public PatchedResolver(Resolver realResolver) {
		this.realResolver = realResolver;
	}

	@Override
	public void resolve(BundleDescription[] discard, Dictionary<Object, Object>[] platformProperties) {
		preInitialize();
		realResolver.resolve(discard, platformProperties);
	}

	@Override
	public ExportPackageDescription resolveDynamicImport(BundleDescription importingBundle, String requestedPackage) {
		preInitialize();
		return realResolver.resolveDynamicImport(importingBundle, requestedPackage);
	}

	/**
	 * In order to patch ResolverImpl, its unresolvedBundles field-value needs
	 * to be replaced between initialize() and addDevConstraints() being called.
	 * <p>
	 * In order to do this, initialize() is called here (if required) so that it
	 * isn't called by ResolverImpl, then the field-value immediately replaced.
	 */
	@SuppressWarnings("unchecked")
	private void preInitialize() {
		try {
			Field initializedField = realResolver.getClass().getDeclaredField("initialized");
			initializedField.setAccessible(true);

			if (!initializedField.getBoolean(realResolver)) {
				Method initializeMethod = realResolver.getClass().getDeclaredMethod("initialize");
				initializeMethod.setAccessible(true);
				initializeMethod.invoke(realResolver);
			}

			Field unresolvedBundles = realResolver.getClass().getDeclaredField("unresolvedBundles");
			unresolvedBundles.setAccessible(true);
			Collection<ResolverBundle> realUnresolvedBundleSet = (Collection<ResolverBundle>) unresolvedBundles
					.get(realResolver);

			// Reset replacement unresolved bundle-set if necessary
			if (realUnresolvedBundleSet != unresolvedBundleSet) {
				unresolvedBundles.set(realResolver, unresolvedBundleSet);
				unresolvedBundleSet.clear();
				unresolvedBundleSet.addAll(realUnresolvedBundleSet);
			}

			unresolvedBundleSet.claimNoUnresolvedBundlesOnce();
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/*
	 * All other methods just delegate to realResolver
	 */

	@Override
	public void flush() {
		realResolver.flush();
	}

	@Override
	public State getState() {
		return realResolver.getState();
	}

	@Override
	public void setState(State value) {
		realResolver.setState(value);
	}

	@Override
	public void bundleAdded(BundleDescription bundle) {
		realResolver.bundleAdded(bundle);
	}

	@Override
	public void bundleRemoved(BundleDescription bundle, boolean pending) {
		realResolver.bundleRemoved(bundle, pending);
	}

	@Override
	public void bundleUpdated(BundleDescription newDescription, BundleDescription existingDescription, boolean pending) {
		realResolver.bundleUpdated(newDescription, existingDescription, pending);
	}

	@Override
	public void setSelectionPolicy(Comparator<BaseDescription> selectionPolicy) {
		realResolver.setSelectionPolicy(selectionPolicy);
	}

	@Override
	public Comparator<BaseDescription> getSelectionPolicy() {
		return realResolver.getSelectionPolicy();
	}

}