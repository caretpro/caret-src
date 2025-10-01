package caret.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Combo;

import java.util.ArrayList;
import java.util.List;

public class CycleValidatorsDialog extends Dialog {

    private String title;
    private String message;

    public CycleValidatorsDialog(Shell parentShell, String title, String message) {
        super(parentShell);
        this.title = title;
        this.message = message;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        
     
        Composite compositePre = new Composite(container, SWT.NONE);
        compositePre.setLayout(new GridLayout(2, false));
        compositePre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Label labelCyclesPre = new Label(compositePre, SWT.NONE);
        labelCyclesPre.setText("Pre-validation cycles:");
        FontData[] fontBoldTitle = labelCyclesPre.getFont().getFontData();
        for (FontData fd : fontBoldTitle) {
            fd.setStyle(SWT.BOLD);
            fd.setHeight(fd.getHeight() + 2);
        }
        labelCyclesPre.setFont(new org.eclipse.swt.graphics.Font(parent.getDisplay(), fontBoldTitle));
        labelCyclesPre.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        Combo comboCyclesPre = new Combo(compositePre, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboCyclesPre.setItems(new String[]{"0", "1", "2", "3"});
        comboCyclesPre.select(1); 
        comboCyclesPre.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        java.util.List<String> listPre = new java.util.ArrayList<>();
        listPre.add("Compilation Validator");
        ListEditorData listEditorDataPre = new ListEditorData(listPre, "  Pre-Validators:", container) {
            @Override
            protected String[] parseString(String str) {
                return str.split(",");
            }

            @Override
            protected String createList(String[] str) {
                return String.join(",", str);
            }

            @Override
            protected String getNewInputObject() {
                org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(
                        getShell(),
                        "Add Validator",
                        "Enter new validator:",
                        "",
                        null
                );
                if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                    return dialog.getValue();
                }
                return null;
            }
        };
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 150;
        gd.horizontalIndent = 10;
        listEditorDataPre.getList().setLayoutData(gd);
        
        Label spacer = new Label(container, SWT.NONE);
        GridData gdSpacer = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSpacer.heightHint = 20;      
        gdSpacer.horizontalSpan = 2;   
        spacer.setLayoutData(gdSpacer);
        
        Composite compositePost = new Composite(container, SWT.NONE);
        compositePost.setLayout(new GridLayout(2, false));
        compositePost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Label labelCyclesPost = new Label(compositePost, SWT.NONE);
        labelCyclesPost.setText("Post-validation cycles:");
        labelCyclesPost.setFont(new org.eclipse.swt.graphics.Font(parent.getDisplay(), fontBoldTitle));
        labelCyclesPost.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        Combo comboCyclesPost = new Combo(compositePost, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboCyclesPost.setItems(new String[]{"0", "1", "2", "3"});
        comboCyclesPost.select(1); 
        comboCyclesPost.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        java.util.List<String> listPost = new java.util.ArrayList<>();
        listPost.add("Test Validator");
        ListEditorData listEditorDataPost = new ListEditorData(listPost, "  Post-Validators:", container) {
            @Override
            protected String[] parseString(String str) {
                return str.split(",");
            }

            @Override
            protected String createList(String[] str) {
                return String.join(",", str);
            }

            @Override
            protected String getNewInputObject() {
                org.eclipse.jface.dialogs.InputDialog dialog = new org.eclipse.jface.dialogs.InputDialog(
                        getShell(),
                        "Add Validator",
                        "Enter new validator:",
                        "",
                        null
                );
                if (dialog.open() == org.eclipse.jface.window.Window.OK) {
                    return dialog.getValue();
                }
                return null;
            }
        };
        listEditorDataPost.getList().setLayoutData(gd);


        return container;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
}
