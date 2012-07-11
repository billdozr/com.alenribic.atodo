package com.alenribic.atodo.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import com.alenribic.atodo.ATodoActivator;
import com.alenribic.atodo.model.MetaInfo;
import com.alenribic.atodo.model.TodoEntry;
import com.alenribic.atodo.model.TodoEntryError;
import com.alenribic.atodo.preferences.ATodoPreferencePage;
import com.alenribic.atodo.preferences.PreferenceConstants;
import com.alenribic.atodo.util.ATodoLog;
import com.alenribic.atodo.util.StringUtils;

public class ATodoView extends ViewPart implements IResourceChangeListener {
	private TableViewer tableViewer;
	private Link prefLinkControl;
	private String finderPath;
	private String cruncherPath;
	private int selectedIndex = 0;
	
	public static String[] SRC_FILE_EXT = new String[] { 
		".java", ".py", ".c", ".cpp", ".hs", 
		".clj", ".html", ".css", ".js", ".groovy",
		".php", ".rb", ".properties" };

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			Object[] results = null;
			try {
				ATodoViewDataProvider dataProvider = 
					new ATodoViewDataProvider(finderPath, cruncherPath, SRC_FILE_EXT);
				results = dataProvider.getData();
				int[] numItems = ATodoViewDataProvider.getNumItems(results);
		        setSummary(numItems[0], numItems[1]);
			} catch (Exception e) {
				ATodoLog.logError(e);
				throw new RuntimeException(
						"ATodo: parsing process failed -> " + 
						e.getMessage(), e);
			}
			return results;
		}
	}
	class ViewLabelProvider extends LabelProvider 
		implements ITableLabelProvider {

		public Image getColumnImage(Object element, int index) {
			if (element instanceof TodoEntryError) {
				switch (index) {
					case 2 :
						return ATodoActivator.getImageDescriptor(
								ATodoActivator.ICONS_PATH + "error.gif")
								.createImage();
					default :
						return null;
				}
			}
			return null;
		}

		public String getColumnText(Object element, int index) {
			if (element instanceof TodoEntry) {
				TodoEntry entry = (TodoEntry)element;
				switch (index) {
				    case 0 :
				    	return entry.getLabels() != null && entry.getLabels().length > 0
				    		? StringUtils.join(entry.getLabels(), ", ") : "No labels";
				    case 1 :
				    	return entry.getUsers() != null && entry.getUsers().length > 0
			    			? StringUtils.join(entry.getUsers(), ", ") : "No users";
					case 2 :
						return entry.getSubject() != null 
							? entry.getSubject() : "None";
					case 3 :
						return entry.getAction();
					case 4 :
						return entry.getPriority() != null 
							? entry.getPriority().toString() : "None";
					case 5 :
						return entry.getTimeSpent() != null 
							? entry.getTimeSpent().toString() : "None";
					case 6 :
						return entry.getSrcInfo().getSrcName().replace(
								ResourcesPlugin.getWorkspace().getRoot()
									.getLocation().toString(), "");
					case 7 :
						return "line " + entry.getSrcInfo().getSrcLine();
					default :
						break;
				}	
			} else if (element instanceof TodoEntryError) {
				TodoEntryError error = (TodoEntryError)element;
				switch (index) {
					case 2 :
						return "ERROR";
					case 3 :
						return error.getMessage().replace("\n", " :: ");
					case 6 :
						return error.getSrcInfo().getSrcName().replace(
								ResourcesPlugin.getWorkspace().getRoot()
									.getLocation().toString(), "");
					case 7 :
						return "line " + error.getSrcInfo().getSrcLine();
					default :
						break;
				}
			}
			return "";
		}
	}	
	class SortBySrcNameAndErr extends ViewerSorter {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			MetaInfo m1 = e1 instanceof TodoEntry 
				? ((TodoEntry)e1).getSrcInfo() 
				: ((TodoEntryError)e1).getSrcInfo();
			MetaInfo m2 = e2 instanceof TodoEntry 
				? ((TodoEntry)e2).getSrcInfo() 
				: ((TodoEntryError)e2).getSrcInfo();				
			int srcComp = m1.getSrcName()
				.compareTo(m2.getSrcName());
			return srcComp;
		}
	}
	private final IPropertyChangeListener propertyChangeListener
		= new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(PreferenceConstants.TODO_FINDER_PATH)
				|| event.getProperty().equals(PreferenceConstants.TODO_CRUNCHER_PATH)) {
				loadPaths();
				if (isValidPreferences() && prefLinkControl != null) {
					Composite parent = prefLinkControl.getParent();
					prefLinkControl.dispose();
					prefLinkControl = null;
					createTableViewer(parent);
					parent.layout();
				} else if (!isValidPreferences() && tableViewer != null) {
					Composite parent = tableViewer.getTable().getParent();
					tableViewer.getTable().dispose();
					tableViewer = null;
					createLinkControl(parent);
					parent.layout();
				}
			}
		}
	};

	/**
	 * The constructor.
	 */
	public ATodoView() {
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		loadPaths();
		if (isValidPreferences()) {
			createTableViewer(parent);
		} else {
			createLinkControl(parent); // User needs to provide configuration
		}
		ATodoActivator.getDefault().getPreferenceStore()
			.addPropertyChangeListener(propertyChangeListener);
	}
	
	private void createLinkControl(Composite parent) {
		prefLinkControl = new Link(parent, SWT.NONE);
		prefLinkControl.setText(
				"The ATodo \"finder\" and \"cruncher\" executables could not be located.\n" +
				"Provide the location via the <a>Preferences</a> page");
		prefLinkControl.setVisible(true);
		prefLinkControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferenceManager mgr = new PreferenceManager();
				IPreferencePage page = new ATodoPreferencePage();
				IPreferenceNode node = new PreferenceNode("1", page);
				mgr.addToRoot(node);
				PreferenceDialog dialog = new PreferenceDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), mgr);
				dialog.create();
				dialog.setMessage(page.getTitle());
				dialog.setBlockOnOpen(false);
				dialog.open();
			}
		});
	}
	
	private void setSummary(int numTasks, int numErrors) {
		StringBuilder summary = new StringBuilder()
			.append(numTasks)
			.append(" task");
		if (numTasks != 1) summary.append("s");
		summary.append(", ")
			.append(numErrors)
			.append(" error");
		if (numErrors != 1) summary.append("s");
		setContentDescription(summary.toString());
	}
	
	private void clearSummary() {
		setContentDescription("");
	}
	
	private void createTableViewer(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL);
		final Table table = tableViewer.getTable();
		table.setLayout(new TableLayout());
		//table.layout(true);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		String[] columnNames = new String[] {
			"Labels", "Users", "Subject", "Action", "Priority", "Time spent", "Path", "Location"};
		int[] columnWidths = new int[] {
				68, 68, 120, 300, 55, 90, 210, 70};
		int[] columnAlignments = new int[] {
				SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.CENTER, SWT.CENTER, SWT.LEFT, SWT.LEFT};
		for (int i = 0; i < columnNames.length; i++) {
			TableColumn tableColumn = 
				new TableColumn(table, columnAlignments[i]);
			tableColumn.setText(columnNames[i]);
			tableColumn.setWidth(columnWidths[i]);
		}
		tableViewer.setContentProvider(new ViewContentProvider());
		tableViewer.setLabelProvider(new ViewLabelProvider());
		tableViewer.setSorter(new SortBySrcNameAndErr());
		tableViewer.setInput(getViewSite());
		tableViewer.addOpenListener(new IOpenListener() {
			@Override
			public void open(OpenEvent event) {
				if (event.getSelection() instanceof StructuredSelection) {
					Object[] selected = ((StructuredSelection)event.getSelection()).toArray();
					if (selected.length > 0) {
						MetaInfo srcInfo = null;
						if (selected[0] instanceof TodoEntry)
							srcInfo = ((TodoEntry)selected[0]).getSrcInfo();
						else srcInfo = ((TodoEntryError)selected[0]).getSrcInfo();
						IWorkbenchWindow dw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = null;
						if (dw != null) page = dw.getActivePage();
						if (page != null) {
							IPath path = ResourcesPlugin.getWorkspace().getRoot().getProjectRelativePath()
								.append(srcInfo.getSrcName().replace(ResourcesPlugin.getWorkspace().getRoot()
									.getLocation().toString(), ""));
							IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
							try {
								// preserve current selection in viewer 
								// as a refresh will occur post openEditor call 
								preserveSelection();
								IMarker marker = file.createMarker(IMarker.LINE_NUMBER);
								marker.setAttribute(IMarker.LINE_NUMBER, srcInfo.getSrcLine());
								IDE.openEditor(page, marker, false);
							} catch (PartInitException e) {
								e.printStackTrace();
							} catch (CoreException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		// Resource Change Listener required for refreshing the table viewer
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		tableViewer.getTable().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				clearSummary();
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(ATodoView.this);
			}
		});
	}
	
	private void preserveSelection() {
		if (tableViewer != null)
			selectedIndex = tableViewer.getTable().getSelectionIndex();
	}
	
	private void restoreSelection() {
		if (tableViewer != null)
			tableViewer.getTable().setSelection(selectedIndex);
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		if (tableViewer != null) tableViewer.getControl().setFocus(); 
		else prefLinkControl.setFocus();
	}
	
	private boolean isValidPreferences() {
		if ("".equals(finderPath) || "".equals(cruncherPath))
			return false;
		return true;
	}

	private void loadPaths() {
		IPreferenceStore prefs = ATodoActivator
			.getDefault().getPreferenceStore();
		finderPath = prefs.getString(PreferenceConstants.TODO_FINDER_PATH);
		cruncherPath = prefs.getString(PreferenceConstants.TODO_CRUNCHER_PATH);
	}
	
	private boolean supportedSourceType(String resourceName) {
		for (int i = 0; i < SRC_FILE_EXT.length; i++) {
			if (resourceName.endsWith(SRC_FILE_EXT[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try{
			if(event.getType() == IResourceChangeEvent.POST_CHANGE){
				event.getDelta().accept(new IResourceDeltaVisitor(){
					public boolean visit(IResourceDelta delta) throws CoreException {
						if(supportedSourceType(delta.getResource().getName()) && delta.getFlags() != IResourceDelta.MARKERS) {
							final String location = delta.getResource().getProject().getLocation()
								+ System.getProperties().getProperty("file.separator")
								+ delta.getResource().getProjectRelativePath();
							ATodoLog.logInfo("File effected: " + location);
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									try {
										int[] numItems = ATodoViewDataModifier.refreshAllByLocation(
												tableViewer, location, finderPath, cruncherPath, SRC_FILE_EXT);
										setSummary(numItems[0], numItems[1]);
									} catch (Exception e) {
										ATodoLog.logError(e);
										throw new RuntimeException(e.getMessage(), e);
									}
									// restore selection after the refresh of the viewer
									restoreSelection(); 
								}
							});
						}
						return true;
					}
				});
			}
		} catch(CoreException e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void dispose() {
        super.dispose();
        ATodoActivator.getDefault().getPreferenceStore()
        	.removePropertyChangeListener(propertyChangeListener);
	}
}