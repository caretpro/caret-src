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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import caret.data.ChatData;
import caret.data.Interaction;
import caret.project.Resource;
import caret.project.java.JavaProject;
import caret.project.java.MethodReplacer;
import caret.project.java.ProjectAnalyzer;
import caret.project.java.ProjectStructure;
import caret.stats.StatisticsQuery;
import caret.tasks.JavaConcept;
import caret.tasks.Task;
import caret.tasks.TasksManager;
import caret.tool.AnnotationParser;
import caret.tool.Hash;
import caret.tool.Log;
import caret.tool.Tuple;
import caret.tool.Util;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StatisticsViewLog {

	private static StatisticsViewLog statisticsViewLog;
    TreeViewer treeViewer;
    int requestedTasks = 0;
    int acceptedTasks = 0;
    int rejectedTasks = 0;
    int survivalTasks = 0;
    int preservationTasks = 6;
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
    
    public static StatisticsViewLog getInstance() {
    	return statisticsViewLog;
    }
    
    @PostConstruct
    public void createPartControl(Composite parent) {
    	
    	this.statisticsViewLog = this;
    	Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false)); // 1 column for the label and tree viewer
        List<Task> tasks = TasksManager.getPreferenceTasks(); 
		String listTasks = ""; 
		int totalClasses = 0;
		int totalMethods = 0;
		int totalPackages = 0;
		
		for (Task itemTask : tasks) {
			listTaskCounter.put(itemTask.getName(), 0);
		}
        Color colorComposite = composite.getBackground();
        Color colorDarkComposite = new Color(Display.getCurrent(), 247, 247, 247);
        Combo combo2 = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        String[] items = {
            "The most used agent",
            "The most accepted agent",
            "The agent with the best preservation rate",
            "The best agent that generates code requiring no modification",
            "The best agent for optimization tasks",
            "The best agent for documentation tasks",
            "The best agent is best for",
            "The most useful agent for a user"
        };
        combo2.setItems(items);
        combo2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        combo2.select(0);
        Label label = new Label(composite, SWT.NONE);
        label.setText("Agent: ");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        
        try {
    		System.out.print("----get query----");
    		List<StatisticsQuery> sqs = StatisticsQuery.getQueriesJSON(chatView.getCurrentProject());
    		for(StatisticsQuery sq: sqs ) {
    			System.out.println("----query----");
    			Label dynamicLabel = new Label(composite, SWT.NONE);
                dynamicLabel.setText(sq.getQuery());
                dynamicLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    			System.out.println("###query----");
    		}
		} catch (Exception e) {
			//e.printStackTrace();
		}
    }

}
