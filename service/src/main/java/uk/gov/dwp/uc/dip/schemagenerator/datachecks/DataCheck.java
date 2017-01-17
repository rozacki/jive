package uk.gov.dwp.uc.dip.schemagenerator.datachecks;

import uk.gov.dwp.uc.dip.mappingreader.TechnicalMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by chrisrozacki on 13/01/2017.
 */
public class DataCheck {

    /** generates SQL that checks data quality
     * The checks are don\t change the table definitions but run SQL count, max, min etc
     * @param rule
     * @return
     */
    public List<String> getDataQualityChecks(TechnicalMapping rule){
        Set<DataCheckEnum> dataChecks = rule.getDataChecks();
        List<String> ret = new ArrayList<>();

        for(DataCheckEnum dataCheck: dataChecks){
            String sql;
            switch (dataCheck){
                case NOT_NULLABLE:
                    sql = String.format("SELECT COUNT(*) FROM %s WHERE `%s` IS NULL"
                            , rule.targetTableName,rule.targetFieldName);
                    ret.add(sql);
                    break;
                case UNIQUE_DATA_CHECK:
                    //String.format("SELECT month_id, COUNT FROM month_dim GROUP BY month_id HAVING COUNT > 1 limit 1;");
                    sql = String.format("SELECT `%s`, COUNT(*) FROM %s GROUP BY `%s` HAVING COUNT(*) > 1 limit 1"
                            ,rule.targetFieldName
                            ,rule.targetTableName
                            ,rule.targetFieldName
                    );

                    ret.add(sql);
                    break;
            }
        }

        return ret;
    }
}
