package uk.gov.dwp.uc.dip.schemagenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private TechnicalMappingReader techMap;
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
        options.addOption("l", true, "HDFS raw data location e.g. /data/2016-01-01");
        options.addOption("t", true, "transform table, only provided table will be transformed, " +
                "if not provided all target tables will be generated");
        options.addOption("g", false, "output postgres create table(s) script");
        options.addOption("o", false, "output target tables list");
        options.addOption("d", false, "output data checks");

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

            SchemaGenerator generator = new SchemaGenerator(technicalMapping);

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
                System.out.println(generator.transformAsString(targetTable));
            }else {
                // transform all rules
                for (String t : generator.techMap.getTargetTables()) {
                    System.out.println("!echo ------------------------;");
                    System.out.println("!echo ------------------------ " + t + ";");
                    System.out.println("!echo ------------------------;");
                    System.out.println(generator.transformAsString(t));
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
            result.append(transformAsString(t));
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

    private String transformAsString(String targetTable){
        List<String> transforms = transform(targetTable);
        StringBuilder result = new StringBuilder();
        for(String transform : transforms){
            result.append(transform).append(";\n\n");
        }
        return result.toString();
    }

    public List<String> transform(String targetTable) {

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
                        techMap, targetTable, sourceTableGenerator.getTargetSourceTableName()));
            }
        }

        return result;
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
}
