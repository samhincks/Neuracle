

package dao.datalayers;

import java.sql.ResultSet;
import java.sql.SQLException;

/**UserDAO: not really in use, for logging in and registering
 * @author 
 */
public class UserDAO {
    private static String userId = null;
    public String login(String name, String password) throws SQLException{
        MySqlDAO mysqlDao = new MySqlDAO();
        mysqlDao.connSQL();
        ResultSet userCount = mysqlDao.selectSQL("select * from userinfo where UserName = '"+name+"' and UserPwd='"+password+"'");
        if (userCount.next()) {
            String id = userCount.getString(1);
            userCount.close();
            return id;
        } else {
            userCount.close();
            return null;
        }
    }

    /**
     * @return the userId
     */
    public static String getUserId() {
        return userId;
    }

    /**
     * @param aUserId the userId to set
     */
    public static void setUserId(String aUserId) {
        userId = aUserId;
    }
}
