package uk.org.secondfiddle.pep.projects.wizard;

import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.StringOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;

public class DirectoryOption extends StringOption {

	public DirectoryOption(BaseOptionTemplateSection section, String name, String label) {
		super(section, name, label);
	}

	@Override
	public void createControl(Composite parent, int span) {
		super.createControl(parent, span);

		Control textField = parent.getChildren()[parent.getChildren().length - 1];

		Composite fieldComposite = new Composite(parent, SWT.NONE);
		fieldComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		fieldComposite.setLayout(gridLayout);

		textField.setParent(fieldComposite);

		Button browseButton = new Button(fieldComposite, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setLayoutData(new GridData());
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(e.display.getActiveShell());
				dialog.setFilterPath(getText());
				dialog.setText("Select Folder");
				dialog.setMessage("Select " + getLabel());

				String chosenFolder = dialog.open();
				if (chosenFolder != null) {
					setText(chosenFolder);
				}
			}
		});
	}

}
