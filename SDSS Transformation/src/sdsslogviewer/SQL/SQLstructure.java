    /**
     * A inner class to store the SQL token width and type.
     */

package sdsslogviewer.SQL;

/**
 *
 * @author James
 */

    public class SQLstructure{
        private String token;
        private int width;
        private int type;

        SQLstructure(){
            this(null,0,0);
        }

        SQLstructure(String tk, int w, int t){
            token=tk;
            width=w;
            type=t;
        }

        public String getToken(){
            return token;
        }

        public void setToken(String tk){
            token=tk;
        }

        public int getWidth(){
            return width;
        }

        public void setWidth(int w){
            width=w;
        }

        public int getType(){
            return type;
        }

        public void setType(int t){
            type=t;
        }

    }
