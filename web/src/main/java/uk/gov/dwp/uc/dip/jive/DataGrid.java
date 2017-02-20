package uk.gov.dwp.uc.dip.jive;

import java.util.Iterator;
import java.util.List;
import com.vaadin.ui.Table;
import org.apache.log4j.Logger;

/**
 * Created by chrisrozacki on 08/02/2017.
 */
public class DataGrid extends Table {

    private final static Logger log = Logger.getLogger(DataGrid.class);
    public DataGrid(){
        this.addItems();
    }

    public void setData(List<List<Object>> data) {
        this.clear();
        Iterator<List<Object>> rowIt = data.iterator();
        if (!rowIt.hasNext()) {
            this.addContainerProperty("No data available", Object.class, null);
            return;
        }
        List<Object> columnNames = rowIt.next();
        // table.addContainerProperty("Name", String.class, null);
        for (Object columnValue : columnNames) {
            this.addContainerProperty(columnValue, Object.class, null);
        }

        int i=0;
        while (rowIt.hasNext()) {
            //Iterator<Object> colIt = rowIt.next().iterator();
            Object[] o = rowIt.next().toArray();
            this.addItem(o,i++);
            log.debug(o);
        }
    }
}
