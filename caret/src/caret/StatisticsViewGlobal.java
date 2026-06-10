package caret;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.data.ChatData;
import caret.data.Interaction;
import caret.data.LogData;
import caret.project.Resource;
import caret.project.java.JavaProject;
import caret.project.java.MethodReplacer;
import caret.project.java.ProjectAnalyzer;
import caret.project.java.ProjectStructure;
import caret.tasks.JavaConcept;
import caret.tasks.Task;
import caret.tasks.TasksManager;
import caret.tool.AnnotationParser;
import caret.tool.Hash;
import caret.tool.Log;
import caret.tool.Tuple;
import caret.tool.Util;
import caret.ui.TasksPluginDialog;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticsViewGlobal {

    private static StatisticsViewGlobal statisticsViewGlobal;
    TreeViewer treeViewer;
    int requestedTasks = 0;
    int acceptedTasks = 0;
    int rejectedTasks = 0;
    int survivalTasks = 0;
    int preservationTasks = 0;
    int undoneTasks = 0;
    Label label;
    static String ELEMENT_PACKAGE = "package";
    static String ELEMENT_USER = "user";
    static String ELEMENT_CLASS = "class";
    static String ELEMENT_METHOD = "method";
    static String ELEMENT_SUBITEM = "subitem";
    static String ELEMENT_TASK = "task";
    
    GridData tableData;
    GridData treeViewerData;
    Table table;
    HashMap<String, Integer> listTaskCounter = new HashMap<String, Integer>();
    private StyledText styledTextTasks;
    private StyledText styledTextTaskRates;
    ChatView chatView = ChatView.getInstance();
    private int selectedIndex;
    
    public static StatisticsViewGlobal getInstance() {
        return statisticsViewGlobal;
    }
    
    @PostConstruct
    public void createPartControl(Composite parent) {
        
        this.statisticsViewGlobal = this;
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false)); // 1 column for the label and tree viewer
        List<Task> tasks = TasksManager.getPreferenceTasks(); 
        String listTasks = ""; 
        int totalClasses = 0;
        int totalMethods = 0;
        int totalPackages = 0;
        try {
            totalClasses = ProjectAnalyzer.getTotalClasses(getCurrentProject());
            totalMethods = ProjectAnalyzer.getTotalMethods(getCurrentProject());
            totalPackages = ProjectAnalyzer.getTotalPackages(getCurrentProject());
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        
        for (Task itemTask : tasks) {
            listTaskCounter.put(itemTask.getName(), 0);
        }
        List<Category> categories = getCategoriesByTasks();
        Color colorComposite = composite.getBackground();
        Color colorDarkComposite = new Color(Display.getCurrent(), 247, 247, 247);
        
        Group group = new Group(composite, SWT.SHADOW_ETCHED_OUT | SWT.BORDER);
        group.setText("Summary: Global");
        group.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY));
        GridLayout groupLayout = new GridLayout(1, false);
        groupLayout.marginWidth = 0;  
        groupLayout.marginHeight = 0;   
        groupLayout.marginTop = 0;     
        groupLayout.marginBottom = 0;   
        groupLayout.horizontalSpacing = 0;
        groupLayout.verticalSpacing = 0;
        group.setLayout(groupLayout);
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        
        Composite innerPanel = new Composite(group, SWT.NONE);
        innerPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        innerPanel.setLayout(new GridLayout(1, false));
        innerPanel.setBackground(colorDarkComposite); 
        
        styledTextTasks = new StyledText(innerPanel, SWT.READ_ONLY | SWT.NONE );
        styledTextTasks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        styledTextTasks.setBackground(colorDarkComposite);
        updateStyledTextTasks();
        
        styledTextTaskRates = new StyledText(innerPanel, SWT.READ_ONLY | SWT.NONE);
        styledTextTaskRates.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        styledTextTaskRates.setBackground(colorDarkComposite);
        updateStyledTextTaskRates();
        
        
        
        Combo combo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.setItems(new String[]{"View by Tasks", "View by Agent", "View by Project", "View by Coverage", "View by User"});
        combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        combo.select(0);
        // Add a listener to detect item selection
        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIndex = combo.getSelectionIndex();
                if (selectedIndex != -1) {
                    String selectedItem = combo.getItem(selectedIndex);
                    if(selectedIndex == 0) {// View by task
                        table.setVisible(false);
                        tableData.exclude = true;
                        treeViewer.getTree().setVisible(true); 
                        treeViewerData.exclude = false;
                        updateStatistics();
                    }
                    if(selectedIndex == 1) {// View by agent
                        table.setVisible(false);
                        tableData.exclude = true;
                        treeViewer.getTree().setVisible(true);
                        treeViewerData.exclude = false;
                        updateStatistics();
                    }
                    if(selectedIndex == 2) {// View by project
                        table.setVisible(false);
                        tableData.exclude = true;
                        treeViewer.getTree().setVisible(true);
                        treeViewerData.exclude = false;
                        treeViewer.setInput(getCategoriesbyProjectStructure());
                    }
                    if(selectedIndex == 3) { // View by coverage
                        treeViewer.getTree().setVisible(false);
                        treeViewerData.exclude = true;
                        table.setVisible(true);
                        tableData.exclude = false;
                        getCovertureByTasks();
                    }
                    if(selectedIndex == 4) { // View by user
                        table.setVisible(false);
                        tableData.exclude = true;
                        treeViewer.getTree().setVisible(true);
                        treeViewerData.exclude = false;
                        updateStatistics();
                    }
                    parent.layout(true, true); 
                    System.out.println("Selected item: " + selectedItem);
                }
            }
        });
        
        Composite radioComposite = new Composite(composite, SWT.NONE);
        radioComposite.setLayout(new GridLayout(3, false)); 
        radioComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        Button acceptedButton = new Button(radioComposite, SWT.RADIO);
        acceptedButton.setText("Accepted");
        acceptedButton.setSelection(true); // Default selected
        Button survivedButton = new Button(radioComposite, SWT.RADIO);
        survivedButton.setText("Survived");
        Button preservedButton = new Button(radioComposite, SWT.RADIO);
        preservedButton.setText("Preserved");
        
        treeViewer = new TreeViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        treeViewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(treeViewerData);
        treeViewer.setContentProvider(new StatisticsContentProvider()); 
        
        Image iconDownload = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/download.png"));
        treeViewer.setLabelProvider(new StyledCellLabelProvider() {
            Image iconList = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/history_list.png"));
            Image iconPackage = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/package_obj.png"));
            Image iconClass = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/file_mode.png"));
            Image iconUser = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/user.png"));
            Image iconMethod = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/public_co.png"));
            Image iconDefault = new Image(Display.getDefault(), getClass().getResourceAsStream("/icons/info.png"));

            @Override
            public void update(ViewerCell cell) {
                Object element = cell.getElement();
                cell.setText(element.toString());
                
                if (element instanceof Category) {
                    Category category = (Category) element;
                    String type = category.getElementType();
                    
                    if (ELEMENT_PACKAGE.equals(type)) cell.setImage(iconPackage);
                    else if (ELEMENT_CLASS.equals(type)) cell.setImage(iconClass);
                    else if (ELEMENT_USER.equals(type)) cell.setImage(iconUser);
                    else if (ELEMENT_METHOD.equals(type)) cell.setImage(iconMethod);
                    else if (ELEMENT_TASK.equals(type)) cell.setImage(iconList);
                    else cell.setImage(iconDefault);
                }
                super.update(cell);
                cell.getControl().redraw(cell.getBounds().x, cell.getBounds().y, 
                        cell.getBounds().width, cell.getBounds().height, false);
            }

            @Override
            protected void paint(Event event, Object element) {
                super.paint(event, element); 

                if (element instanceof Category) {
                    Category category = (Category) element;
                    if (ELEMENT_TASK.equals(category.getElementType())&& category.getData("installed").equals("NOT") ) {
                        TreeItem item = (TreeItem) event.item;
                        Rectangle textBounds = item.getTextBounds(0); 
                        int textWidth = event.gc.textExtent(category.getName()).x;
                        int x = textBounds.x + textWidth + 12;
                        int y = event.y + (event.height - 16) / 2;

                        Rectangle oldClipping = event.gc.getClipping();
                        event.gc.setClipping((Rectangle) null); 

                        event.gc.drawImage(iconDownload, x, y);

                        event.gc.setClipping(oldClipping);
                        
                        item.setData("iconX", x);
                    }
                }
            }

            @Override 
            public void dispose() {
                Image[] icons = {iconList, iconPackage, iconClass, iconUser, iconMethod, iconDefault};
                for (Image img : icons) {
                    if (img != null && !img.isDisposed()) img.dispose();
                }
            }
        });

        treeViewer.getTree().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                Point pt = new Point(event.x, event.y);
                TreeItem item = treeViewer.getTree().getItem(pt);

                if (item == null) {
                    for (TreeItem i : treeViewer.getTree().getItems()) {
                        Rectangle bounds = i.getBounds();
                        if (event.y >= bounds.y && event.y <= (bounds.y + bounds.height)) {
                            item = i;
                            break;
                        }
                    }
                }

                if (item == null) {
                    return;
                }

                Object data = item.getData();

                if (data instanceof Category) {
                    Category category = (Category) data;
                    String type = category.getElementType();
                    String pluginIdAttr = category.getData("pluginId");
                    String installedAttr = category.getData("installed");

                    if (ELEMENT_TASK.equals(type) && installedAttr.equals("NOT") ) {
                        GC gc = new GC(treeViewer.getTree());
                        int textWidth = gc.textExtent(category.getName()).x;
                        gc.dispose();

                        Rectangle textBounds = item.getTextBounds(0);
                        
                        int iconStartX = textBounds.x + textWidth + 12;
                        int iconEndX = iconStartX + 22; 

                        if (event.x >= iconStartX && event.x <= iconEndX) {
                            Display.getDefault().asyncExec(() -> {
                            	System.out.println("Click: "+pluginIdAttr);
                                TasksPluginDialog responseDialog = new TasksPluginDialog(Display.getCurrent().getActiveShell());
                                responseDialog.setPluginId(pluginIdAttr);
                                responseDialog.open();
                            });
                        }
                    }
                }
            }
        });
        treeViewer.setInput(categories);
        treeViewer.getTree().setVisible(true); 
        treeViewerData.exclude = false;
        treeViewer.addSelectionChangedListener(event -> {
            IStructuredSelection selection = (IStructuredSelection) event.getSelection();
            Object selectedElement = selection.getFirstElement();
            
            if (selectedElement instanceof Category) {
                Category category = (Category) selectedElement;
                
                if (ELEMENT_CLASS.equals(category.getElementType())) {
                    System.out.println("Class selected: " + category.getName());
                    ICompilationUnit cu = JavaProject.getClass(getCurrentProject(), category.getName());
                    String classpath = ""+ cu.getPath();
                    String projectName = getCurrentProject().getName();
                    String projectPath = "/" + projectName ;
                    String classpathrelative = classpath.substring(projectPath.length());
                    openClassInEditor(getCurrentProject().getName(), classpathrelative);
                }else {
                    System.out.println("NO Class selected: " + category.getName());
                }
            }
        });
        treeViewer.getTree().addListener(SWT.EraseItem, event -> {
        });
        table = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(tableData);
        table.setVisible(false); 
        tableData.exclude = true;

        String[] columnNames = { "Task", "Percentage", "Coverage" };
        for (String columnName : columnNames) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(columnName);
            column.setWidth(150); 
        }
        
        /*for (Task itemTask : tasks) {
            if(listTaskCounter.get(itemTask.getName()) > 0) {
                TableItem item = new TableItem(table, SWT.NONE);
                int coverage = (totalMethods == 0) ? 0 : Math.round((listTaskCounter.get(itemTask.getName()) * 100f) / totalMethods);
                String percent = (coverage == 0)? "<1%": coverage + "%";
                item.setText(new String[] { itemTask.getName(), percent });
                
                TableEditor editor = new TableEditor(table);
                ProgressBar progressBar = new ProgressBar(table, SWT.NONE);
                progressBar.setMaximum(100);
                progressBar.setSelection(coverage);
                editor.grabHorizontal = true;
                editor.setEditor(progressBar, item, 2);
            }
        }*/
        Map<String, Integer> taskCounts = getCovertureByTasks();

        for (Map.Entry<String, Integer> entry : taskCounts.entrySet()) {
            String taskName = entry.getKey();
            int taskTotal = entry.getValue();

            if (taskTotal > 0) {
                TableItem item = new TableItem(table, SWT.NONE);
                int coverage = (totalMethods == 0) ? 0 : Math.round((taskTotal * 100f) / totalMethods);
                String percent = (coverage == 0) ? "<1%" : coverage + "%";
                item.setText(new String[] { taskName, percent });
                
                TableEditor editor = new TableEditor(table);
                ProgressBar progressBar = new ProgressBar(table, SWT.NONE);
                progressBar.setMaximum(100);
                progressBar.setSelection(coverage);
                editor.grabHorizontal = true;
                editor.setEditor(progressBar, item, 2);
            }
        }
        
        table.getColumn(1).pack();
        table.getColumn(2).pack();
        System.out.println("###CARET STATISTICS treeViewer created:");
        table.addListener(SWT.MouseDown, event -> {
            TableItem item = table.getItem(new Point(event.x, event.y));
            if (item != null) {
                for (int i = 0; i < table.getColumnCount(); i++) {
                    if (item.getBounds(i).contains(event.x, event.y) && i == 1) { // Column 1: Percentage
                        ToolTip tooltip = new ToolTip(table.getShell(), SWT.BALLOON);
                        tooltip.setText("Info");
                        tooltip.setMessage("this is calculated by %");
                        tooltip.setAutoHide(true);
                        tooltip.setVisible(true);
                    }
                }
            }
        });
    }

    private List<Category> getCategoriesByTasks() {
        requestedTasks = 0;
        acceptedTasks = 0;
        rejectedTasks = 0;
        List<Category> categories = new ArrayList<>();
        String name = getCurrentProject().getName();
        List<Interaction> interactions = LogData.getInteractionsJSON(getCurrentProject());
        for (Interaction interaction : interactions) {
            if(interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
                
                requestedTasks++;
                if(interaction.getResult().isUsed()){
                    acceptedTasks++;
                }else {
                    rejectedTasks++;
                }
                
                Integer count = listTaskCounter.get(interaction.getTaskName());
                if(count!=null) {
                    count++;
                    listTaskCounter.put(interaction.getTaskName(), count);
                }
                
                Category existingCategory = null;
                for (Category category : categories) {
                    if(category.getName() != null){
                        if (category.getName().equals(interaction.getTaskName())) {
                            existingCategory = category;
                            break;
                        }
                    }else {
                        //System.out.println("###@CategoryByTask: name-> (NULL):");
                    }
                }
                String targetParameter = interaction.getTargetParameterType();
                Category categoryClass = new Category (Util.getClassName(interaction.getContext().getResource().getFileName()));
                categoryClass.setElementType(ELEMENT_CLASS);
                if (existingCategory != null) {
                    existingCategory.setElementType(ELEMENT_TASK);
                    Category categoryMethod = null;
                    if(targetParameter!=null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName());
                            categoryMethod.setElementType(ELEMENT_METHOD);
                        }else {
                            categoryMethod = new Category ("*");
                        }
                    }else {
                        categoryMethod = new Category ("**");
                        
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("User: "+interaction.getGitUser()+"<"+interaction.getGitEmail()+">");
                    categoryMethod.addSubItem("Agent: " +interaction.getResult().getAgent().getTechnology());
                    categoryClass.addSubItem(categoryMethod);
                    existingCategory.addSubItem(categoryClass);
                } else {
                    Category newCategory = new Category(interaction.getTaskName());
                    newCategory.setElementType(ELEMENT_TASK);
                    String pluginId = TasksManager.findPluginId(interaction.getTaskCode());
                    if(pluginId == null){
                    	newCategory.setData("installed", "NOT");
                    }else {
                    	newCategory.setData("installed", "YES");
                    }
                    newCategory.setData("pluginId", interaction.getTaskPluginId());
                    Category categoryMethod = null;
                    if(interaction.getTargetParameterType() != null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName()+"()");
                            categoryMethod.setElementType(ELEMENT_METHOD);
                            
                        }else {
                            categoryMethod = new Category ("***");
                        }
                    }else {
                        categoryMethod = new Category ("****");
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("User: "+interaction.getGitUser()+"<"+interaction.getGitEmail()+">");
                    categoryMethod.addSubItem("Agent: " +interaction.getResult().getAgent().getTechnology());
                    categoryClass.addSubItem(categoryMethod);
                    newCategory.addSubItem(categoryClass);
                    categories.add(newCategory);
                }
            }
        }
        return categories;
    }
    
    private Map<String, Integer> getCovertureByTasks() {
        requestedTasks = 0;
        acceptedTasks = 0;
        rejectedTasks = 0;
        
        Map<String, Integer> taskCounts = new HashMap<>();
        List<Interaction> interactions = LogData.getInteractionsJSON(getCurrentProject());
        
        for (Interaction interaction : interactions) {
            if (interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
                
                requestedTasks++;
                if (interaction.getResult().isUsed()) {
                    acceptedTasks++;
                } else {
                    rejectedTasks++;
                }
                
                Integer count = listTaskCounter.get(interaction.getTaskName());
                if (count != null) {
                    count++;
                    listTaskCounter.put(interaction.getTaskName(), count);
                }
                
                String taskName = interaction.getTaskName();
                taskCounts.put(taskName, taskCounts.getOrDefault(taskName, 0) + 1);
            }
        }
        taskCounts.forEach((name, total) -> System.out.println("Task: " + name + " | Total: " + total));
        return taskCounts;
    }

    private List<Category> getCategoriesByAgents() {
        requestedTasks = 0;
        acceptedTasks = 0;
        rejectedTasks = 0;
        List<Category> agentCategories = new ArrayList<>();
        String name = getCurrentProject().getName();
        System.out.println("###@CARET STATISTICS NEW Project:"+name);
        List<Interaction> interactions = LogData.getInteractionsJSON(getCurrentProject());
        for (Interaction interaction : interactions) {
            if(interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
                requestedTasks++;
                if(interaction.getResult().isUsed()){
                    acceptedTasks++;
                }else {
                    rejectedTasks++;
                }
                
                Integer count = listTaskCounter.get(interaction.getTaskName());
                if(count!=null) {
                    count++;
                    listTaskCounter.put(interaction.getTaskName(), count);
                }
                
                Category existingAgentCategory = null;
                for (Category agentCategory : agentCategories) {
                    if(agentCategory.getName() != null){
                        if (agentCategory.getName().equals(interaction.getResult().getAgent().getTechnology())) {
                            existingAgentCategory = agentCategory;
                            break;
                        }
                    }else {
                        //System.out.println("###@CategoryByTask: name-> (NULL):");
                    }
                }
                String targetParameter = interaction.getTargetParameterType();
                Category categoryClass = new Category (Util.getClassName(interaction.getContext().getResource().getFileName()));
                categoryClass.setElementType(ELEMENT_CLASS);
                if (existingAgentCategory != null) {
                    existingAgentCategory.setElementType(ELEMENT_TASK);
                    Category categoryMethod = null;
                    if(targetParameter!=null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName());
                            categoryMethod.setElementType(ELEMENT_METHOD);
                        }else {
                            categoryMethod = new Category ("*");
                        }
                    }else {
                        categoryMethod = new Category ("**");
                        
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("User: "+interaction.getGitUser()+"<"+interaction.getGitEmail()+">");
                    categoryMethod.addSubItem("Task: " +interaction.getTaskName());
                    categoryClass.addSubItem(categoryMethod);
                    existingAgentCategory.addSubItem(categoryClass);
                    /*if (currentAgentCategory.getName().equals(interaction.getResult().getAgent().getTechnology())) {
                        currentAgentCategory.addSubItem(existingTaskCategory);
                    }*/
                } else {
                    Category newAgentCategory = new Category(interaction.getResult().getAgent().getTechnology());
                    newAgentCategory.setElementType(ELEMENT_TASK);
                    Category categoryMethod = null;
                    if(interaction.getTargetParameterType() != null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName()+"()");
                            categoryMethod.setElementType(ELEMENT_METHOD);
                            
                        }else {
                            categoryMethod = new Category ("***");
                        }
                    }else {
                        categoryMethod = new Category ("****");
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("User: "+interaction.getGitUser()+"<"+interaction.getGitEmail()+">");
                    categoryMethod.addSubItem("Task: " +interaction.getTaskName());
                    categoryClass.addSubItem(categoryMethod);
                    newAgentCategory.addSubItem(categoryClass);
                    /*if (currentAgentCategory.getName().equals(interaction.getResult().getAgent().getTechnology())) {
                        currentAgentCategory.addSubItem(newTaskCategory);
                    }*/
                    agentCategories.add(newAgentCategory);
                }
            }
        }
        return agentCategories;
    }
    
    private List<Category> getCategoriesByUser() {
        requestedTasks = 0;
        acceptedTasks = 0;
        rejectedTasks = 0;
        List<Category> taskCategories = new ArrayList<>();
        List<Category> userCategories = new ArrayList<>();
        String name = getCurrentProject().getName();
        List<Interaction> interactions = LogData.getInteractionsJSON(getCurrentProject());
        for (Interaction interaction : interactions) {
            if(interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
                
                requestedTasks++;
                if(interaction.getResult().isUsed()){
                    acceptedTasks++;
                }else {
                    rejectedTasks++;
                }
                
                Integer count = listTaskCounter.get(interaction.getTaskName());
                if(count!=null) {
                    count++;
                    listTaskCounter.put(interaction.getTaskName(), count);
                }
                
                Category existingUserCategory = null;
                for (Category userCategory : userCategories) {
                    if(userCategory.getName() != null){
                        if (userCategory.getName().equals(interaction.getGitUser())) {
                            existingUserCategory = userCategory;
                            break;
                        }
                    }else {
                        //System.out.println("###@CategoryByUser: name-> (NULL):");
                    }
                }
                
                Category existingTaskCategory = null;
                for (Category categoryTask : taskCategories) {
                    if(categoryTask.getName() != null){
                        if (categoryTask.getName().equals(interaction.getTaskName())) {
                            existingTaskCategory = categoryTask;
                            break;
                        }
                    }else {
                        //System.out.println("###@CategoryByUser: name-> (NULL):");
                    }
                }
                
                String targetParameter = interaction.getTargetParameterType();
                Category categoryClass = new Category (Util.getClassName(interaction.getContext().getResource().getFileName()));
                categoryClass.setElementType(ELEMENT_CLASS);
                
                Category _categoryTask;
                if (existingTaskCategory != null) {
                    existingTaskCategory.setElementType(ELEMENT_TASK);
                    Category categoryMethod = null;
                    if(targetParameter!=null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName());
                            categoryMethod.setElementType(ELEMENT_METHOD);
                        }else {
                            categoryMethod = new Category ("*");
                        }
                    }else {
                        categoryMethod = new Category ("**");
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("Agent: " +interaction.getResult().getAgent().getTechnology());
                    categoryClass.addSubItem(categoryMethod);
                    existingTaskCategory.addSubItem(categoryClass);
                    _categoryTask = existingTaskCategory;
                } else {
                    Category newTaskCategory = new Category(interaction.getTaskName());
                    newTaskCategory.setElementType(ELEMENT_TASK);
                    String pluginId = TasksManager.findPluginId(interaction.getTaskCode());
                    if(pluginId == null){
                    	newTaskCategory.setData("installed", "NOT");
                    }else {
                    	newTaskCategory.setData("installed", "YES");
                    }
                    newTaskCategory.setData("pluginId", interaction.getTaskPluginId());
                    System.out.println("###pluginId: "+pluginId);
                    Category categoryMethod = null;
                    if(interaction.getTargetParameterType() != null) {
                        if(interaction.getTargetParameterType().equals(JavaConcept.METHOD.name())) {
                            categoryMethod = new Category (interaction.getTargetParameterName()+"()");
                            categoryMethod.setElementType(ELEMENT_METHOD);
                        }else {
                            categoryMethod = new Category ("***");
                        }
                    }else {
                        categoryMethod = new Category ("****");
                    }
                    categoryMethod.addSubItem(Util.getDateFormat("yyyy-MM-dd HH:mm:ss", interaction.getTimestamp()));
                    categoryMethod.addSubItem("Agent: " +interaction.getResult().getAgent().getTechnology());
                    categoryClass.addSubItem(categoryMethod);
                    newTaskCategory.addSubItem(categoryClass);
                    _categoryTask = newTaskCategory;
                    taskCategories.add(newTaskCategory);
                }
                
                if (existingUserCategory != null) {
                    existingUserCategory.setElementType(ELEMENT_USER);
                    if (existingTaskCategory == null) {
                        existingUserCategory.addSubItem(_categoryTask);
                    }
                }else {
                    Category newUserCategory = new Category(interaction.getGitUser());
                    newUserCategory.setElementType(ELEMENT_USER);
                    newUserCategory.addSubItem(_categoryTask);
                    userCategories.add(newUserCategory);
                        //;
                }
            }
        }
        return userCategories;
    }

    
    public void updateStatistics() {
        System.out.println("###CARET STATISTICS UPDATE");
        if(selectedIndex == 0) {
            treeViewer.setInput(getCategoriesByTasks());
        }
        if(selectedIndex == 1) {
            treeViewer.setInput(getCategoriesByAgents());
        }
        if(selectedIndex == 4) {
            treeViewer.setInput(getCategoriesByUser());
        }
        
        /*label.setText("Tasks -> Requested ("+requestedTasks+")\n"
                + "Accepted("+acceptedTasks+")\n"
                + "Rejected("+rejectedTasks+")");*/
        updateStyledTextTasks();
        updateStyledTextTaskRates();
    }
    
    public void updateStatistics(List<Category> newCategories) {
        if (treeViewer != null && !treeViewer.getTree().isDisposed()) {
            treeViewer.setInput(newCategories);
        }
    }
    
    public List<Category> getCategoriesbyProjectStructure() {
        Pattern annotationPattern = Pattern.compile("@Generated\\((.*?)\\)");
        List<Category> categories = new ArrayList<>();
        try {
            IProject project = getCurrentProject();
            if (project != null && project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();

                for (IPackageFragment myPackage : packages) {
                    if (myPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        String packageName = myPackage.getElementName();
                        if(packageName.equals("")) {
                            packageName ="[Default]";
                        }
                        Category packageCategory = new Category(packageName);
                        packageCategory.setElementType(ELEMENT_PACKAGE);
                        
                        boolean classAnnotationGenerated = false;
                        for (ICompilationUnit unit : myPackage.getCompilationUnits()) {
                            Category classCategory = new Category(unit.getElementName().replace(".java", ""));
                            classCategory.setElementType(ELEMENT_CLASS);
                            boolean annotationGenerated;
                            for (IType type : unit.getTypes()) {
                                annotationGenerated = false;
                                for (IMethod method : type.getMethods()) {
                                    Category methodCategory = new Category(method.getElementName()+ (method.getNumberOfParameters()>0 ? "(...)":"()"));
                                    methodCategory.setElementType(ELEMENT_METHOD);
                                    String source = unit.getSource();
                                    String methodSignature = method.getSource();
                                    int start = method.getSourceRange().getOffset();
                                    int length = method.getSourceRange().getLength();
                                    String methodSource = source.substring(start, start + length);
                                    Matcher matcher = annotationPattern.matcher(methodSource);
                                    if (matcher.find()) {
                                        String annotationContent = matcher.group(1); 
                                        
                                        Map<String, String> parsedData = AnnotationParser.parseAnnotation(annotationContent);
                                        Category taskCategory = new Category("Task: " + parsedData.get("task"));
                                        String pluginId = TasksManager.findPluginId(parsedData.get("task"));
                                        if(pluginId == null){
                                        	taskCategory.setData("installed", "NOT");
                                        }else {
                                        	taskCategory.setData("installed", "YES");
                                        }
                                        taskCategory.setData("pluginId", pluginId);
                                        taskCategory.setElementType(ELEMENT_TASK);
                                        taskCategory.addSubItem("Agent: " + parsedData.get("agent"));
                                        taskCategory.addSubItem("Date: " + parsedData.get("timestamp"));
                                        
                                        methodCategory.addSubItem(taskCategory);
                                        annotationGenerated = true;
                                        classCategory.addSubItem(methodCategory);
                                    }
                                }
                                if(annotationGenerated) {
                                    packageCategory.addSubItem(classCategory);
                                    classAnnotationGenerated = true;
                                }
                            }
                           
                        }
                        if(classAnnotationGenerated) {
                            categories.add(packageCategory);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return categories;
    }


    private static class StatisticsContentProvider implements ITreeContentProvider {

        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof List) {
                return ((List<?>) inputElement).toArray();
            }
            return new Object[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof Category) {
                return ((Category) parentElement).getSubItems().toArray();
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof Category) {
                return !((Category) element).getSubItems().isEmpty();
            }
            return false;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private static class Category {
        private String name;
        private List<Object> subItems;
        private String elementType;
        private Category parent;
        private Map<String, String> data;

        public String getElementType() {
            return elementType;
        }

        public void setElementType(String elementType) {
            this.elementType = elementType;
        }

        public Category(String name) {
            this.name = name;
            this.subItems = new ArrayList<>();
            this.data = new HashMap<>();
        }

        public void addSubItem(Object subItem) {
            subItems.add(subItem);
        }
        
        public void addSubItem(Category subItem) {
            subItem.setParent(this);
            subItems.add(subItem);
        }

        public String getName() {
            return name;
        }

        public List<Object> getSubItems() {
            return subItems;
        }
       
        public void setParent(Category parent) {
            this.parent = parent;
        }

        public Category getParent() {
            return parent;
        }

        public void setData(String key, String value) {
            data.put(key, value);
        }

        public String getData(String key) {
            return data.get(key);
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /*public List<Interaction> getIteractionsJSON(IProject project) {
        String projectPath = project.getLocation().toString();
        System.out.println("##getIteractionsJSON: " + projectPath+"/.log");
        List<Interaction> totalInteractions = new ArrayList<Interaction> ();
        List <String> listInteractionsJSON = Util.readFilesFromDirectory(projectPath+"/.log", ".json");
        Gson gson = new Gson();
        for (String interactionsJSON : listInteractionsJSON) {
            List<Interaction> interactions = gson.fromJson(interactionsJSON, new TypeToken<List<Interaction>>() {}.getType());
            totalInteractions.addAll(interactions);
        }
        return totalInteractions;
    }*/
    
    public IProject getCurrentProject() {
        IProject project = null;
        //IEditorPart  editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        IResource  resource= Resource.getSelectedResource();
        if(resource != null){
            //IResource  resource= (IResource)editorPart.getEditorInput().getAdapter(IResource.class);
            project = resource.getProject();
            if(project != null) {
                return project;
            }
        }
        if(project == null) {
            if (project == null) {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window != null && window.getActivePage() != null) {
                    IEditorPart editorPart = window.getActivePage().getActiveEditor();
                    if (editorPart != null) {
                        IResource editorResource = editorPart.getEditorInput().getAdapter(IResource.class);
                        if (editorResource != null) {
                            project = editorResource.getProject();
                        }
                    }
                }
            }
        }
        
        if(project == null){
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            IProject lastProject = null;
            long modificationStamp = 0;
            for (IProject proj : projects) {
                if(lastProject == null) {
                    lastProject = proj;
                }
                if(proj.getModificationStamp()> modificationStamp) {
                    lastProject = proj;
                    modificationStamp = proj.getModificationStamp();
                }
            }
            if(lastProject != null) {
                return lastProject;
            }
        }
        return project;
    }
    
    private void updateStyledTextTasks() {
        
        if (styledTextTasks == null || styledTextTasks.isDisposed()) {
            return;
        }
        String requested = "Requested (" + requestedTasks + ")";
        String accepted = "Accepted (" + acceptedTasks + ")";
        String rejected = "Rejected (" + rejectedTasks + ")";
        String text = "Tasks: "+ requested +" | "
                + accepted +" | "
                + rejected;

        styledTextTasks.setText(text);

        // Define colors
        Display display = Display.getCurrent();
        Color gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        Color blue = display.getSystemColor(SWT.COLOR_BLUE);
        Color green = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        Color red = display.getSystemColor(SWT.COLOR_RED);

        // Apply styles
        StyleRange tasksStyle = new StyleRange(0, 6, gray, null, SWT.BOLD);
        StyleRange requestedStyle = new StyleRange(text.indexOf("Requested"), requested.length(), blue, null, SWT.NORMAL);
        StyleRange acceptedStyle = new StyleRange(text.indexOf("Accepted"), accepted.length(), green, null, SWT.NORMAL);
        StyleRange rejectedStyle = new StyleRange(text.indexOf("Rejected"), rejected.length(), red, null, SWT.NORMAL);

        styledTextTasks.setStyleRanges(new StyleRange[]{tasksStyle, requestedStyle, acceptedStyle, rejectedStyle});
    }
    
    private void updateStyledTextTaskRates() {
        countGeneratedAnnotations();
        Log.d("***TASK RATES: "+requestedTasks+"-"+acceptedTasks+"-"+survivalTasks+"-"+preservationTasks);
	    int acceptanceRate = (int) Math.round(((double) acceptedTasks / requestedTasks)*100);
	    int survivalRate = (int) Math.round(((double) survivalTasks / requestedTasks)*100);
	    int preservationRate = (int) Math.round(((double) preservationTasks / requestedTasks)*100);
        String acceptance = "Acceptance (" + Util.clampToPercentageRange(acceptanceRate) + "%)";
        String survival = "Survival (" + Util.clampToPercentageRange(survivalRate) + "%)";
        String preservation = "Preservation (" + Util.clampToPercentageRange(preservationRate) + "%)";
        
        if (styledTextTaskRates == null || styledTextTaskRates.isDisposed()) {
            return;
        }

        String text = "Rates: " + acceptance + " | "
                + survival + " | "
                + preservation;

        styledTextTaskRates.setText(text);

        // Define colors
        Display display = Display.getCurrent();
        Color gray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
        Color blue = display.getSystemColor(SWT.COLOR_DARK_BLUE);
        Color green = display.getSystemColor(SWT.COLOR_DARK_GREEN);
        Color red = display.getSystemColor(SWT.COLOR_RED);

        // Apply styles
        StyleRange tasksStyle = new StyleRange(0, 6, gray, null, SWT.BOLD);
        StyleRange requestedStyle = new StyleRange(text.indexOf("Acceptance"), acceptance.length(), blue, null, SWT.NORMAL);
        StyleRange acceptedStyle = new StyleRange(text.indexOf("Survival"), survival.length(), blue, null, SWT.NORMAL);
        StyleRange rejectedStyle = new StyleRange(text.indexOf("Preservation"), preservation.length(), blue, null, SWT.NORMAL);

        styledTextTaskRates.setStyleRanges(new StyleRange[]{tasksStyle, requestedStyle, acceptedStyle, rejectedStyle});
    }

    public void openClassInEditor(String projectName, String classFilePath) {
        try {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project != null && project.isAccessible()) {
                IFile file = project.getFile(new Path(classFilePath));
                if (file.exists()) {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    IDE.openEditor(page, file);
                    System.out.println("### File opened in editor: " + file.getFullPath());
                } else {
                    System.out.println("### File not found: " + classFilePath);
                }
            } else {
                System.out.println("### Project not accessible: " + projectName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("### Error opening file in editor: " + e.getMessage());
        }
    }
    
    public void countGeneratedAnnotations() {
        survivalTasks=0;
        preservationTasks=0;
        Pattern annotationPattern = Pattern.compile("@Generated\\((.*?)\\)");
        try {
            IProject project = getCurrentProject();
            if (project != null && project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
                IJavaProject javaProject = JavaCore.create(project);
                IPackageFragment[] packages = javaProject.getPackageFragments();
                List<Interaction> interactions = LogData.getInteractionsJSON(getCurrentProject());
                Log.d("-interacions.size:"+interactions.size());
                for (IPackageFragment myPackage : packages) {
                    if (myPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
                        for (ICompilationUnit unit : myPackage.getCompilationUnits()) {
                            String source = unit.getSource();
                            for (IType type : unit.getTypes()) {
                                for (IMethod method : type.getMethods()) {
                                    int start = method.getSourceRange().getOffset();
                                    int length = method.getSourceRange().getLength();
                                    String methodSource = source.substring(start, start + length);
                                    
                                    Matcher matcher = annotationPattern.matcher(methodSource);
                                    if (matcher.find()) {
                                        survivalTasks++;
                                        String body = extractMethodBody(methodSource);
                                        String annotationContent = matcher.group(1); 
                                        Map<String, String> parsedData = AnnotationParser.parseAnnotation(annotationContent);
                                        String id = parsedData.get("id");
                                        String hash = Hash.md5(body);
                                        //Log.d("-id:"+id);
                                         for (Interaction interaction : interactions) {
                                            if(interaction.getRole().equals("CARET") && interaction.isPassedPreValidations()) {
                                                if(String.valueOf(interaction.getTimestamp()).equals(id)) {
                                                    if(hash.equals(interaction.getHash())) {
                                                        preservationTasks++;
                                                    }
                                                    break;
                                                }
                                            }
                                         }
                                        //--
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public String extractMethodBody(String methodSource) {
        // Create an ASTParser
        ASTParser parser = ASTParser.newParser(AST.JLS17); // Use appropriate JLS level
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS); // We're parsing method(s)
        parser.setSource(methodSource.toCharArray());
        parser.setResolveBindings(false);

        ASTNode node = parser.createAST(null);

        // Expecting a TypeDeclaration with methods
        if (node instanceof TypeDeclaration) {
            TypeDeclaration typeDecl = (TypeDeclaration) node;
            MethodDeclaration[] methods = typeDecl.getMethods();

            if (methods.length > 0) {
                Block body = methods[0].getBody(); // We assume one method in input
                return body != null ? body.toString() : null;
            }
        } else if (node instanceof MethodDeclaration) {
            Block body = ((MethodDeclaration) node).getBody();
            return body != null ? body.toString() : null;
        }

        return null; // Body not found or invalid input AC
    }

}
