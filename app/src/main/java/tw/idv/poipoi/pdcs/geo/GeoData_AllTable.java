package tw.idv.poipoi.pdcs.geo;

import java.io.Serializable;

/**
 * Created by DuST on 2017/4/26.
 */

public class GeoData_AllTable implements Serializable {

    public String NAME;
    public String D_NAME;
    public String E_NAME;
    public String CODE;

    public String NAME_103;
    public String D_NAME_103;
    public String E_NAME_103;
    public String CODE_103;

    public Polygon[] polygons;

}
