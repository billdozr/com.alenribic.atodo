package com.alenribic.atodo.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.alenribic.atodo.ATodoActivator;

public class ATodoPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	private FileFieldEditor finderFieldEditor;
	private FileFieldEditor cruncherFieldEditor;

	public ATodoPreferencePage() {
		super("ATodo", GRID);
		setPreferenceStore(ATodoActivator.getDefault().getPreferenceStore());
		setDescription("Provide the location of the ATodo \"finder\" and \"cruncher\" executables\n" +
				"(Example: /to/path/todo-find, /to/path/todo-crunch )");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		finderFieldEditor = new FileFieldEditor(PreferenceConstants.TODO_FINDER_PATH, 
				"&Finder:", getFieldEditorParent());
		addField(finderFieldEditor);
		cruncherFieldEditor = new FileFieldEditor(PreferenceConstants.TODO_CRUNCHER_PATH, 
				"&Cruncher:", getFieldEditorParent());
		addField(cruncherFieldEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */ 
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void checkState() {
		super.checkState();
		if (!isValid())
			return;
		if (!"".equals(finderFieldEditor.getStringValue().trim()) 
			&& !finderFieldEditor.getStringValue().trim().endsWith("todo-find")) {
			setErrorMessage("Invalid location specified for the \"todo-find\"");
			setValid(false);
		} else if (!"".equals(cruncherFieldEditor.getStringValue().trim()) 
					&& !cruncherFieldEditor.getStringValue().trim().endsWith("todo-crunch")) {
			setErrorMessage("Invalid location specified for the \"todo-crunch\"");
			setValid(false);
		} else {
			setErrorMessage(null);
			setValid(true);
		}
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getProperty().equals(FieldEditor.VALUE)) {
			if (event.getSource() == finderFieldEditor
					|| event.getSource() == cruncherFieldEditor)
				checkState();
		}
	}
}