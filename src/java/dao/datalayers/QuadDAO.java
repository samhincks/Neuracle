
package dao.datalayers;

import java.util.ArrayList;
import org.json.JSONObject;
import timeseriestufts.kth.streams.DataLayer;
import timeseriestufts.kth.streams.quad.MultiExperiment;

/** For now just a formality, since we can't really visualize so called quad daos
 * @author samhincks
 */
public class QuadDAO extends DataLayerDAO{
    public ArrayList<TriDAO> piles; //.. the underlying piles, which may, for isntance, hold technique set
    public QuadDAO(MultiExperiment multi, ArrayList<TriDAO> piles) {
        this.piles= piles;
        this.dataLayer = multi;
        this.setId(multi.id);
    }

    @Override
    public JSONObject getJSON() throws Exception {
        JSONObject jsonObj = new JSONObject();
        //jsonObj.put("error", "cannot visualize multiple experiments");
        return jsonObj;
    }

}
