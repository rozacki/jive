package uk.gov.dwp.uc.dip.jive;

import java.util.Collection;
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

    /***
     * Sets both columns and rows
     * @param content
     */
    public void setContent(List<List<Object>> content) {
        log.debug("");
        this.removeAllItems();

        //remove all columns
        Object[] ids  = this.getContainerPropertyIds().toArray();
        for(Object id: ids){
            log.debug(id);
            this.removeContainerProperty(id);
        }

        Iterator<List<Object>> rowIt = content.iterator();
        if (!rowIt.hasNext()) {
            this.addContainerProperty("No data available", Object.class, null);
            return;
        }
        // add columns
        List<Object> columnNames = rowIt.next();
        for (Object columnValue : columnNames) {
            this.addContainerProperty(columnValue, Object.class, null);
        }
        // add rows
        int i=0;
        while (rowIt.hasNext()) {
            //Iterator<Object> colIt = rowIt.next().iterator();
            Object[] o = rowIt.next().toArray();
            this.addItem(o,i++);
        }
    }

    /***
     * Sets only rows not columns names
     * @param rows
     */
    public void setRows(List<List<Object>> rows){

    }
}
