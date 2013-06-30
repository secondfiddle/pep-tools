package uk.org.secondfiddle.pep.features.action;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.internal.SharedImages;
import org.eclipse.ui.internal.Workbench;

import uk.org.secondfiddle.pep.features.viewer.FeatureTreeDropSupport;

@SuppressWarnings("restriction")
public class FeatureAndPluginPasteAction extends Action {

	private final ISelectionProvider selectionProvider;

	private final Clipboard clipboard;

	public FeatureAndPluginPasteAction(ISelectionProvider selectionProvider, Clipboard clipboard) {
		super("Paste", Workbench.getInstance().getSharedImages().getImageDescriptor(SharedImages.IMG_TOOL_PASTE));
		this.selectionProvider = selectionProvider;
		this.clipboard = clipboard;
	}

	@Override
	public void runWithEvent(Event event) {
		Object source = clipboard.getContents(FileTransfer.getInstance());
		Object target = getSingleTarget();
		if (source instanceof String[] && target != null) {
			FeatureTreeDropSupport.performDrop(toSelection(source), target);
		}
	}

	public void revalidate() {
		Object source = clipboard.getContents(FileTransfer.getInstance());
		Object target = getSingleTarget();
		if (source instanceof String[] && target != null) {
			setEnabled(FeatureTreeDropSupport.validateDrop(toSelection(source), target));
		} else {
			setEnabled(false);
		}
	}

	private ISelection toSelection(Object source) {
		String[] filenames = (String[]) source;
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		Collection<IContainer> containers = new HashSet<IContainer>();
		for (String filename : filenames) {
			IPath path = Path.fromOSString(filename);
			IContainer container = workspaceRoot.getContainerForLocation(path);
			if (container != null) {
				containers.add(container);
			}
		}

		return new StructuredSelection(containers.toArray());
	}

	private Object getSingleTarget() {
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.size() == 1) {
				return structuredSelection.getFirstElement();
			}
		}
		return null;
	}

}
