package caret.ui;

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

import java.util.ArrayList;
import java.util.List;

public class CheckDialog extends Dialog {
    private List<String> items;
    private List<Boolean> selectedItems;
    private String title;

    public CheckDialog(Shell parentShell, String title, List<String> items) {
        super(parentShell);
        this.title = title;
        this.items = items;
        this.selectedItems = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            selectedItems.add(false); 
        }
    }

    public List<Boolean> getSelectedItems() {
        return selectedItems;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        Label label = new Label(container, SWT.NONE);
        label.setText("Enable or disable validators:");
        label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        
        for (int i = 0; i < items.size(); i++) {
            final int index = i; 
            Button checkbox = new Button(container, SWT.CHECK);
            checkbox.setText(items.get(i));
            checkbox.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            checkbox.setSelection(true);
            checkbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    selectedItems.set(index, checkbox.getSelection());
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
