SQLQueryLogTransformer Version 1.0 2013/11/21

This README is not for usage but for development.
For further hints about usage please refer to the original
thesis this program was developed for, or the README that
is shipped with the executable program.

CONTACT:
------------------------------------
You can contact me any time at florianbecker@gmx-topmail.de


DEVELOPER NOTES:
------------------------------------

The program is structured as follows:

The introduction point is found in
sdssenhancer/SDSSEnhancer.java

	This is where the interface is constructed and the five different methods are called
	(CSVConversion, AccessAreaExtraction, UserSessionCalculation, Geolocation, Generalization).
	
	The class SDSSEnhancer opens the interface and starts a new SDSSHandler
	
	The Interface is constructed in class UI.
	Here you may also find the ActionListener.
	
	The class SDSSHandler defines the several calls of the methods and handles the DB connections and storage:
	- Geolocation is found in geolocation/Geolocator.java
	- UserSession pushes the PL/SQL procedure to the Server and calls it
	- Generalization pushes the PL/SQL procedures and functions to the Server and calls it
	- CSVTransformation is found in transformation/CSVParser.java
	- AccessArea Extraction is found in accessarea/AccessAreaExtraction.java
	
	LimitLinesDocumentListener.java and MessageConsole.java handle the redirect of the console output to the Interface.
	

Package: accessarea
		Entry point is AccessAreaExtraction.java
		This class takes each SQL statement from the SDSSHandler and pushes it through the different steps of Access Area Extraction.
		Additionally here are some String optimizations to catch often occuring errors within SDSS CSV files.
		The steps are:
		- Parsing of the SQL statements using jSQLParser (SQLParser.java)
		- Transforming the statement into first version of access area (SQLParser.java and Converter.java)
		- Prepare the constraints in the new access area for CNF conversion (CNFPreparation.java)
		- Convert to CNF using the AIMA3e CNF Converter
		- Translate back from AIMA3e structure to jSQLParser structure (FinalCNF.java)
		- Handle Self-Joins with SelfJoinHandler.java
		- Consolidate constraints via Consolidation.java
		
		Additionally we define two new object classes:
		- AccessArea, with a list of FromItems and an Expression, representing FROM and WHERE clauses. Including getters and setters.
		- PreparedPredicates, used for CNF preparation, with the original predicates and the new CNF structure. This way we can translate it back after CNF conversion.


Package: geolocation
		Geolocator.java calls the Maxmind's GeoLite API and returns the location details to the SDSSHandler for each IP.
		


Package: transformation
		The CSVparser.java takes the CSV files that come from the SDSSHandler and uses the parts from the SDSS Log viewer to split them into smaller parts.
		Then each part will be parsed and the resulting tuple is sent back to the SDSSHandler where it is stored in the database.
		

A few exemplary SDSS queries can be found in test.java/QueryTest.java


Within the 'net.sf.jsqlparser' Package you can find the adapted java files of the jSQL Parser 0.85
If you wish to update these to a newer version you may find there notes about the changes and some new files.
The edited files and the edited grammar are found within 3rdParty\JSqlParser-master and 3rdParty\JSqlParser-master\src\main\javacc\net\sf\jsqlparser\parser\JSqlParserCC.jj


sdsslogviewer and prefusePlus packages are necessary for CSVConversion by Jian Zhang (alias James Zhang).


For more details, please refer to comments within each file.


SOURCES & ACKNOWLEDGMENTS:
-----------------------------------------

In the following I list the websites, people and sources that helped me and provided pieces of code used within this project:

SDSS Log Viewer by James Zhang (Very much appreciated! He shared SourceCodes with me):
http://cluster.ischool.drexel.edu/~jz85/SDSSLogViewer/SDSSLogViewer.html

jSQLParser (Great project!):
https://github.com/JSQLParser/JSqlParser

AIMA3e-JAVA (used CNF Converter, in general is about AI, very interesting!):
https://code.google.com/p/aima-java/

Java Tips Weblog:
http://tips4java.wordpress.com/2008/11/08/message-console/
http://tips4java.wordpress.com/2008/10/15/limit-lines-in-document/

Maxmind's GeoLite:
http://www.maxmind.com/de/geolocation_landing

And of course this all would never have been without the SDSS and it's SkyServer:
Funding for SDSS-III has been provided by the Alfred P. Sloan Foundation, the Participating Institutions, the National Science Foundation,
and the U.S. Department of Energy Office of Science. The SDSS-III web site is http://www.sdss3.org/.

SDSS-III is managed by the Astrophysical Research Consortium for the Participating Institutions of the SDSS-III Collaboration
including the University of Arizona, the Brazilian Participation Group, Brookhaven National Laboratory, Carnegie Mellon University,
University of Florida, the French Participation Group, the German Participation Group, Harvard University, the Instituto de Astrofisica de Canarias,
the Michigan State/Notre Dame/JINA Participation Group, Johns Hopkins University, Lawrence Berkeley National Laboratory,
Max Planck Institute for Astrophysics, Max Planck Institute for Extraterrestrial Physics, New Mexico State University, New York University,
Ohio State University, Pennsylvania State University, University of Portsmouth, Princeton University, the Spanish Participation Group,
University of Tokyo, University of Utah, Vanderbilt University, University of Virginia, University of Washington, and Yale University. 