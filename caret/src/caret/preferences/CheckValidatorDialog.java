package caret.preferences;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import java.util.HashMap;

public class CheckValidatorDialog extends Dialog {
    private HashMap<String, Boolean> validators;
    private String title;
    private String message;

    public CheckValidatorDialog(Shell parentShell, String title, String message, HashMap<String, Boolean> validators) {
        super(parentShell);
        this.title = title;
        this.validators = validators;
        this.message = message;
    }

    public HashMap<String, Boolean> getValidators() {
        return validators;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        Label label = new Label(container, SWT.NONE);
        label.setText(message);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        
        for (String validator : validators.keySet()) {
            Button checkbox = new Button(container, SWT.CHECK);
            checkbox.setText(validator);
            checkbox.setSelection(validators.get(validator));
            checkbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

            checkbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    validators.put(validator, checkbox.getSelection());
                }
            });
        }

        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
}
