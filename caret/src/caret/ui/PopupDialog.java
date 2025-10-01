package caret.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import java.util.List;

public class PopupDialog extends Dialog {
    private List<String> items;
    private int selectedIndex = -1; 

    public PopupDialog(Shell parentShell, List<String> items) {
        super(parentShell);
        this.items = items;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        for (int i = 0; i < items.size(); i++) {
            final int index = i; 
            Button radioButton = new Button(container, SWT.RADIO);
            radioButton.setText(items.get(i));
            radioButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            if(i == 0) {
            	radioButton.setSelection(true);
            	selectedIndex = 0;
            }
            radioButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (radioButton.getSelection()) {
                        selectedIndex = index; 
                    }
                }
            });
        }

        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Select a agent");
    }
}

