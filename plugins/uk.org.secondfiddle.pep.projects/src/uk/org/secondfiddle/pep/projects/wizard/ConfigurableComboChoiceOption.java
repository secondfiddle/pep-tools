package uk.org.secondfiddle.pep.projects.wizard;

import org.eclipse.pde.ui.templates.AbstractChoiceOption;
import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.ComboChoiceOption;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * This is a copy of {@link ComboChoiceOption} with the {@link Combo} creation
 * extracted to an overridable method.
 */
public abstract class ConfigurableComboChoiceOption extends AbstractChoiceOption {

	private Combo fCombo;

	private Label fLabel;

	public ConfigurableComboChoiceOption(BaseOptionTemplateSection section, String name, String label, String[][] choices) {
		super(section, name, label, choices);
	}

	public void createControl(Composite parent, int span) {
		fLabel = createLabel(parent, 1);
		fLabel.setEnabled(isEnabled());
		fill(fLabel, 1);

		fCombo = createCombo(parent);
		fill(fCombo, 1);
		for (int i = 0; i < fChoices.length; i++) {
			String[] choice = fChoices[i];
			fCombo.add(choice[1], i);
			fCombo.setEnabled(isEnabled());
		}
		fCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (isBlocked())
					return;
				if (fCombo.getSelectionIndex() != -1) {
					String[] choice = fChoices[fCombo.getSelectionIndex()];
					setValue(choice[0], false);
					getSection().validateOptions(ConfigurableComboChoiceOption.this);
				}
			}
		});

		if (getChoice() != null)
			selectChoice(getChoice());
	}

	protected abstract Combo createCombo(Composite parent);

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
		int index = getIndexOfChoice(choice);

		if (index == -1) {
			fCombo.select(0);
			setValue(fChoices[0][0], false);
		} else {
			fCombo.select(index);
		}
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
