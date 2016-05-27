/* Written by FlorianB for SQLQueryLogTransformer
 * This was a try with transformation the XML files offered by the SDSS Sky Server.
 * Due to multiple errors within the files this was discarded. CSV files were prefered.
 * This can be used as a basis if ever wished for.
 */

package transformation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.SQLException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class XMLparser {

	/**
	 * @param string
	 * @throws XMLStreamException 
	 * @throws FileNotFoundException 
	 * @throws SQLException 
	 */
	public static void main(String xmlFile) throws XMLStreamException, FileNotFoundException, SQLException {
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader( new FileInputStream(xmlFile));

		StringBuilder spacer = new StringBuilder();
		int count=0;
		String[] tuple = new String[18];//18 columns of interest in SQL Log 

		while ( parser.hasNext() )
		{
		  //System.out.println( "Event: " + parser.getEventType() );

		  switch ( parser.getEventType() )
		  {
		    case XMLStreamConstants.START_DOCUMENT:
		      //System.out.println( "START_DOCUMENT: " + parser.getVersion() );
		      break;

		    case XMLStreamConstants.END_DOCUMENT:
		      //System.out.println( "END_DOCUMENT: " );
		      parser.close();
		      break;

		    case XMLStreamConstants.NAMESPACE:
		      //System.out.println( "NAMESPACE: " + parser.getNamespaceURI() );
		      break;

		    case XMLStreamConstants.START_ELEMENT:
		      spacer.append( "  " );
		      //System.out.println( spacer + "START_ELEMENT: " + parser.getLocalName() );

		      if(parser.getLocalName()=="row"){
				    count++;
		      	//	System.out.println(spacer + "  Row: #"+ String.valueOf(count));
		      		for ( int i = 0; i < parser.getAttributeCount(); i++ ){
		      		//	System.out.println( spacer + "  Attribut: "
		              //              + parser.getAttributeLocalName( i )
		                //            + " Wert: " + parser.getAttributeValue( i ) );
		      			if(i<tuple.length) tuple[i]=parser.getAttributeValue( i );
		      		}
		      		//for ( int i = 0; i <tuple.length; i++ )System.out.println(tuple[i]);
		      		transformation.Transform.saveTuple(tuple);
		      }
		      break;

		    case XMLStreamConstants.CHARACTERS:
		      if ( ! parser.isWhiteSpace() )
		        //System.out.println( spacer + "  CHARACTERS: " + parser.getText() );
		      break;

		    case XMLStreamConstants.END_ELEMENT:
		      //System.out.println( spacer + "END_ELEMENT: " + parser.getLocalName() );
		      if(spacer.length()>=2) spacer.delete( (spacer.length() - 2), spacer.length() );
		      break;

		    default:
		      break;
		  }
		  try{
			  parser.next();
		  } catch (XMLStreamException e){
			  System.out.println(e.getMessage());
		  } catch (NullPointerException e){
			  System.out.println(e.getMessage());
			  break;
		  }	  
		}
    	System.out.println("Rows: "+ String.valueOf(count));
	}
}
