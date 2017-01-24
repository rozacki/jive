package uk.gov.dwp.uc.dip.jive.hiverun;

import org.apache.commons.lang.exception.ExceptionUtils;

@SuppressWarnings("WeakerAccess")
public class StatementResult {

    class BeanProperties{
        final static String ORDER = "order";
        final static String STATEMENT = "statement";
        final static String SUCCESS = "success";
    }
    private boolean success = false;
    private String statement;
    private int order;

    StatementResult(boolean success, String statement, int order) {
        this.success = success;
        this.statement = statement;
        this.order = order;
    }

    StatementResult(boolean success, Exception e, int order) {
        this(success, ExceptionUtils.getFullStackTrace(e), order);
    }

    @SuppressWarnings("unused")
    public boolean isSuccess() {
        return success;
    }

    @SuppressWarnings("unused")
    public String getStatement() {
        return statement;
    }

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatementResult that = (StatementResult) o;

        return getOrder() == that.getOrder();

    }

    @Override
    public int hashCode() {
        return getOrder();
    }


}
