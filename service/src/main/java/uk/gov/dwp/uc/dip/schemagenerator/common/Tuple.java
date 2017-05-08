package uk.gov.dwp.uc.dip.schemagenerator.common;

/**
 * Created by chrisrozacki on 08/05/2017.
 */
public class Tuple<X, Y> {
    public final X x;
    public final Y y;
    public Tuple(X x, Y y) {
        this.x = x;
        this.y = y;
    }
}

