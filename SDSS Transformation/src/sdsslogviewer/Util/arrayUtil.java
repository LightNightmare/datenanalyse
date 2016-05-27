/**
 * A simple class to do String array merge and other utilities.<p/>
 * @author Jian Zhang
 */

package sdsslogviewer.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author James
 */
public class arrayUtil {

    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static String[] mergeString(String[]... arrays){

        final List<String> output = new ArrayList();

        for (String[] array : arrays){
            output.addAll(Arrays.asList(array));
        }

        return output.toArray(new String[]{});
    }

/*    public static void main(String[] args){
        String a[] = {"a", "b", "c"};
        String b[] = {"d", "e" };
        String c[] = {"f"};

        String[] merged = null;
        merged = arrayUtil.mergeString(a, b, c);
        System.out.println(Arrays.toString(merged));
    } */
}
