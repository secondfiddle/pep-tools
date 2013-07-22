package uk.org.secondfiddle.pep.projects.wizard.option;

import org.eclipse.pde.ui.templates.AbstractChoiceOption;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.ComboChoiceOption;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This is based on {@link ComboChoiceOption} with the {@link Combo} creation
 * extracted to an overridable method.
 */
public class EditableComboChoiceOption extends AbstractChoiceOption {

	private Combo fCombo;

	private Label fLabel;

	public EditableComboChoiceOption(BaseOptionTemplateSection section, String name, String label, String[][] choices) {
		super(section, name, label, choices);
	}

	public void createControl(Composite parent, int span) {
		fLabel = createLabel(parent, 1);
		fLabel.setEnabled(isEnabled());
		fill(fLabel, 1);

		fCombo = new Combo(parent, SWT.DROP_DOWN);
		fill(fCombo, 1);
		for (int i = 0; i < fChoices.length; i++) {
			String[] choice = fChoices[i];
			fCombo.add(choice[1], i);
			fCombo.setEnabled(isEnabled());
		}
		fCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (isBlocked()) {
					return;
				}
				setValue(fCombo.getText(), false);
				getSection().validateOptions(EditableComboChoiceOption.this);
			}
		});

		if (getChoice() != null) {
			selectChoice(getChoice());
		}
	}

	protected void setOptionValue(Object value) {
		if (fCombo != null && value != null) {
			selectChoice(value.toString());
		}
	}

	protected void setOptionEnabled(boolean enabled) {
		if (fLabel != null) {
			fLabel.setEnabled(enabled);
			fCombo.setEnabled(enabled);
		}
	}

	protected void selectOptionChoice(String choice) {
		fCombo.setText(choice);
	}

	protected int getIndexOfChoice(String choice) {
		final int NOT_FOUND = -1;
		if (choice == null) {
			return NOT_FOUND;
		}
		for (int i = 0; i < fChoices.length; i++) {
			String testChoice = fChoices[i][0];
			if (choice.equals(testChoice)) {
				return i;
			}
		}
		return NOT_FOUND;
	}

}
