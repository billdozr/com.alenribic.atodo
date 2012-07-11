package com.alenribic.atodo.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.TableItem;

import com.alenribic.atodo.model.MetaInfo;
import com.alenribic.atodo.model.TodoEntry;
import com.alenribic.atodo.model.TodoEntryError;

public final class ATodoViewDataModifier {
	private ATodoViewDataModifier() {}
	
	public static void removeAllByLocation(TableViewer viewer, final String location) {
		TableItem[] items = viewer.getTable().getItems();
		List filteredData = new ArrayList();
		for (TableItem tableItem : items) {
			MetaInfo srcInfo = null;
			if (tableItem.getData() instanceof TodoEntry) {
				srcInfo = ((TodoEntry)tableItem.getData()).getSrcInfo();
			} else if (tableItem.getData() instanceof TodoEntryError) {
				srcInfo = ((TodoEntryError)tableItem.getData()).getSrcInfo();
			}
			if (location.equals(srcInfo.getSrcName())) {
				filteredData.add(tableItem.getData());
			}
		}
		viewer.remove(filteredData.toArray());
	}
	
	public static void createAllByLocation(TableViewer viewer, 
			final String location, 
			final String finderPath, 
			final String cruncherPath, 
			final String[] srcFileExts) throws Exception {
		ATodoViewDataProvider dataProvider = 
			new ATodoViewDataProvider(finderPath, cruncherPath, srcFileExts, location);
		Object[] data = dataProvider.getData();
		viewer.add(data);
	}
	
	/**
	 * 
	 * @param viewer
	 * @param location
	 * @param finderPath
	 * @param cruncherPath
	 * @param srcFileExts
	 * @return Number of TodoEntry(s) and TodoEntryError(s)
	 * @throws Exception
	 */
	public static int[] refreshAllByLocation(TableViewer viewer, 
			final String location, 
			final String finderPath, 
			final String cruncherPath, 
			final String[] srcFileExts) throws Exception {
		removeAllByLocation(viewer, location);
		createAllByLocation(viewer, location, finderPath, cruncherPath, srcFileExts);
		return ATodoViewDataProvider.getNumItems(viewer.getTable());
	}
}
