import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVWriter;

public class Shell {

	public static void main(String... args) {

		/******
		 * Try connecting to the server.
		 ******/

		Connection connection = null;
		ResultSet result = null;
		ResultSetMetaData rsmd = null;
		String SQL_DISTRIBUTION = "SELECT YY, MM, COUNT(MM) " 
								+ "FROM PARSED_STATEMENTS "
								+ "WHERE YY >= 2003 AND YY <= 2005 "
								+ "GROUP BY YY, MM "
								+ "ORDER BY YY, MM";
		String SQL_DATA_SLICE_1 = "SELECT * FROM "
								+ "BDCOURSE.PARSED_STATEMENTS "
								+ "WHERE thetime >= '01-APR-03 00.00.00.000000000 AM' and thetime <= '30-JUN-04 12.00.0.000000000 PM'";
		String SQL_DATA_SLICE_3 = "SELECT * FROM "
				+ "BDCOURSE.PARSED_STATEMENTS "
				+ "WHERE thetime >= '01-APR-03 00.00.00.000000000 AM' and thetime <= '30-JUN-04 12.00.0.000000000 PM' "
				+ "AND can_be_stifle = 0";
		
		String SQL_DATA_SLICE_4 = "SELECT * FROM "
				+ "from parsed_statements "
				+ "WHERE thetime >= '01-APR-03 00.00.00.000000000 AM' and thetime < '30-JUN-04 12.00.0.000000000 PM' "
				+ "and parsed_statements.CAN_BE_STIFLE = 0 "
				+ "and STAT_ID not in(select id from FROM_WHERE_STATEMENTS where count > 1000 and DISTINCT_IPS_COUNT < 20) ";

		try {
			connection = DriverManager.getConnection("jdbc:oracle:thin:@marsara.ipd.kit.edu:1521:student", "bdcourse",
					"bdcourse");
			result = connection.prepareStatement(SQL_DATA_SLICE_4).executeQuery();
			rsmd = result.getMetaData();
			
			System.out.println("We got a result!");
			
			/******
			 * Save result 
			 ******/
			String path = "slice-4.csv";
			int colCount = rsmd.getColumnCount();
			List<String[]> buffer = new LinkedList<String[]>();
			int writtenRows = 0;
			final int bufferThreshold = 1000;
			int filterHits = 0;
			
			String[] headers = new String[colCount];
			for (int i = 1; i <= colCount; i++) {
				headers[i-1] = rsmd.getColumnName(i);
			}
			buffer.add(headers);
			
			while (result.next()) {
				if (buffer.size() >= bufferThreshold) {
					writeBuffer(path, buffer);
					writtenRows += buffer.size();
					System.out.println(writtenRows + " rows written. Filtered out " + filterHits + " rows.");
					buffer = new LinkedList<String[]>();
				}
				
				String[] line = new String[colCount];
				for (int i = 1; i <= colCount; i++) {
					String columnValue = result.getString(i);
					line[i-1] = columnValue;
				}
				
				if (containsUDF(line[17])) { //The column with the whole statement.
					filterHits++;
					//continue;
				} else {
				
					buffer.add(line);
				}
			}
			
			writeBuffer(path, buffer);
			writtenRows += buffer.size();
			System.out.println(writtenRows + " rows written. Filtered out " + filterHits + " rows.");
			buffer = new LinkedList<String[]>();
			
			connection.close();
		} catch (SQLException e) {
			System.out.println("Could not establish server connection. " + e);
		}

		/******
		 * Try writing a CSV file. A tutorial can be found here:
		 * http://viralpatel.net/blogs/java-read-write-csv-file/
		 ******/

	}
	
	private static void writeBuffer(String path, List<String[]> buffer) {
		String csv = path;
		CSVWriter writer = null;

		try {
			File f = new File(csv);
			if(f.exists() && !f.isDirectory()) { 
				writer = new CSVWriter(new FileWriter(f, true),';');
			} else if (!f.exists() && !f.isDirectory()){
				f.createNewFile();
				writer = new CSVWriter(new FileWriter(f, true),';');
			} else {
				System.out.println("That is no file, it's a directory!");
			}

			writer.writeAll(buffer);
			writer.close();
		} catch (IOException e) {
			System.out.println("Could not write file." + e);
		}
	}
	
	private static boolean containsUDF(String clause) {
		if (clause.contains("fCamcol".toLowerCase())
				|| clause.contains("fCoordsFromEq".toLowerCase())
				|| clause.contains("fCoordType".toLowerCase())
				|| clause.contains("fCoordTypeN".toLowerCase())
				|| clause.contains("fDatediffSec".toLowerCase())
				|| clause.contains("fDistanceArcMinEq".toLowerCase())
				|| clause.contains("fDistanceArcMinXYZ".toLowerCase())
				|| clause.contains("fDMS".toLowerCase())
				|| clause.contains("fDocColumns".toLowerCase())
				|| clause.contains("fDocFunctionParams".toLowerCase())
				|| clause.contains("fEnum".toLowerCase())
				|| clause.contains("fEqFromMuNu".toLowerCase())
				|| clause.contains("fEtaToNormal".toLowerCase())
				|| clause.contains("fFiber".toLowerCase())
				|| clause.contains("fField".toLowerCase())
				|| clause.contains("fFieldMask".toLowerCase())
				|| clause.contains("fFieldMaskN".toLowerCase())
				|| clause.contains("fFieldQuality".toLowerCase())
				|| clause.contains("fFieldQualityN".toLowerCase())
				|| clause.contains("fFirstFieldBit".toLowerCase())
				|| clause.contains("fFramesStatus".toLowerCase())
				|| clause.contains("fFramesStatusN".toLowerCase())
				|| clause.contains("fGetJpegObjects".toLowerCase())
				|| clause.contains("fGetLonLat".toLowerCase())
				|| clause.contains("fGetNearbyFrameEq".toLowerCase())
				|| clause.contains("fGetNearbyObjAllXYZ".toLowerCase())
				|| clause.contains("fGetNearbyObjEq".toLowerCase())
				|| clause.contains("fGetNearbyObjWWT".toLowerCase())
				|| clause.contains("fGetNearbyObjXYZ".toLowerCase())
				|| clause.contains("fGetNearestFrameEq".toLowerCase())
				|| clause.contains("fGetNearestFrameidEq".toLowerCase())
				|| clause.contains("fGetNearestObjEq".toLowerCase())
				|| clause.contains("fGetNearestObjIdEq".toLowerCase())
				|| clause.contains("fGetNearestObjIdEqMode".toLowerCase())
				|| clause.contains("fGetNearestObjIdEqType".toLowerCase())
				|| clause.contains("fGetNearestObjXYZ".toLowerCase())
				|| clause.contains("fGetObjFromRect".toLowerCase())
				|| clause.contains("fGetUrlExpEq".toLowerCase())
				|| clause.contains("fGetUrlExpId".toLowerCase())
				|| clause.contains("fGetUrlFitsAtlas".toLowerCase())
				|| clause.contains("fGetUrlFitsBin".toLowerCase())
				|| clause.contains("fGetUrlFitsCFrame".toLowerCase())
				|| clause.contains("fGetUrlFitsField".toLowerCase())
				|| clause.contains("fGetUrlFitsMask".toLowerCase())
				|| clause.contains("fGetUrlFitsSpectrum".toLowerCase())
				|| clause.contains("fGetUrlFrameImg".toLowerCase())
				|| clause.contains("fGetUrlNavEq".toLowerCase())
				|| clause.contains("fGetUrlNavId".toLowerCase())
				|| clause.contains("fGetUrlSpecImg".toLowerCase())
				|| clause.contains("fHMS".toLowerCase())
				|| clause.contains("fHoleType".toLowerCase())
				|| clause.contains("fHoleTypeN".toLowerCase())
				|| clause.contains("fHTM_Cover".toLowerCase())
				|| clause.contains("fHTM_Cover_ErrorMessage".toLowerCase())
				|| clause.contains("fHTM_Lookup".toLowerCase())
				|| clause.contains("fHTM_Lookup_ErrorMessage".toLowerCase())
				|| clause.contains("fHTM_To_String".toLowerCase())
				|| clause.contains("fHtmEq".toLowerCase())
				|| clause.contains("fHtmXyz".toLowerCase())
				|| clause.contains("fIAUFromEq".toLowerCase())
				|| clause.contains("fImageMask".toLowerCase())
				|| clause.contains("fImageMaskN".toLowerCase())
				|| clause.contains("fInsideMask".toLowerCase())
				|| clause.contains("fInsideMaskN".toLowerCase())
				|| clause.contains("fIsNumbers".toLowerCase())
				|| clause.contains("fMagToFlux".toLowerCase())
				|| clause.contains("fMagToFluxErr".toLowerCase())
				|| clause.contains("fMaskType".toLowerCase())
				|| clause.contains("fMaskTypeN".toLowerCase())
				|| clause.contains("fMJD".toLowerCase())
				|| clause.contains("fMJDToGMT".toLowerCase())
				|| clause.contains("fMuNuFromEq".toLowerCase())
				|| clause.contains("fObj".toLowerCase())
				|| clause.contains("fObjidFromSDSS".toLowerCase())
				|| clause.contains("fObjType".toLowerCase())
				|| clause.contains("fObjTypeN".toLowerCase())
				|| clause.contains("fPhotoDescription".toLowerCase())
				|| clause.contains("fPhotoFlags".toLowerCase())
				|| clause.contains("fPhotoFlagsN".toLowerCase())
				|| clause.contains("fPhotoMode".toLowerCase())
				|| clause.contains("fPhotoModeN".toLowerCase())
				|| clause.contains("fPhotoStatus".toLowerCase())
				|| clause.contains("fPhotoStatusN".toLowerCase())
				|| clause.contains("fPhotoType".toLowerCase())
				|| clause.contains("fPhotoTypeN".toLowerCase())
				|| clause.contains("fPlate".toLowerCase())
				|| clause.contains("fPrimaryObjID".toLowerCase())
				|| clause.contains("fPrimTarget".toLowerCase())
				|| clause.contains("fPrimTargetN".toLowerCase())
				|| clause.contains("fProgramType".toLowerCase())
				|| clause.contains("fProgramTypeN".toLowerCase())
				|| clause.contains("fPspStatus".toLowerCase())
				|| clause.contains("fPspStatusN".toLowerCase())
				|| clause.contains("fRerun".toLowerCase())
				|| clause.contains("fRotateV3".toLowerCase())
				|| clause.contains("fRun".toLowerCase())
				|| clause.contains("fSDSS".toLowerCase())
				|| clause.contains("fSecTarget".toLowerCase())
				|| clause.contains("fSecTargetN".toLowerCase())
				|| clause.contains("fSkyVersion".toLowerCase())
				|| clause.contains("fSpecClass".toLowerCase())
				|| clause.contains("fSpecClassN".toLowerCase())
				|| clause.contains("fSpecDescription".toLowerCase())
				|| clause.contains("fSpecidFromSDSS".toLowerCase())
				|| clause.contains("fSpecLineNames".toLowerCase())
				|| clause.contains("fSpecLineNamesN".toLowerCase())
				|| clause.contains("fSpecZStatus".toLowerCase())
				|| clause.contains("fSpecZStatusN".toLowerCase())
				|| clause.contains("fSpecZWarning".toLowerCase())
				|| clause.contains("fSpecZWarningN".toLowerCase())
				|| clause.contains("fTiMask".toLowerCase())
				|| clause.contains("fTiMaskN".toLowerCase())
				|| clause.contains("fWedgeV3".toLowerCase())
				|| clause.contains("replacei".toLowerCase())
		) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean compliesToSubtask4(String[] row) {
		int DISTINCT_IPS = 42;
		int COUNT = 43;
		if (Integer.parseInt(row[DISTINCT_IPS]) > 20 && Integer.parseInt(row[COUNT]) < 1000) {
			return false;
		} else {
			return true;
		}
	}

}

