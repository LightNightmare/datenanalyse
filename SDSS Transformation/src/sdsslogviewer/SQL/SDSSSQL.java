/*
 */

package sdsslogviewer.SQL;

import java.util.HashSet;

/**
 * SQL query specified for SDSS query only, a subset of MSSQL.<p/>
 * Nov. 5th, 2010:  Class created for dealing with SDSS specific queries.<p/>
 *
 * Dec. 14th, 2010: lower all case to search.<p/>
 * @author James
 */

@SuppressWarnings("rawtypes")
public class SDSSSQL extends MSSQL {

    /** SDSS unique tokens */
    protected static HashSet SDSStables = new HashSet();
    protected static HashSet SDSSviews  = new HashSet();
    protected static HashSet SDSSfunctions = new HashSet();
    protected static HashSet SDSSprocedures = new HashSet();

    /** Types of SDSSSQL tokens, including table, view, function, and procedure.*/
    public final static int SDSSSQL_TABLE       = 7;
    public final static int SDSSSQL_VIEW        = 8;
    public final static int SDSSSQL_FUNCTION    = 9;
    public final static int SDSSSQL_PROCEDURE   =10;

    /**
     * Create a new SDSS sql query.<p/>
     */
    public SDSSSQL (){
        super();
        loadSDSStokens();
    }

    
    private static String removeHeader (String token){
        try {
            if (token.startsWith("bestdr")){
                token = token.substring(9);
            }
            if (token.startsWith("dbo.")){
                token = token.substring(4);
            }
        } catch (StringIndexOutOfBoundsException siofbe) {
            System.err.println(token);
        }
        return token;
    }

    /**
     * A set of isXXX method to check if a token is one of the types.
     * @param token
     * @return
     */
    public static boolean isSDSStable (String token){
        token = removeHeader(token);
        return SDSStables.contains(token);
    }

    public static boolean isSDSSview (String token){
        token = removeHeader(token);
        return SDSSviews.contains(token);
    }

    public static boolean isSDSSfunction(String token){
        token = removeHeader(token);
        return SDSSfunctions.contains(token);
    }

    public static boolean isSDSSprocedure(String token){
        token = removeHeader(token);
        return SDSSprocedures.contains(token);
    }
    
    /**
     * getType method.return the types of token.<p/>
     */
    public static int getSDSSTokenType(String token){

        if (MSSQL.getMSSQLTokenType(token)!= MSSQL.MSSQL_UNKNOWN){
            return MSSQL.getMSSQLTokenType(token);
        } else {
            if (isSDSStable(token)){
                return SDSSSQL_TABLE;
            } else {
                if (isSDSSview(token)){
                    return SDSSSQL_VIEW;
                } else {
                    if (isSDSSfunction(token)){
                        return SDSSSQL_FUNCTION;
                    } else {
                        if (isSDSSprocedure(token)){
                            return SDSSSQL_PROCEDURE;
                        } else
                            return MSSQL.MSSQL_UNKNOWN;
                    }
                }
            }
        }// end if
    } //end method

    @SuppressWarnings("unchecked")
	private void loadSDSStokens (){

        /** SDSS' unique table names*/
        SDSStables.add("algorithm");			//Algorithm
        SDSStables.add("ap7mag");			//Ap7Mag
        SDSStables.add("besttarget2sector");		//BestTarget2Sector
        SDSStables.add("chunk");			//Chunk
        SDSStables.add("dataconstants");		//DataConstants
        SDSStables.add("dbcolumns");			//DBColumns
        SDSStables.add("dbobjects");			//DBObjects
        SDSStables.add("dbviewcols");			//DBViewCols
        SDSStables.add("dependency");			//Dependency
        SDSStables.add("dr3quasarcatalog");		//DR3QuasarCatalog
        SDSStables.add("dr5quasarcatalog");		//DR5QuasarCatalog
        SDSStables.add("elredshift");			//ELRedShift
        SDSStables.add("field");			//Field
        SDSStables.add("fieldprofile");			//FieldProfile
        SDSStables.add("fieldqa");			//FieldQA
        SDSStables.add("first");			//First
        SDSStables.add("frame");			//Frame
        SDSStables.add("glossary");			//Glossary
        SDSStables.add("halfspace");			//HalfSpace
        SDSStables.add("history");			//History
        SDSStables.add("holeobj");			//HoleObj
        SDSStables.add("inventory");			//Inventory
        SDSStables.add("loadhistory");			//LoadHistory
        SDSStables.add("mask");                         //Mask
        SDSStables.add("maskedobject");			//MaskedObject
        SDSStables.add("match");			//Match
        SDSStables.add("matchhead");			//MatchHead
        SDSStables.add("neighbors");			//Neighbors
        SDSStables.add("objmask");			//ObjMask
        SDSStables.add("origfield");			//OrigField
        SDSStables.add("origphotoobjall");		//OrigPhotoObjAll
        SDSStables.add("photoobjall");			//PhotoObjAll
        SDSStables.add("photoprofile");			//PhotoProfile
        SDSStables.add("phototag");			//PhotoTag
        SDSStables.add("photoz");			//Photoz
        SDSStables.add("photoz2");			//Photoz2
        SDSStables.add("platex");			//PlateX
        SDSStables.add("profiledefs");			//ProfileDefs
        SDSStables.add("propermotions");		//ProperMotions
        SDSStables.add("psobjall");			//PsObjAll
        SDSStables.add("qsobest");			//QsoBest
        SDSStables.add("qsobunch");			//QsoBunch
        SDSStables.add("qsocatalogall");		//QsoCatalogAll
        SDSStables.add("qsoconcordanceall");		//QsoConcordanceAll
        SDSStables.add("qsospec");			//QsoSpec
        SDSStables.add("qsotarget");			//QsoTarget
        SDSStables.add("queryresults");			//QueryResults
        SDSStables.add("rc3");                          //RC3
        SDSStables.add("recentqueries");		//RecentQueries
        SDSStables.add("region");			//Region
        SDSStables.add("region2box");			//Region2Box
        SDSStables.add("regionarcs");			//RegionArcs
        SDSStables.add("regionpatch");			//RegionPatch
        SDSStables.add("rmatrix");			//Rmatrix
        SDSStables.add("rosat");			//Rosat
        SDSStables.add("runqa");			//RunQA
        SDSStables.add("runshift");			//RunShift
        SDSStables.add("sdssconstants");		//SDSSConstants
        SDSStables.add("sector");			//Sector
        SDSStables.add("sector2tile");			//Sector2Tile
        SDSStables.add("segment");			//Segment
        SDSStables.add("siteconstants");		//SiteConstants
        SDSStables.add("sitedbs");			//SiteDBs
        SDSStables.add("sitediagnostics");		//SiteDiagnostics
        SDSStables.add("speclineall");			//SpecLineAll
        SDSStables.add("speclineindex");		//SpecLineIndex
        SDSStables.add("specobjall");			//SpecObjAll
        SDSStables.add("specphotoall");			//SpecPhotoAll
        SDSStables.add("spplines");			//sppLines
        SDSStables.add("sppparams");			//sppParams
        SDSStables.add("stetson");			//Stetson
        SDSStables.add("stripedefs");			//StripeDefs
        SDSStables.add("tabledesc");			//TableDesc
        SDSStables.add("target");			//Target
        SDSStables.add("targetinfo");			//TargetInfo
        SDSStables.add("targetparam");			//TargetParam
        SDSStables.add("targphotoobjall");		//TargPhotoObjAll
        SDSStables.add("targphototag");			//TargPhotoTag
        SDSStables.add("targrunqa");			//TargRunQA
        SDSStables.add("tileall");			//TileAll
        SDSStables.add("tiledtargetall");		//TiledTargetAll
        SDSStables.add("tilinggeometry");		//TilingGeometry
        SDSStables.add("tilinginfo");			//TilingInfo
        SDSStables.add("tilingnote");			//TilingNote
        SDSStables.add("tilingrun");			//TilingRun
        SDSStables.add("uberastro");			//UberAstro
        SDSStables.add("ubercal");			//UberCal
        SDSStables.add("usno");                         //USNO
        SDSStables.add("versions");			//Versions
        SDSStables.add("xcredshift");			//XCRedshift
        SDSStables.add("zone");                         //Zone

        /** SDSS' unique view names*/
        SDSSviews.add("columns");			//Columns
        SDSSviews.add("coordtype");			//CoordType
        SDSSviews.add("fieldmask");			//FieldMask
        SDSSviews.add("fieldquality");			//FieldQuality
        SDSSviews.add("framesstatus");			//FramesStatus
        SDSSviews.add("galaxy");			//Galaxy
        SDSSviews.add("galaxytag");			//GalaxyTag
        SDSSviews.add("holetype");			//HoleType
        SDSSviews.add("imagemask");			//ImageMask
        SDSSviews.add("insidemask");			//InsideMask
        SDSSviews.add("masktype");			//MaskType
        SDSSviews.add("objtype");			//ObjType
        SDSSviews.add("photoaux");			//PhotoAux
        SDSSviews.add("photoauxall");			//PhotoAuxAll
        SDSSviews.add("photofamily");			//PhotoFamily
        SDSSviews.add("photoflags");			//PhotoFlags
        SDSSviews.add("photomode");			//PhotoMode
        SDSSviews.add("photoobj");			//PhotoObj
        SDSSviews.add("photoprimary");			//PhotoPrimary
        SDSSviews.add("photosecondary");		//PhotoSecondary
        SDSSviews.add("photostatus");			//PhotoStatus
        SDSSviews.add("phototype");			//PhotoType
        SDSSviews.add("primtarget");			//PrimTarget
        SDSSviews.add("programtype");			//ProgramType
        SDSSviews.add("pspstatus");			//PspStatus
        SDSSviews.add("qsocatalog");			//QsoCatalog
        SDSSviews.add("qsoconcordance");		//QsoConcordance
        SDSSviews.add("regionconvex");			//RegionConvex
        SDSSviews.add("run");                           //Run
        SDSSviews.add("sectarget");			//SecTarget
        SDSSviews.add("sky");                           //Sky
        SDSSviews.add("spbsparams");			//spbsParams
        SDSSviews.add("specclass");			//SpecClass
        SDSSviews.add("specline");			//SpecLine
        SDSSviews.add("speclinenames");			//SpecLineNames
        SDSSviews.add("specobj");			//SpecObj
        SDSSviews.add("specphoto");			//SpecPhoto
        SDSSviews.add("speczstatus");			//SpecZStatus
        SDSSviews.add("speczwarning");			//SpecZWarning
        SDSSviews.add("star");                          //Star
        SDSSviews.add("startag");			//StarTag
        SDSSviews.add("targphotoobj");			//TargPhotoObj
        SDSSviews.add("targphotoprimary");		//TargPhotoPrimary
        SDSSviews.add("targphotosecondary");		//TargPhotoSecondary
        SDSSviews.add("tile");                          //Tile
        SDSSviews.add("tiledtarget");			//TiledTarget
        SDSSviews.add("tilingboundary");		//TilingBoundary
        SDSSviews.add("tilingmask");			//TilingMask
        SDSSviews.add("timask");			//TiMask
        SDSSviews.add("ubercalibstatus");		//UberCalibStatus
        SDSSviews.add("unknown");			//Unknown

        /** SDSS defined functions*/
        SDSSfunctions.add("fcamcol");			//fCamcol
        SDSSfunctions.add("fcoordsfromeq");		//fCoordsFromEq
        SDSSfunctions.add("fcoordtype");		//fCoordType
        SDSSfunctions.add("fcoordtypen");		//fCoordTypeN
        SDSSfunctions.add("fcosmoabsmag");		//fCosmoAbsMag
        SDSSfunctions.add("fcosmoabsmagfromlumdist");	//fCosmoAbsMagFromLumDist
        SDSSfunctions.add("fcosmoageofuniverse");	//fCosmoAgeOfUniverse
        SDSSfunctions.add("fcosmocomovingvolume");	//fCosmoComovingVolume
        SDSSfunctions.add("fcosmocomovvolumefromdl");	//fCosmoComovVolumeFromDl
        SDSSfunctions.add("fcosmoda");			//fCosmoDa
        SDSSfunctions.add("fcosmodc");			//fCosmoDc
        SDSSfunctions.add("fcosmodistances");		//fCosmoDistances
        SDSSfunctions.add("fcosmodl");			//fCosmoDl
        SDSSfunctions.add("fcosmodm");			//fCosmoDm
        SDSSfunctions.add("fcosmohubbledistance");	//fCosmoHubbleDistance
        SDSSfunctions.add("fcosmolookbacktime");	//fCosmoLookBackTime
        SDSSfunctions.add("fcosmoquantities");		//fCosmoQuantities
        SDSSfunctions.add("fcosmotimeinterval");	//fCosmoTimeInterval
        SDSSfunctions.add("fcosmozfromda");		//fCosmoZfromDa
        SDSSfunctions.add("fcosmozfromdc");		//fCosmoZfromDc
        SDSSfunctions.add("fcosmozfromdl");		//fCosmoZfromDl
        SDSSfunctions.add("fcosmozfromdm");		//fCosmoZfromDm
        SDSSfunctions.add("fdatediffsec");		//fDatediffSec
        SDSSfunctions.add("fdistancearcmineq");		//fDistanceArcMinEq
        SDSSfunctions.add("fdistancearcminxyz");	//fDistanceArcMinXYZ
        SDSSfunctions.add("fdistanceeq");		//fDistanceEq
        SDSSfunctions.add("fdistancexyz");		//fDistanceXyz
        SDSSfunctions.add("fdms");			//fDMS
        SDSSfunctions.add("fdmsbase");			//fDMSbase
        SDSSfunctions.add("fdoccolumns");		//fDocColumns
        SDSSfunctions.add("fdoccolumnswithrank");	//fDocColumnsWithRank
        SDSSfunctions.add("fdocfunctionparams");	//fDocFunctionParams
        SDSSfunctions.add("fenum");			//fEnum
        SDSSfunctions.add("feqfrommunu");		//fEqFromMuNu
        SDSSfunctions.add("fetafromeq");		//fEtaFromEq
        SDSSfunctions.add("fetatonormal");		//fEtaToNormal
        SDSSfunctions.add("ffiber");			//fFiber
        SDSSfunctions.add("ffield");			//fField
        SDSSfunctions.add("ffieldmask");		//fFieldMask
        SDSSfunctions.add("ffieldmaskn");		//fFieldMaskN
        SDSSfunctions.add("ffieldquality");		//fFieldQuality
        SDSSfunctions.add("ffieldqualityn");		//fFieldQualityN
        SDSSfunctions.add("ffirstfieldbit");		//fFirstFieldBit
        SDSSfunctions.add("ffootprinteq");		//fFootprintEq
        SDSSfunctions.add("fframesstatus");		//fFramesStatus
        SDSSfunctions.add("fframesstatusn");		//fFramesStatusN
        SDSSfunctions.add("fgetalpha");			//fGetAlpha
        SDSSfunctions.add("fgetlat");			//fGetLat
        SDSSfunctions.add("fgetlon");			//fGetLon
        SDSSfunctions.add("fgetlonlat");		//fGetLonLat
        SDSSfunctions.add("fgetnearbyframeeq");		//fGetNearbyFrameEq
        SDSSfunctions.add("fgetnearbyobjalleq");	//fGetNearbyObjAllEq
        SDSSfunctions.add("fgetnearbyobjallxyz");	//fGetNearbyObjAllXYZ
        SDSSfunctions.add("fgetnearbyobjeq");		//fGetNearbyObjEq
        SDSSfunctions.add("fgetnearbyobjxyz");		//fGetNearbyObjXYZ
        SDSSfunctions.add("fgetnearbyspecobjalleq");	//fGetNearbySpecObjAllEq
        SDSSfunctions.add("fgetnearbyspecobjallxyz");	//fGetNearbySpecObjAllXYZ
        SDSSfunctions.add("fgetnearbyspecobjeq");	//fGetNearbySpecObjEq
        SDSSfunctions.add("fgetnearbyspecobjxyz");	//fGetNearbySpecObjXYZ
        SDSSfunctions.add("fgetnearestframeeq");	//fGetNearestFrameEq
        SDSSfunctions.add("fgetnearestframeideq");	//fGetNearestFrameidEq
        SDSSfunctions.add("fgetnearestobjalleq");	//fGetNearestObjAllEq
        SDSSfunctions.add("fgetnearestobjeq");		//fGetNearestObjEq
        SDSSfunctions.add("fgetnearestobjidalleq");	//fGetNearestObjIdAllEq
        SDSSfunctions.add("fgetnearestobjideq");	//fGetNearestObjIdEq
        SDSSfunctions.add("fgetnearestobjideqmode");	//fGetNearestObjIdEqMode
        SDSSfunctions.add("fgetnearestobjideqtype");	//fGetNearestObjIdEqType
        SDSSfunctions.add("fgetnearestobjxyz");		//fGetNearestObjXYZ
        SDSSfunctions.add("fgetnearestspecobjalleq");	//fGetNearestSpecObjAllEq
        SDSSfunctions.add("fgetnearestspecobjallxyz");	//fGetNearestSpecObjAllXYZ
        SDSSfunctions.add("fgetnearestspecobjeq");	//fGetNearestSpecObjEq
        SDSSfunctions.add("fgetnearestspecobjid");	//fGetNearestSpecObjID
        SDSSfunctions.add("fgetnearestspecobjidalleq");	//fGetNearestSpecObjIdAllEq
        SDSSfunctions.add("fgetnearestspecobjideq");	//fGetNearestSpecObjIdEq
        SDSSfunctions.add("fgetnearestspecobjideqtype");//fGetNearestSpecObjIdEqType
        SDSSfunctions.add("fgetnearestspecobjxyz");	//fGetNearestSpecObjXYZ
        SDSSfunctions.add("fgetobjectseq");		//fGetObjectsEq
        SDSSfunctions.add("fgetobjectsmaskeq");		//fGetObjectsMaskEq
        SDSSfunctions.add("fgetobjfromrect");		//fGetObjFromRect
        SDSSfunctions.add("fgetobjfromrecteq");		//fGetObjFromRectEq
        SDSSfunctions.add("fgetphotoapmag");		//fGetPhotoApMag
        SDSSfunctions.add("fgeturlatlasimageid");	//fGetUrlAtlasImageId
        SDSSfunctions.add("fgeturlexpeq");		//fGetUrlExpEq
        SDSSfunctions.add("fgeturlexpid");		//fGetUrlExpId
        SDSSfunctions.add("fgeturlfitsatlas");		//fGetUrlFitsAtlas
        SDSSfunctions.add("fgeturlfitsbin");		//fGetUrlFitsBin
        SDSSfunctions.add("fgeturlfitscframe");		//fGetUrlFitsCFrame
        SDSSfunctions.add("fgeturlfitsfield");		//fGetUrlFitsField
        SDSSfunctions.add("fgeturlfitsmask");		//fGetUrlFitsMask
        SDSSfunctions.add("fgeturlfitsspectrum");	//fGetUrlFitsSpectrum
        SDSSfunctions.add("fgeturlframeimg");		//fGetUrlFrameImg
        SDSSfunctions.add("fgeturlnaveq");		//fGetUrlNavEq
        SDSSfunctions.add("fgeturlnavid");		//fGetUrlNavId
        SDSSfunctions.add("fgeturlspecimg");		//fGetUrlSpecImg
        SDSSfunctions.add("fhms");			//fHMS
        SDSSfunctions.add("fhmsbase");			//fHMSbase
        SDSSfunctions.add("fholetype");			//fHoleType
        SDSSfunctions.add("fholetypen");		//fHoleTypeN
        SDSSfunctions.add("fhtmcovercircleeq");		//fHtmCoverCircleEq
        SDSSfunctions.add("fhtmcovercirclexyz");	//fHtmCoverCircleXyz
        SDSSfunctions.add("fhtmcoverregion");		//fHtmCoverRegion
        SDSSfunctions.add("fhtmcoverregionerror");	//fHtmCoverRegionError
        SDSSfunctions.add("fhtmeq");			//fHtmEq
        SDSSfunctions.add("fhtmeqtoxyz");		//fHtmEqToXyz
        SDSSfunctions.add("fhtmgetcenterpoint");	//fHtmGetCenterPoint
        SDSSfunctions.add("fhtmgetcornerpoints");	//fHtmGetCornerPoints
        SDSSfunctions.add("fhtmgetstring");		//fHtmGetString
        SDSSfunctions.add("fhtmversion");		//fHtmVersion
        SDSSfunctions.add("fhtmxyz");			//fHtmXyz
        SDSSfunctions.add("fhtmxyztoeq");		//fHtmXyzToEq
        SDSSfunctions.add("fiaufromeq");		//fIAUFromEq
        SDSSfunctions.add("fimagemask");		//fImageMask
        SDSSfunctions.add("fimagemaskn");		//fImageMaskN
        SDSSfunctions.add("finsidemask");		//fInsideMask
        SDSSfunctions.add("finsidemaskn");		//fInsideMaskN
        SDSSfunctions.add("fisnumbers");		//fIsNumbers
        SDSSfunctions.add("flambdafromeq");		//fLambdaFromEq
        SDSSfunctions.add("fmagtoflux");		//fMagToFlux
        SDSSfunctions.add("fmagtofluxerr");		//fMagToFluxErr
        SDSSfunctions.add("fmasktype");			//fMaskType
        SDSSfunctions.add("fmasktypen");		//fMaskTypeN
        SDSSfunctions.add("fmjd");			//fMJD
        SDSSfunctions.add("fmjdtogmt");			//fMJDToGMT
        SDSSfunctions.add("fmufromeq");			//fMuFromEq
        SDSSfunctions.add("fmunufromeq");		//fMuNuFromEq
        SDSSfunctions.add("fnormalizestring");		//fNormalizeString
        SDSSfunctions.add("fnufromeq");			//fNuFromEq
        SDSSfunctions.add("fobj");			//fObj
        SDSSfunctions.add("fobjid");			//fObjID
        SDSSfunctions.add("fobjidfromsdss");		//fObjidFromSDSS
        SDSSfunctions.add("fobjidfromsdsswithff");	//fObjidFromSDSSWithFF
        SDSSfunctions.add("fobjtype");			//fObjType
        SDSSfunctions.add("fobjtypen");			//fObjTypeN
        SDSSfunctions.add("fphotoapmag");		//fPhotoApMag
        SDSSfunctions.add("fphotoapmagerr");		//fPhotoApMagErr
        SDSSfunctions.add("fphotodescription");		//fPhotoDescription
        SDSSfunctions.add("fphotoflags");		//fPhotoFlags
        SDSSfunctions.add("fphotoflagsn");		//fPhotoFlagsN
        SDSSfunctions.add("fphotomode");		//fPhotoMode
        SDSSfunctions.add("fphotomoden");		//fPhotoModeN
        SDSSfunctions.add("fphotostatus");		//fPhotoStatus
        SDSSfunctions.add("fphotostatusn");		//fPhotoStatusN
        SDSSfunctions.add("fphototype");		//fPhotoType
        SDSSfunctions.add("fphototypen");		//fPhotoTypeN
        SDSSfunctions.add("fplate");			//fPlate
        SDSSfunctions.add("fprimaryobjid");		//fPrimaryObjID
        SDSSfunctions.add("fprimtarget");		//fPrimTarget
        SDSSfunctions.add("fprimtargetn");		//fPrimTargetN
        SDSSfunctions.add("fprogramtype");		//fProgramType
        SDSSfunctions.add("fprogramtypen");		//fProgramTypeN
        SDSSfunctions.add("fpspstatus");		//fPspStatus
        SDSSfunctions.add("fpspstatusn");		//fPspStatusN
        SDSSfunctions.add("fregioncontainspointeq");	//fRegionContainsPointEq
        SDSSfunctions.add("fregioncontainspointxyz");	//fRegionContainsPointXYZ
        SDSSfunctions.add("fregionfuzz");		//fRegionFuzz
        SDSSfunctions.add("fregiongetobjectsfromregionid");	//fRegionGetObjectsFromRegionId
        SDSSfunctions.add("fregiongetobjectsfromstring");	//fRegionGetObjectsFromString
        SDSSfunctions.add("fregionoverlapid");		//fRegionOverlapId
        SDSSfunctions.add("fregionscontainingpointeq");	//fRegionsContainingPointEq
        SDSSfunctions.add("fregionscontainingpointxyz");	//fRegionsContainingPointXYZ
        SDSSfunctions.add("freplace");			//fReplace
        SDSSfunctions.add("frerun");			//fRerun
        SDSSfunctions.add("frotatev3");			//fRotateV3
        SDSSfunctions.add("frun");			//fRun
        SDSSfunctions.add("fsdss");			//fSDSS
        SDSSfunctions.add("fsdssfromobjid");		//fSDSSfromObjID
        SDSSfunctions.add("fsectarget");		//fSecTarget
        SDSSfunctions.add("fsectargetn");		//fSecTargetN
        SDSSfunctions.add("fskyversion");		//fSkyVersion
        SDSSfunctions.add("fspecclass");		//fSpecClass
        SDSSfunctions.add("fspecclassn");		//fSpecClassN
        SDSSfunctions.add("fspecdescription");		//fSpecDescription
        SDSSfunctions.add("fspecidfromsdss");		//fSpecidFromSDSS
        SDSSfunctions.add("fspeclinenames");		//fSpecLineNames
        SDSSfunctions.add("fspeclinenamesn");		//fSpecLineNamesN
        SDSSfunctions.add("fspeczstatus");		//fSpecZStatus
        SDSSfunctions.add("fspeczstatusn");		//fSpecZStatusN
        SDSSfunctions.add("fspeczwarning");		//fSpecZWarning
        SDSSfunctions.add("fspeczwarningn");		//fSpecZWarningN
        SDSSfunctions.add("fsphconvexaddhalfspace");	//fSphConvexAddHalfspace
        SDSSfunctions.add("fsphdiff");			//fSphDiff
        SDSSfunctions.add("fsphdiffadvanced");		//fSphDiffAdvanced
        SDSSfunctions.add("fsphgetarcs");		//fSphGetArcs
        SDSSfunctions.add("fsphgetarea");		//fSphGetArea
        SDSSfunctions.add("fsphgetconvexes");		//fSphGetConvexes
        SDSSfunctions.add("fsphgethalfspaces");		//fSphGetHalfspaces
        SDSSfunctions.add("fsphgethtmranges");		//fSphGetHtmRanges
        SDSSfunctions.add("fsphgethtmrangesadvanced");	//fSphGetHtmRangesAdvanced
        SDSSfunctions.add("fsphgetoutlinearcs");	//fSphGetOutlineArcs
        SDSSfunctions.add("fsphgetpatches");		//fSphGetPatches
        SDSSfunctions.add("fsphgetregionstring");	//fSphGetRegionString
        SDSSfunctions.add("fsphgetregionstringbin");	//fSphGetRegionStringBin
        SDSSfunctions.add("fsphgetversion");		//fSphGetVersion
        SDSSfunctions.add("fsphgrow");			//fSphGrow
        SDSSfunctions.add("fsphgrowadvanced");		//fSphGrowAdvanced
        SDSSfunctions.add("fsphintersect");		//fSphIntersect
        SDSSfunctions.add("fsphintersectadvanced");	//fSphIntersectAdvanced
        SDSSfunctions.add("fsphintersectquery");	//fSphIntersectQuery
        SDSSfunctions.add("fsphregioncontainsxyz");	//fSphRegionContainsXYZ
        SDSSfunctions.add("fsphsimplify");		//fSphSimplify
        SDSSfunctions.add("fsphsimplifyadvanced");	//fSphSimplifyAdvanced
        SDSSfunctions.add("fsphsimplifybinary");	//fSphSimplifyBinary
        SDSSfunctions.add("fsphsimplifybinaryadvanced");	//fSphSimplifyBinaryAdvanced
        SDSSfunctions.add("fsphsimplifyquery");		//fSphSimplifyQuery
        SDSSfunctions.add("fsphsimplifyqueryadvanced");	//fSphSimplifyQueryAdvanced
        SDSSfunctions.add("fsphsimplifystring");	//fSphSimplifyString
        SDSSfunctions.add("fsphsimplifystringadvanced");	//fSphSimplifyStringAdvanced
        SDSSfunctions.add("fsphunion");			//fSphUnion
        SDSSfunctions.add("fsphunionadvanced");		//fSphUnionAdvanced
        SDSSfunctions.add("fsphunionquery");		//fSphUnionQuery
        SDSSfunctions.add("fstripeofrun");		//fStripeOfRun
        SDSSfunctions.add("fstripetonormal");		//fStripeToNormal
        SDSSfunctions.add("fstripofrun");		//fStripOfRun
        SDSSfunctions.add("ftimask");			//fTiMask
        SDSSfunctions.add("ftimaskn");			//fTiMaskN
        SDSSfunctions.add("ftokenadvance");		//fTokenAdvance
        SDSSfunctions.add("ftokennext");		//fTokenNext
        SDSSfunctions.add("ftokenstringtotable");	//fTokenStringToTable
        SDSSfunctions.add("fubercalibstatus");		//fUberCalibStatus
        SDSSfunctions.add("fubercalibstatusn");		//fUberCalibStatusN
        SDSSfunctions.add("fubercalmag");		//fUberCalMag
        SDSSfunctions.add("funnyfunction");		//FunnyFunction
        SDSSfunctions.add("fvarbintohex");		//fVarBinToHex
        SDSSfunctions.add("fwedgev3");			//fWedgeV3

        /** SDSS stored procedures */
        SDSSprocedures.add("spcopydbsimpletable");	//spCopyDbSimpleTable
        SDSSprocedures.add("spcopydbsubset");		//spCopyDbSubset
        SDSSprocedures.add("spdocenum");		//spDocEnum
        SDSSprocedures.add("spdockeysearch");		//spDocKeySearch
        SDSSprocedures.add("spexecutesql");		//spExecuteSQL
        SDSSprocedures.add("spexecutesql2");		//spExecuteSQL2
        SDSSprocedures.add("spgetfiberlist");		//spGetFiberList
        SDSSprocedures.add("spgetmatch");		//spGetMatch
        SDSSprocedures.add("spgetneighbors");		//spGetNeighbors
        SDSSprocedures.add("spgetneighborsall");	//spGetNeighborsAll
        SDSSprocedures.add("spgetneighborsprim");	//spGetNeighborsPrim
        SDSSprocedures.add("spgetneighborsradius");	//spGetNeighborsRadius
        SDSSprocedures.add("spgetspecneighborsall");	//spGetSpecNeighborsAll
        SDSSprocedures.add("spgetspecneighborsprim");	//spGetSpecNeighborsPrim
        SDSSprocedures.add("spgetspecneighborsradius");	//spGetSpecNeighborsRadius
        SDSSprocedures.add("splogsqlperformance");	//spLogSqlPerformance
        SDSSprocedures.add("splogsqlstatement");	//spLogSqlStatement
        SDSSprocedures.add("spnearestobjeq");		//spNearestObjEq
        SDSSprocedures.add("spqsocatalogs");		//spQsoCatalogs
        SDSSprocedures.add("spregionand");		//spRegionAnd
        SDSSprocedures.add("spsetwebserverurl");	//spSetWebServerUrl
        SDSSprocedures.add("spskyservercolumns");	//spSkyServerColumns
        SDSSprocedures.add("spskyserverconstraints");	//spSkyServerConstraints
        SDSSprocedures.add("spskyserverdatabases");	//spSkyServerDatabases
        SDSSprocedures.add("spskyserverformattedquery");	//spSkyServerFormattedQuery
        SDSSprocedures.add("spskyserverfreeformquery");	//spSkyServerFreeFormQuery
        SDSSprocedures.add("spskyserverfunctionparams");	//spSkyServerFunctionParams
        SDSSprocedures.add("spskyserverfunctions");		//spSkyServerFunctions
        SDSSprocedures.add("spskyserverindices");		//spSkyServerIndices
        SDSSprocedures.add("spskyservertables");		//spSkyServerTables

    } //end of load

}

