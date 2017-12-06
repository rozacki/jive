package uk.gov.dwp.uc.dip.schemagenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.opencsv.CSVReader;
import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingException;
import uk.gov.dwp.uc.dip.mappingreader.TechnicalMappingReader;
import uk.gov.dwp.uc.dip.schemagenerator.datachecks.DataCheck;
import uk.gov.dwp.uc.dip.schemagenerator.postgrestable.PostgresTableGenerator;
import uk.gov.dwp.uc.dip.schemagenerator.sourcetable.SourceTableGenerator;
import uk.gov.dwp.uc.dip.schemagenerator.transformtable.TransformTableGenerator;

/**
 * Created by chrisrozacki on 12/10/2016.
 * Generate source tables from Technical Mapping
 */
public class SchemaGenerator {

    private String sourceJsonFileLocation;
    public TechnicalMappingReader techMap;
    private final static Logger log = Logger.getLogger(SchemaGenerator.class);

    public SchemaGenerator() {
    }

    public SchemaGenerator(String tmPath) throws IOException, TechnicalMappingException {
        techMap = TechnicalMappingReader.getInstance(tmPath);
        techMap.read();
    }

    public SchemaGenerator(String tmPath, String sourceJsonFileLocation) throws IOException {
        this(tmPath);
        this.sourceJsonFileLocation = sourceJsonFileLocation;
    }

    public static void main(String[] args) {
        Options options = new Options();

        //todo: group options together for example:
        //-d does not require l, or -t requires -d to be not present
        options.addOption("tm", true, "technical mapping file e.g. /var/tmp/tm.csv");
        options.addOption("l", true, "HDFS raw data location e.g. /data/2017-01-01");
        options.addOption("t", true, "transform table, only provided table will be transformed, " +
                "if not provided all target tables will be generated");
        options.addOption("g", false, "output postgres create table(s) script");
        options.addOption("o", false, "output target tables list");
        options.addOption("d", false, "output data checks");
        options.addOption("orc", false, "store table as orc");
        options.addOption("avro", false, "store table as avro");
        options.addOption("where", true, "target table name to where clause to append at the end of create .. statement");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

            String technicalMapping ="";
            if (cmd.hasOption("tm")) {
                technicalMapping = (cmd.getParsedOptionValue("tm")).toString();
            }else{
                help(options);
            }

            String targetTable="";
            if (cmd.hasOption("t")) {
                targetTable = (cmd.getParsedOptionValue("t")).toString();
            }

            boolean generateTargetDBCreateTable = cmd.hasOption("g");
            boolean listTargetTable = cmd.hasOption("o");
            boolean generateDataQualityChecks = cmd.hasOption("d");
            String storeTableAs = "";
            if (cmd.hasOption("orc"))
                storeTableAs = "STORED AS ORC";
            if(cmd.hasOption("avro"))
                storeTableAs = "STORED AS AVRO";

            SchemaGenerator generator = new SchemaGenerator(technicalMapping);
            HashMap<String,String> wheres = new LinkedHashMap<>();

            if(cmd.hasOption("where")){
                wheres = getWhereClauses(cmd.getParsedOptionValue("where").toString());
            }

            if (cmd.hasOption("l")) {
                generator.setSourceJsonFileLocation((cmd.getParsedOptionValue("l")).toString());
            }else{
                System.err.println("missing location parameter");
                help(options);
            }

            if(generateTargetDBCreateTable) {
                PostgresTableGenerator postgresTableGenerator = new PostgresTableGenerator();
                if (targetTable.length() > 0) {
                    System.out.println(postgresTableGenerator.getSqlForTable(generator.techMap, targetTable));
                }else {
                    // transform all rules
                    for (String t : generator.techMap.getTargetTables()) {
                        System.out.println(postgresTableGenerator.getSqlForTable(generator.techMap, t));
                    }
                }
            }else if(listTargetTable){
                for (String table : generator.techMap.getTargetTables()) {
                    System.out.println(table);
                }
            }else if(generateDataQualityChecks){
                if(targetTable.length()>0) {
                    System.out.println("!echo ------------------------;");
                    System.out.println("!echo ------------------------ " + targetTable + ";");
                    System.out.println("!echo ------------------------;");
                    System.out.println(generator.dataCheckAsString(targetTable));
                }else{
                    // transform all rules
                    for (String t : generator.techMap.getTargetTables()) {
                        System.out.println("!echo ------------------------;");
                        System.out.println("!echo ------------------------ " + t + ";");
                        System.out.println("!echo ------------------------;");
                        System.out.println(generator.dataCheckAsString(t));
                    }
                }
            }
            else if(targetTable.length()>0) {
                // transform single table
                System.out.println("!echo ------------------------;");
                System.out.println("!echo ------------------------ " + targetTable + ";");
                System.out.println("!echo ------------------------;");
                System.out.println(generator.transformAsString(targetTable, storeTableAs, wheres));
            }else {
                // transform all rules
                for (String t : generator.techMap.getTargetTables()) {
                    System.out.println("!echo ------------------------;");
                    System.out.println("!echo ------------------------ " + t + ";");
                    System.out.println("!echo ------------------------;");
                    System.out.println(generator.transformAsString(t, storeTableAs, wheres));
                }
            }
        } catch (ParseException e) {
            help(options);
        } catch (IOException | TechnicalMappingException e){
            // TODO perhaps do something nicer with TechnicalMappingException
            e.printStackTrace();
        }
    }

    public String transformAll(){
        StringBuilder result = new StringBuilder();
        for (String t : techMap.getTargetTables()) {
            result.append("!echo ------------------------;\n");
            result.append("!echo ------------------------ ").append(t).append(";\n");
            result.append("!echo ------------------------;\n");
            result.append(transformAsString(t, "", new LinkedHashMap<>()));
        }
        return result.toString();
    }

    public List<String> transformAllToList(){
        List<String> transforms = new ArrayList<>();
        for (String t : techMap.getTargetTables()) {
            transform(t).forEach(s -> transforms.add(s) );
        }
        return transforms;
    }

    private String transformAsString(String targetTable, String storeTableAs, HashMap<String,String> wheres){
        List<String> transforms = transformStoreaAs(targetTable, storeTableAs, wheres);
        StringBuilder result = new StringBuilder();
        for(String transform : transforms){
            result.append(transform).append(";\n\n");
        }
        return result.toString();
    }

    public List<String> transformStoreaAs(String targetTable, String storeTableAs, HashMap<String,String> wheres){
        List<String> result = new ArrayList<>();
        // SOURCE STEP
        // TODO there's only going to be one source database per table (for foreseeable future).
        // TODO remove next.
        for(String sourceDatabase: techMap.getSourceDatabases(targetTable)) {
            // TODO similarly, 1:1 collection mapping
            for (String sourceCollection : techMap.getSourceCollection(targetTable, sourceDatabase)) {

                // SOURCE STEP
                SourceTableGenerator sourceTableGenerator = new SourceTableGenerator(sourceJsonFileLocation);
                result.addAll(sourceTableGenerator.generateSqlForTable(
                        techMap, targetTable, sourceDatabase, sourceCollection));

                // TRANSFORM STEP
                TransformTableGenerator transformTableGenerator = new TransformTableGenerator();
                result.addAll(transformTableGenerator.generateSqlForTable(
                        techMap, targetTable, sourceTableGenerator.getTargetSourceTableName(), storeTableAs, wheres));
            }
        }

        return result;
    }

    public List<String> transform(String targetTable) {
        return transformStoreaAs(targetTable, "", new LinkedHashMap<>());
    }

    public String dataCheckAsString(String targetTable){
        List<String> dataChecks = generateDataChecks(targetTable);
        StringBuilder result = new StringBuilder();
        for(String dataCheck : dataChecks){
            result.append(dataCheck).append(";\n\n");
        }
        return result.toString();
    }

    public List<String> generateDataChecks(String targetTable){
        List<String> result = new ArrayList<>();
        List<TechnicalMapping> rules =  techMap.getTargetColumns(targetTable);
        DataCheck dataCheck = new DataCheck();

        for(TechnicalMapping rule: rules){
            result.addAll(dataCheck.getDataQualityChecks(rule));
        }

        return result;
    }

    private static void help(Options o){
        try {
            SchemaGenerator s = new SchemaGenerator();
            HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp("java -cp validateschemaspike-1.0-SNAPSHOT-jar-with-dependencies.jar " +
                    s.getClass().getCanonicalName() +
                    " -tm sample-data/mongoMappings.xlsx -l /etl/uc/mongo/2016-11-28 \n\n",o);
        } catch (Exception ignored) {

        }

        System.exit(0);
    }

    public void setSourceJsonFileLocation(String sourceJsonFileLocation) {
        this.sourceJsonFileLocation = sourceJsonFileLocation;
    }

    /***
     * Loads map of target-table=where-clause that will ba attached at the end of the
     * @return
     */
    private static HashMap<String,String> getWhereClauses(String filePath) throws IOException{
        HashMap<String,String> wheres = new LinkedHashMap<>();
        CSVReader reader = new CSVReader(new FileReader(filePath));
        List<String[]> rows=reader.readAll();

        for(String row[] : rows){
            wheres.put(row[0], row[1]);
        }

        return wheres;
    }
}
