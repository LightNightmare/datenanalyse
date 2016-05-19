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
								+ "WHERE TRUNC(THETIME) >= TO_DATE('2003-04-01', 'YYYY-MM-DD') "
								//+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";
								+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";
		String SQL_DATA_SLICE_3 = "SELECT * FROM "
				//+ "BDCOURSE.PARSED_STATEMENTS JOIN FROM_WHERE_STATEMENTS ON PARSED_STATEMENTS.stat_id = FROM_WHERE_STATEMENTS.id "
				+ "BDCOURSE.PARSED_STATEMENTS "
				+ "WHERE TRUNC(THETIME) >= TO_DATE('2003-04-01', 'YYYY-MM-DD') "
				//+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";
				+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD') "
				+ "AND can_be_stifle = 0";
		
		String SQL_DATA_SLICE_4 = "SELECT BDCOURSE.PARSED_STATEMENTS.STATEMENT FROM "
				+ "BDCOURSE.PARSED_STATEMENTS ,BDCOURSE.FROM_WHERE_STATEMENTS "
				+ "WHERE TRUNC(THETIME) >= TO_DATE('2003-04-01', 'YYYY-MM-DD') "
				//+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD')";
				+ "AND TRUNC(THETIME) <= TO_DATE('2004-06-30', 'YYYY-MM-DD') "
				+ "AND SEQ <=1407584931 AND SEQ >=18346 "
				+ "AND can_be_stifle = 0 "
				+ "AND PARSED_STATEMENTS.stat_id = FROM_WHERE_STATEMENTS.id "
				+ "AND FROM_WHERE_STATEMENTS.distinct_ips_count >=20"
				+ "AND FROM_WHERE_STATEMENTS.count <=1000";

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
			
			String[] headers = new String[colCount];
			for (int i = 1; i <= colCount; i++) {
				headers[i-1] = rsmd.getColumnName(i);
			}
			buffer.add(headers);
			
			while (result.next()) {
				if (buffer.size() >= bufferThreshold) {
					writeBuffer(path, buffer);
					writtenRows += buffer.size();
					System.out.println(writtenRows + " rows written.");
					buffer = new LinkedList<String[]>();
				}
				
				String[] line = new String[colCount];
				for (int i = 1; i <= colCount; i++) {
					String columnValue = result.getString(i);
					line[i-1] = columnValue;
				}
				buffer.add(line);
			}
			
			writeBuffer(path, buffer);
			writtenRows += buffer.size();
			System.out.println(writtenRows + " rows written.");
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
		if (clause.contains("name description ".trim())
				|| clause.contains("fCamcol Extracts Camcol from an SDSS Photo Object ID  ".trim())
				|| clause.contains("fCoordsFromEq Returns table of stripe, lambda, eta, mu, nu derived from ra,dec  ".trim())
				|| clause.contains("fCoordType Return the CoordType value, indexed by name  ".trim())
				|| clause.contains("fCoordTypeN Return the CoordType name, indexed by value  ".trim())
				|| clause.contains("fDatediffSec fDatediffSec(start,now) returns the difference in seconds as a float  ".trim())
				|| clause.contains("fDistanceArcMinEq returns distance (arc minutes) between two points (ra1,dec1) and (ra2,dec2)  ".trim())
				|| clause.contains("fDistanceArcMinXYZ returns distance (arc minutes) between two points (x1,y1,z1) and (x2,y2,z2)  ".trim())
				|| clause.contains("fDMS Convert declination in degrees to +dd:mm:ss.ss notation  ".trim())
				|| clause.contains("fDocColumns Return the list of Columns in a given table  ".trim())
				|| clause.contains("fDocFunctionParams Return the parameters of a function  ".trim())
				|| clause.contains("fEnum Converts @value in an enum table to a string  ".trim())
				|| clause.contains("fEqFromMuNu Compute Equatorial coordinates from @mu and @nu  ".trim())
				|| clause.contains("fEtaToNormal Compute the normal vector from an eta value  ".trim())
				|| clause.contains("fFiber Extracts Fiber from an SDSS Spec ID  ".trim())
				|| clause.contains("fField Extracts Field from an SDSS Photo Object ID.  ".trim())
				|| clause.contains("fFieldMask Returns mask value for a given name (e.g. 'seeing')  ".trim())
				|| clause.contains("fFieldMaskN Returns description of mask value (e.g. 'SEEING PSF')  ".trim())
				|| clause.contains("fFieldQuality Returns bitmask of named field quality (e.g. ACCEPTABLE, BAD, GOOD, HOLE, MISSING)  ".trim())
				|| clause.contains("fFieldQualityN Returns description of quality value (e.g. ACCEPTABLE, BAD, GOOD, HOLE, MISSING)  ".trim())
				|| clause.contains("fFirstFieldBit Returns the bit that indicates whether an objID is in the first field of a segment  ".trim())
				|| clause.contains("fFramesStatus Returns the FramesStatus value, indexed by name  ".trim())
				|| clause.contains("fFramesStatusN Returns the FramesStatus name, indexed by value  ".trim())
				|| clause.contains("fGetJpegObjects A helper function for SDSS cutout that returns all objects within a certain radius of an (ra,dec)  ".trim())
				|| clause.contains("fGetLonLat Converts a 3-vector to longitude-latitude  ".trim())
				|| clause.contains("fGetNearbyFrameEq Returns table with a record describing the frames neareby (@ra,@dec) at a given @zoom level.  ".trim())
				|| clause.contains("fGetNearbyObjAllXYZ Returns table of photo objects within @r arcmins of an xyz point (@nx,@ny, @nz).  ".trim())
				|| clause.contains("fGetNearbyObjEq Returns table of primary objects within @r arcmins of an Equatorial point (@ra,@dec)  ".trim())
				|| clause.contains("fGetNearbyObjWWT Returns table of objects within @r arcmins of an Equatorial point (@ra,@dec).  ".trim())
				|| clause.contains("fGetNearbyObjXYZ Returns table of primary objects within @r arcmins of an xyz point (@nx,@ny, @nz).  ".trim())
				|| clause.contains("fGetNearestFrameEq Returns table with a record describing the nearest frame to (@ra,@dec) at a given @zoom level.  ".trim())
				|| clause.contains("fGetNearestFrameidEq Returns teh fieldid of the nearest frame to (@ra,@dec) at a given @zoom level.  ".trim())
				|| clause.contains("fGetNearestObjEq Returns table holding a record describing the closest primary object within @r arcminutes of (@ra,@dec).  ".trim())
				|| clause.contains("fGetNearestObjIdEq Returns the objId of nearest photoPrimary within @r arcmins of ra, dec  ".trim())
				|| clause.contains("fGetNearestObjIdEqMode Returns the objId of nearest @mode PhotoObjAll within @r arcmins of ra, dec  ".trim())
				|| clause.contains("fGetNearestObjIdEqType Returns the objID of the nearest photPrimary of type @t within @r arcmin  ".trim())
				|| clause.contains("fGetNearestObjXYZ Returns nearest primary object within @r arcminutes of an xyz point (@nx,@ny, @nz).  ".trim())
				|| clause.contains("fGetObjFromRect Returns table of objects inside a rectangle defined by two ra,dec pairs.  ".trim())
				|| clause.contains("fGetUrlExpEq Returns the URL for an ra,dec, measured in degrees.  ".trim())
				|| clause.contains("fGetUrlExpId Returns the URL for an Photo objID.  ".trim())
				|| clause.contains("fGetUrlFitsAtlas Get the URL to the FITS file of all atlas images in a field  ".trim())
				|| clause.contains("fGetUrlFitsBin Get the URL to the FITS file of a binned frame given FieldID and band.  ".trim())
				|| clause.contains("fGetUrlFitsCFrame Get the URL to the FITS file of a corrected frame given the fieldID and band  ".trim())
				|| clause.contains("fGetUrlFitsField Given a FieldID returns the URL to the FITS file of that field  ".trim())
				|| clause.contains("fGetUrlFitsMask Get the URL to the FITS file of a frame mask given the fieldID and band  ".trim())
				|| clause.contains("fGetUrlFitsSpectrum Get the URL to the FITS file of the spectrum given the SpecObjID  ".trim())
				|| clause.contains("fGetUrlFrameImg Returns the URL for a JPG image of the frame  ".trim())
				|| clause.contains("fGetUrlNavEq Returns the URL for an ra,dec, measured in degrees.  ".trim())
				|| clause.contains("fGetUrlNavId Returns the Navigator URL for an Photo objID.  ".trim())
				|| clause.contains("fGetUrlSpecImg Returns the URL for a GIF image of the spectrum given the SpecObjID  ".trim())
				|| clause.contains("fHMS Convert right ascension in degrees to +hh:mm:ss.ss notation  ".trim())
				|| clause.contains("fHoleType Return the HoleType value, indexed by name  ".trim())
				|| clause.contains("fHoleTypeN Return the HoleType name, indexed by value  ".trim())
				|| clause.contains("fHTM_Cover Returns a table of (HtmIdStart, HtmIdEnd) that covers the area.  ".trim())
				|| clause.contains("fHTM_Cover_ErrorMessage Returns error message for area specification of an fHTM_Cover param  ".trim())
				|| clause.contains("fHTM_Lookup Find the htmID of the specified point on the celestial sphere  ".trim())
				|| clause.contains("fHTM_Lookup_ErrorMessage Returns the error message that fHTM_Lookup generated  ".trim())
				|| clause.contains("fHTM_To_String Translates an HTMID to a string format  ".trim())
				|| clause.contains("fHtmEq Returns 20-deep HTM ID of a given J2000 Equatorial point (@ra,@dec)  ".trim())
				|| clause.contains("fHtmXyz Returns HTM ID of a pint a J2000 xyz point (@x,@y, @z).  ".trim())
				|| clause.contains("fIAUFromEq Convert ra, dec in degrees to extended IAU name  ".trim())
				|| clause.contains("fImageMask Return the ImageMask flag value, indexed by name  ".trim())
				|| clause.contains("fImageMaskN Return the expanded ImageMask corresponding to the flag value as a string  ".trim())
				|| clause.contains("fInsideMask Returns the InsideMask value corresponding to a name  ".trim())
				|| clause.contains("fInsideMaskN Returns the expanded InsideMask corresponding to the bits, as a string  ".trim())
				|| clause.contains("fIsNumbers Check that the substring is a valid number.  ".trim())
				|| clause.contains("fMagToFlux Convert Luptitudes to AB flux in nJy  ".trim())
				|| clause.contains("fMagToFluxErr Convert the error in luptitudes to AB flux in nJy  ".trim())
				|| clause.contains("fMaskType Returns the MaskType value, indexed by name  ".trim())
				|| clause.contains("fMaskTypeN Returns the MaskType name, indexed by value (3-> Galaxy, 6-> Star,...)  ".trim())
				|| clause.contains("fMJD Extracts MJD from an SDSS Spec ID  ".trim())
				|| clause.contains("fMJDToGMT Computes the String of a Modified Julian Date.  ".trim())
				|| clause.contains("fMuNuFromEq Compute stripe coordinates from Equatorial  ".trim())
				|| clause.contains("fObj Extracts Obj from an SDSS Photo Object ID.  ".trim())
				|| clause.contains("fObjidFromSDSS Computes the long objID from the 5-part SDSS numbers.  ".trim())
				|| clause.contains("fObjType Return the ObjType value, indexed by name  ".trim())
				|| clause.contains("fObjTypeN Return the ObjType name, indexed by value  ".trim())
				|| clause.contains("fPhotoDescription Returns a string indicating Object type and object flags  ".trim())
				|| clause.contains("fPhotoFlags Returns the PhotoFlags value corresponding to a name  ".trim())
				|| clause.contains("fPhotoFlagsN Returns the expanded PhotoFlags corresponding to the status value as a string  ".trim())
				|| clause.contains("fPhotoMode Returns the Mode value, indexed by name (Primary, Secondary, Family, Outside)  ".trim())
				|| clause.contains("fPhotoModeN Returns the Mode name, indexed by value ()  ".trim())
				|| clause.contains("fPhotoStatus Returns the PhotoStatus flag value corresponding to a name  ".trim())
				|| clause.contains("fPhotoStatusN Returns the string describing to the status flags in words  ".trim())
				|| clause.contains("fPhotoType Returns the PhotoType value, indexed by name (Galaxy, Star,...)  ".trim())
				|| clause.contains("fPhotoTypeN Returns the PhotoType name, indexed by value (3-> Galaxy, 6-> Star,...)  ".trim())
				|| clause.contains("fPlate Extracts plate from an SDSS Spec ID  ".trim())
				|| clause.contains("fPrimaryObjID Match an objID to a PhotoPrimary and set/unset the first field bit.  ".trim())
				|| clause.contains("fPrimTarget Returns the PrimTarget value corresponding to a name  ".trim())
				|| clause.contains("fPrimTargetN Returns the expanded PrimTarget corresponding to the flag value as a string  ".trim())
				|| clause.contains("fProgramType Return the ProgramType value, indexed by name  ".trim())
				|| clause.contains("fProgramTypeN Return the ProgramType name, indexed by value  ".trim())
				|| clause.contains("fPspStatus Returns the PspStatus value, indexed by name  ".trim())
				|| clause.contains("fPspStatusN Returns the PspStatus name, indexed by value  ".trim())
				|| clause.contains("fRerun Extracts Rerun from an SDSS Photo Object ID  ".trim())
				|| clause.contains("fRotateV3 Rotates a 3-vector by a given rotation matrix  ".trim())
				|| clause.contains("fRun Extracts Run from an SDSS Photo Object ID  ".trim())
				|| clause.contains("fSDSS Computes the 6-part SDSS numbers from the long objID  ".trim())
				|| clause.contains("fSecTarget Returns the SecTarget value corresponding to a name  ".trim())
				|| clause.contains("fSecTargetN Returns the expanded SecTarget corresponding to the flag value as a string  ".trim())
				|| clause.contains("fSkyVersion Extracts SkyVersion from an SDSS Photo Object ID  ".trim())
				|| clause.contains("fSpecClass Returns the SpecClass value, indexed by name  ".trim())
				|| clause.contains("fSpecClassN Returns the SpecClass name, indexed by value  ".trim())
				|| clause.contains("fSpecDescription Returns a string indicating class, status and zWarning for a specObj  ".trim())
				|| clause.contains("fSpecidFromSDSS Computes the long Spec IDs  ".trim())
				|| clause.contains("fSpecLineNames Return the SpecLineNames value, indexed by name  ".trim())
				|| clause.contains("fSpecLineNamesN Return the SpecLineNames name, indexed by value  ".trim())
				|| clause.contains("fSpecZStatus Return the SpecZStatus value, indexed by name  ".trim())
				|| clause.contains("fSpecZStatusN Return the SpecZStatus name, indexed by value  ".trim())
				|| clause.contains("fSpecZWarning Return the SpecZWarning value, indexed by name  ".trim())
				|| clause.contains("fSpecZWarningN Return the expanded SpecZWarning corresponding to the flag value as a string  ".trim())
				|| clause.contains("fTiMask Returns the TiMask value corresponding to a name  ".trim())
				|| clause.contains("fTiMaskN Returns the expanded TiMask corresponding to the flag value as a string  ".trim())
				|| clause.contains("fWedgeV3 Compute the wedge product of two vectors  ".trim())
				|| clause.contains("replacei Case-insensitve string replacement  ".trim())
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
