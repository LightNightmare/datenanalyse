package sdsslogviewer.SQL;

/**
 * Exception occurred in parsing SDSS Log data. Thrown by SQLParser<p/>
 * @author James
 */
@SuppressWarnings("serial")
public class LogParseException extends Exception{

    LogParseException(){
        super("");
    };
}
