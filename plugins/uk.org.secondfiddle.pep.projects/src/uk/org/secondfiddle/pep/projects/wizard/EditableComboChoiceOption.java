package uk.org.secondfiddle.pep.projects.wizard;

import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class EditableComboChoiceOption extends ConfigurableComboChoiceOption {

	public EditableComboChoiceOption(BaseOptionTemplateSection section, String name, String label, String[][] choices) {
		super(section, name, label, choices);
	}

	@Override
	protected Combo createCombo(Composite parent) {
		return new Combo(parent, SWT.DROP_DOWN);
	}

}
