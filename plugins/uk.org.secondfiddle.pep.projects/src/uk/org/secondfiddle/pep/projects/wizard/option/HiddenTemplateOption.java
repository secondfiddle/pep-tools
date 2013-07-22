package uk.org.secondfiddle.pep.projects.wizard.option;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
import org.eclipse.pde.ui.templates.TemplateOption;
import org.eclipse.swt.widgets.Composite;

public class HiddenTemplateOption extends TemplateOption {

	private static final Pattern SUBSTITUTE_PATTERN = Pattern.compile("\\$([^\\$]+)\\$");

	public HiddenTemplateOption(BaseOptionTemplateSection section, String name) {
		super(section, name, null);
	}

	@Override
	public String getValue() {
		String value = super.getValue().toString();
		Matcher matcher = SUBSTITUTE_PATTERN.matcher(value);
		while (matcher.find()) {
			String replacement = String.valueOf(getSection().getValue(matcher.group(1)));
			value = value.replace(matcher.group(), replacement);
		}
		return value;
	}

	@Override
	public void setValue(Object value) {
		// only set once
		if (super.getValue() == null) {
			super.setValue(value);
		}
	}

	@Override
	public void createControl(Composite parent, int span) {
	}

}
