package uk.gov.dwp.uc.dip.jive;

import com.vaadin.ui.*;
import org.apache.log4j.Logger;

/**
 * Created by chrisrozacki on 07/03/2017.
 */
public class MappingEditor extends Panel {

    private final static ConsoleLogger consoleLogger = (ConsoleLogger) Logger.getLogger("ConsoleLogger", new Log4JFactory(JiveUI.Console));

    public MappingEditor(){
        super();
        final VerticalLayout mainLayout = new VerticalLayout();
        final HorizontalLayout schemaMappingLayout = new HorizontalLayout();
        final HorizontalLayout targetTableLayout = new HorizontalLayout();
        final Tree schemaTree = new Tree();
        final Table mappingGrid= new Table();
        final JSONSchemaLoader loadButton = new JSONSchemaLoader("Load schema", schemaTree);
        final TextField database = new TextField("Database");
        final TextField collection = new TextField("Collection");
        final DateField dateTime = new DateField("Date");
        final ComboBox currentTargetTable = new ComboBox("Select target table");
        final Button addTargetTable = new Button("Add target table");
        final TextField newTargetTable = new TextField("New target table");

        this.setContent(mainLayout);
        mainLayout.addComponent(targetTableLayout);
        targetTableLayout.addComponent(database);
        targetTableLayout.addComponent(collection);
        targetTableLayout.addComponent(dateTime);
        targetTableLayout.addComponent(currentTargetTable);
        targetTableLayout.addComponent(newTargetTable);
        targetTableLayout.addComponent(addTargetTable);
        schemaMappingLayout.addComponent(loadButton);
        schemaMappingLayout.addComponent(schemaTree);
        schemaMappingLayout.addComponent(mappingGrid);
        mainLayout.addComponent(schemaMappingLayout);
        mainLayout.addComponent(JiveUI.Console);


        // schema tree
        schemaTree.setCaption("Schema");
        schemaTree.setVisible(false);

        //sourceDB,sourceCollection,sourceFieldLocation,sourceDataType,destinationTable,destinationField,destinationDataType,function,dataQuality
        mappingGrid.addContainerProperty("source DB", String.class, null);
        mappingGrid.addContainerProperty("source collection", String.class, null);
        mappingGrid.addContainerProperty("source field location", String.class, null);
        mappingGrid.addContainerProperty("destination table", String.class, null);
        mappingGrid.addContainerProperty("destination column", String.class, null);
        mappingGrid.addContainerProperty("destination data type", String.class, null);
        mappingGrid.addContainerProperty("destination function", String.class, null);
        mappingGrid.addContainerProperty("destination DQ", String.class, null);
        mappingGrid.setVisible(true);

    }
}
