package caret.property;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class TestPropertyPage extends FieldEditorPreferencePage implements IWorkbenchPropertyPage {

    private IProject project;
    private static final String PREF_KEY = "customStringKey"; // Preference key for storing the string

    public TestPropertyPage() {
        super(GRID);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);

        // Make the "Apply" and "Cancel" buttons work with the project's preferences
        ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(new ProjectScope(project), "caret");
        setPreferenceStore(preferenceStore);
    }

    @Override
    protected void createFieldEditors() {
        // Create a field editor for a string input
        StringFieldEditor stringEditor = new StringFieldEditor(PREF_KEY, "Enter your custom string:", getFieldEditorParent());
        addField(stringEditor);
    }

    public void setElement(IAdaptable element) {
        project = (IProject) element.getAdapter(IProject.class);
    }

    public IAdaptable getElement() {
        return project;
    }
}
