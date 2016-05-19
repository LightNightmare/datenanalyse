package kit.labcourse.filters;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

public class FilterForSecondTask {

	static TreeMap<String, AtomicLong> userFunctions;
	static BufferedWriter secondTaskData;
	
	public static void filter(BufferedReader firstTaskData) {
		userFunctions = new TreeMap<String, AtomicLong>() {{
			put("fcamcol", new AtomicLong(0));
			put("fcoordsfromeq", new AtomicLong(0));
			put("fcoordtype", new AtomicLong(0)); //*
			put("fcoordtypen", new AtomicLong(0)); //*
			put("fdatediffsec", new AtomicLong(0));
			put("fdistancearcmineq", new AtomicLong(0));
			put("fdistancearcminxyz", new AtomicLong(0));
			put("fdms", new AtomicLong(0));
			put("fdoccolumns", new AtomicLong(0));
			put("fdocfunctionparams", new AtomicLong(0));
			put("fenum", new AtomicLong(0));
			put("feqfrommunu", new AtomicLong(0));
			put("fetatonormal", new AtomicLong(0));
			put("ffiber", new AtomicLong(0));
			put("ffield", new AtomicLong(0)); //*
			put("ffieldmask", new AtomicLong(0)); //*
			put("ffieldmaskn", new AtomicLong(0)); //*
			put("ffieldquality", new AtomicLong(0)); //*
			put("ffieldqualityn", new AtomicLong(0)); //*
			put("ffirstfieldbit", new AtomicLong(0));
			put("fframesstatus", new AtomicLong(0)); //*
			put("fframesstatusn", new AtomicLong(0)); //*
			put("fgetjpegobjects", new AtomicLong(0));
			put("fgetlonlat", new AtomicLong(0));
			put("fgetnearbyframeeq", new AtomicLong(0));
			put("fgetnearbyobjallxyz", new AtomicLong(0));
			put("fgetnearbyobjeq", new AtomicLong(0));
			put("fgetnearbyobjwwt", new AtomicLong(0));
			put("fgetnearbyobjxyz", new AtomicLong(0));
			put("fgetnearestframeeq", new AtomicLong(0));
			put("fgetnearestframeideq", new AtomicLong(0));
			put("fgetnearestobjeq", new AtomicLong(0));
			put("fgetnearestobjideq", new AtomicLong(0)); //*
			put("fgetnearestobjideqmode", new AtomicLong(0)); //*
			put("fgetnearestobjideqtype", new AtomicLong(0)); //*
			put("fgetnearestobjxyz", new AtomicLong(0));
			put("fgetobjfromrect", new AtomicLong(0));
			put("fgeturlexpeq", new AtomicLong(0));
			put("fgeturlexpid", new AtomicLong(0));
			put("fgeturlfitsatlas", new AtomicLong(0));
			put("fgeturlfitsbin", new AtomicLong(0));
			put("fgeturlfitscframe", new AtomicLong(0));
			put("fgeturlfitsfield", new AtomicLong(0));
			put("fgeturlfitsmask", new AtomicLong(0));
			put("fgeturlfitsspectrum", new AtomicLong(0));
			put("fgeturlframeimg", new AtomicLong(0));
			put("fgeturlnaveq", new AtomicLong(0));
			put("fgeturlnavid", new AtomicLong(0));
			put("fgeturlspecimg", new AtomicLong(0));
			put("fhms", new AtomicLong(0));
			put("fholetype", new AtomicLong(0)); //*
			put("fholetypen", new AtomicLong(0)); //*
			put("fhtm_cover", new AtomicLong(0)); //*
			put("fhtm_cover_errormessage", new AtomicLong(0)); //*
			put("fhtm_lookup", new AtomicLong(0)); //*
			put("fhtm_lookup_errormessage", new AtomicLong(0)); //*
			put("fhtm_to_string", new AtomicLong(0));
			put("fhtmeq", new AtomicLong(0));
			put("fhtmxyz", new AtomicLong(0));
			put("fiaufromeq", new AtomicLong(0));
			put("fimagemask", new AtomicLong(0)); //*
			put("fimagemaskn", new AtomicLong(0)); //*
			put("finsidemask", new AtomicLong(0)); //*
			put("finsidemaskn", new AtomicLong(0)); //*
			put("fisnumbers", new AtomicLong(0));
			put("fmagtoflux", new AtomicLong(0)); //*
			put("fmagtofluxerr", new AtomicLong(0)); //*
			put("fmasktype", new AtomicLong(0)); //*
			put("fmasktypen", new AtomicLong(0)); //*
			put("fmjd", new AtomicLong(0)); //*
			put("fmjdtogmt", new AtomicLong(0)); //*
			put("fmunufromeq", new AtomicLong(0));
			put("fobj", new AtomicLong(0)); //*
			put("fobjidfromsdss", new AtomicLong(0)); //*
			put("fobjtype", new AtomicLong(0)); //*
			put("fobjtypen", new AtomicLong(0)); //*
			put("fphotodescription", new AtomicLong(0));
			put("fphotoflags", new AtomicLong(0)); //*
			put("fphotoflagsn", new AtomicLong(0)); //*
			put("fphotomode", new AtomicLong(0)); //*
			put("fphotomoden", new AtomicLong(0)); //*
			put("fphotostatus", new AtomicLong(0)); //*
			put("fphotostatusn", new AtomicLong(0)); //*
			put("fphototype", new AtomicLong(0)); //*
			put("fphototypen", new AtomicLong(0)); //*
			put("fplate", new AtomicLong(0));
			put("fprimaryobjid", new AtomicLong(0));
			put("fprimtarget", new AtomicLong(0)); //*
			put("fprimtargetn", new AtomicLong(0)); //*
			put("fprogramtype", new AtomicLong(0)); //*
			put("fprogramtypen", new AtomicLong(0)); //*
			put("fpspstatus", new AtomicLong(0)); //*
			put("fpspstatusn", new AtomicLong(0)); //*
			put("frerun", new AtomicLong(0));
			put("frotatev3", new AtomicLong(0));
			put("frun", new AtomicLong(0));
			put("fsdss", new AtomicLong(0));
			put("fsectarget", new AtomicLong(0)); //*
			put("fsectargetn", new AtomicLong(0)); //*
			put("fskyversion", new AtomicLong(0));
			put("fspecclass", new AtomicLong(0)); //*
			put("fspecclassn", new AtomicLong(0)); //*
			put("fspecdescription", new AtomicLong(0));
			put("fspecidfromsdss", new AtomicLong(0));
			put("fspeclinenames", new AtomicLong(0)); //*
			put("fspeclinenamesn", new AtomicLong(0)); //*
			put("fspeczstatus", new AtomicLong(0)); //*
			put("fspeczstatusn", new AtomicLong(0)); //*
			put("fspeczwarning", new AtomicLong(0)); //*
			put("fspeczwarningn", new AtomicLong(0)); //*
			put("ftimask", new AtomicLong(0)); //*
			put("ftimaskn", new AtomicLong(0)); //*
			put("fwedgev3", new AtomicLong(0));
			put("replacei", new AtomicLong(0));
		}};
		
		String line = "";
		int counter = 0;
		long counterOfQueries = -1;
		long counterOfQueriesWithFunc = 0;
		
		try {
			secondTaskData = new BufferedWriter(new FileWriter(new File(".\\secondTaskFilteredStatements.csv")));
		} catch (IOException e) {
			System.out.println("The writer for the filtered statements could not be opened!");
			e.printStackTrace();
		}
		try {
			while((line = firstTaskData.readLine()) != null) {
				line = line.toLowerCase();
				if (line.contains("fcamcol")) {
					userFunctions.get("fcamcol").getAndIncrement();
					counter++;
				}
				else if (line.contains("fcoordsfromeq")) {
					userFunctions.get("fcoordsfromeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fcoordtype")) {
					if (line.contains("fcoordtypen"))
						userFunctions.get("fcoordtypen").getAndIncrement();
					else userFunctions.get("fcoordtype").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdatediffsec")) {
					userFunctions.get("fdatediffsec").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdistancearcmineq")) {
					userFunctions.get("fdistancearcmineq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdistancearcminxyz")) {
					userFunctions.get("fdistancearcminxyz").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdms")) {
					userFunctions.get("fdms").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdoccolumns")) {
					userFunctions.get("fdoccolumns").getAndIncrement();
					counter++;
				}
				else if (line.contains("fdocfunctionparams")) {
					userFunctions.get("fdocfunctionparams").getAndIncrement();
					counter++;
				}
				else if (line.contains("fenum")) {
					userFunctions.get("fenum").getAndIncrement();
					counter++;
				}
				else if (line.contains("feqfrommunu")) {
					userFunctions.get("feqfrommunu").getAndIncrement();
					counter++;
				}
				else if (line.contains("fetatonormal")) {
					userFunctions.get("fetatonormal").getAndIncrement();
					counter++;
				}
				else if (line.contains("ffiber")) {
					userFunctions.get("ffiber").getAndIncrement();
					counter++;
				}
				else if (line.contains("ffield")) {
					if (line.contains("ffieldmask")) {
						if (line.contains("ffieldmaskn"))
							userFunctions.get("ffieldmaskn").getAndIncrement();
						else userFunctions.get("ffieldmask").getAndIncrement();
					}
					else if (line.contains("ffieldquality")) {
						if (line.contains("ffieldqualityn"))
							userFunctions.get("ffieldqualityn").getAndIncrement();
						else userFunctions.get("ffieldquality").getAndIncrement();
					}
					else userFunctions.get("ffield").getAndIncrement();
					counter++;
				}
				else if (line.contains("ffirstfieldbit")) {
					userFunctions.get("ffirstfieldbit").getAndIncrement();
					counter++;
				}
				else if (line.contains("fframesstatus")) {
					if (line.contains("fframesstatusn"))
						userFunctions.get("fframesstatusn").getAndIncrement();
					else userFunctions.get("fframesstatus").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetjpegobjects")) {
					userFunctions.get("fgetjpegobjects").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetlonlat")) {
					userFunctions.get("fgetlonlat").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearbyframeeq")) {
					userFunctions.get("fgetnearbyframeeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearbyobjallxyz")) {
					userFunctions.get("fgetnearbyobjallxyz").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearbyobjeq")) {
					userFunctions.get("fgetnearbyobjeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearbyobjwwt")) {
					userFunctions.get("fgetnearbyobjwwt").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearbyobjxyz")) {
					userFunctions.get("fgetnearbyobjxyz").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearestframeeq")) {
					userFunctions.get("fgetnearestframeeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearestframeideq")) {
					userFunctions.get("fgetnearestframeideq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearestobjeq")) {
					userFunctions.get("fgetnearestobjeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearestobjideq")) {
					if (line.contains("fgetnearestobjideqmode"))
						userFunctions.get("fgetnearestobjideqmode").getAndIncrement();
					else if (line.contains("fgetnearestobjideqtype"))
						userFunctions.get("fgetnearestobjideqtype").getAndIncrement();
					else userFunctions.get("fgetnearestobjideq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetnearestobjxyz")) {
					userFunctions.get("fgetnearestobjxyz").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgetobjfromrect")) {
					userFunctions.get("fgetobjfromrect").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlexpeq")) {
					userFunctions.get("fgeturlexpeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlexpid")) {
					userFunctions.get("fgeturlexpid").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitsatlas")) {
					userFunctions.get("fgeturlfitsatlas").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitsbin")) {
					userFunctions.get("fgeturlfitsbin").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitscframe")) {
					userFunctions.get("fgeturlfitscframe").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitsfield")) {
					userFunctions.get("fgeturlfitsfield").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitsmask")) {
					userFunctions.get("fgeturlfitsmask").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlfitsspectrum")) {
					userFunctions.get("fgeturlfitsspectrum").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlframeimg")) {
					userFunctions.get("fgeturlframeimg").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlnaveq")) {
					userFunctions.get("fgeturlnaveq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fgeturlnavid")) {
					userFunctions.get("fgeturlnavid").getAndIncrement();
					counter++;
				}				
				else if (line.contains("fgeturlspecimg")) {
					userFunctions.get("fgeturlspecimg").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhms")) {
					userFunctions.get("fhms").getAndIncrement();
					counter++;
				}
				else if (line.contains("fholetype")) {
					if (line.contains("fholetypen"))
						userFunctions.get("fholetypen").getAndIncrement();
					else userFunctions.get("fholetype").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhtm_cover")) {
					if (line.contains("fhtm_cover_errormessage"))
						userFunctions.get("fhtm_cover_errormessage").getAndIncrement();
					else userFunctions.get("fhtm_cover").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhtm_lookup")) {
					if (line.contains("fhtm_lookup_errormessage"))
						userFunctions.get("fhtm_lookup_errormessage").getAndIncrement();
					else userFunctions.get("fhtm_lookup").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhtm_to_string")) {
					userFunctions.get("fhtm_to_string").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhtmeq")) {
					userFunctions.get("fhtmeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fhtmxyz")) {
					userFunctions.get("fhtmxyz").getAndIncrement();
					counter++;
				}
				else if (line.contains("fiaufromeq")) {
					userFunctions.get("fiaufromeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fimagemask")) {
					if (line.contains("fimagemaskn"))
						userFunctions.get("fimagemaskn").getAndIncrement();
					else userFunctions.get("fimagemask").getAndIncrement();
					counter++;
				}
				else if (line.contains("finsidemask")) {
					if (line.contains("finsidemaskn"))
						userFunctions.get("finsidemaskn").getAndIncrement();
					else userFunctions.get("finsidemask").getAndIncrement();
					counter++;
				}
				else if (line.contains("fisnumbers")) {
					userFunctions.get("fisnumbers").getAndIncrement();
					counter++;
				}
				else if (line.contains("fmagtoflux")) {
					if (line.contains("fmagtofluxerr"))
						userFunctions.get("fmagtofluxerr").getAndIncrement();
					else userFunctions.get("fmagtoflux").getAndIncrement();
					counter++;
				}
				else if (line.contains("fmasktype")) {
					if (line.contains("fmasktypen"))
						userFunctions.get("fmasktypen").getAndIncrement();
					else userFunctions.get("fmasktype").getAndIncrement();
					counter++;
				}
				else if (line.contains("fmjd")) {
					if (line.contains("fmjdtogmt"))
						userFunctions.get("fmjdtogmt").getAndIncrement();
					else userFunctions.get("fmjd").getAndIncrement();
					counter++;
				}
				else if (line.contains("fmunufromeq")) {
					userFunctions.get("fmunufromeq").getAndIncrement();
					counter++;
				}
				else if (line.contains("fobj")) {
					if (line.contains("fobjidfromsdss"))
						userFunctions.get("fobjidfromsdss").getAndIncrement();
					else if (line.contains("fobjtype")) {
						if (line.contains("fobjtypen"))
							userFunctions.get("fobjtypen").getAndIncrement();
						else userFunctions.get("fobjtype").getAndIncrement();
					}
					else userFunctions.get("fobj").getAndIncrement();
					counter++;
				}				
				else if (line.contains("fphotodescription")) {
					userFunctions.get("fphotodescription").getAndIncrement();
					counter++;
				}
				else if (line.contains("fphotoflags")) {
					if (line.contains("fphotoflagsn"))
						userFunctions.get("fphotoflagsn").getAndIncrement();
					else userFunctions.get("fphotoflags").getAndIncrement();
					counter++;
				}
				else if (line.contains("fphotomode")) {
					if (line.contains("fphotomoden"))
						userFunctions.get("fphotomoden").getAndIncrement();
					else userFunctions.get("fphotomode").getAndIncrement();
					counter++;
				}
				else if (line.contains("fphotostatus")) {
					if (line.contains("fphotostatusn"))
						userFunctions.get("fphotostatusn").getAndIncrement();
					else userFunctions.get("fphotostatus").getAndIncrement();
					counter++;
				}
				else if (line.contains("fphototype")) {
					if (line.contains("fphototypen"))
						userFunctions.get("fphototypen").getAndIncrement();
					else userFunctions.get("fphototype").getAndIncrement();
					counter++;
				}
				else if (line.contains("fplate")) {
					userFunctions.get("fplate").getAndIncrement();
					counter++;
				}
				else if (line.contains("fprimaryobjid")) {
					userFunctions.get("fprimaryobjid").getAndIncrement();
					counter++;
				}
				else if (line.contains("fprimtarget")) {
					if (line.contains("fprimtargetn"))
						userFunctions.get("fprimtargetn").getAndIncrement();
					else userFunctions.get("fprimtarget").getAndIncrement();
					counter++;
				}
				else if (line.contains("fprogramtype")) {
					if (line.contains("fprogramtypen"))
						userFunctions.get("fprogramtypen").getAndIncrement();
					else userFunctions.get("fprogramtype").getAndIncrement();
					counter++;
				}
				else if (line.contains("fpspstatus")) {
					if (line.contains("fpspstatusn"))
						userFunctions.get("fpspstatusn").getAndIncrement();
					else userFunctions.get("fpspstatus").getAndIncrement();
					counter++;
				}
				else if (line.contains("frerun")) {
					userFunctions.get("frerun").getAndIncrement();
					counter++;
				}
				else if (line.contains("frotatev3")) {
					userFunctions.get("frotatev3").getAndIncrement();
					counter++;
				}
				else if (line.contains("frun")) {
					userFunctions.get("frun").getAndIncrement();
					counter++;
				}
				else if (line.contains("fsdss")) {
					userFunctions.get("fsdss").getAndIncrement();
					counter++;
				}
				else if (line.contains("fsectarget")) {
					if (line.contains("fsectargetn"))
						userFunctions.get("fsectargetn").getAndIncrement();
					else userFunctions.get("fsectarget").getAndIncrement();
					counter++;
				}
				else if (line.contains("fskyversion")) {
					userFunctions.get("fskyversion").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspecclass")) {
					if (line.contains("fspecclassn"))
						userFunctions.get("fspecclassn").getAndIncrement();
					else userFunctions.get("fspecclass").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspecdescription")) {
					userFunctions.get("fspecdescription").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspecidfromsdss")) {
					userFunctions.get("fspecidfromsdss").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspeclinenames")) {
					if (line.contains("fspeclinenamesn"))
						userFunctions.get("fspeclinenamesn").getAndIncrement();
					else userFunctions.get("fspeclinenames").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspeczstatus")) {
					if (line.contains("fspeczstatusn"))
						userFunctions.get("fspeczstatusn").getAndIncrement();
					else userFunctions.get("fspeczstatus").getAndIncrement();
					counter++;
				}
				else if (line.contains("fspeczwarning")) {
					if (line.contains("fspeczwarningn"))
						userFunctions.get("fspeczwarningn").getAndIncrement();
					else userFunctions.get("fspeczwarning").getAndIncrement();
					counter++;
				}
				else if (line.contains("ftimask")) {
					if (line.contains("ftimaskn"))
						userFunctions.get("ftimaskn").getAndIncrement();
					else userFunctions.get("ftimask").getAndIncrement();
					counter++;
				}
				else if (line.contains("fwedgev3")) {
					userFunctions.get("fwedgev3").getAndIncrement();
					counter++;
				}
				else if (line.contains("replacei")) {
					userFunctions.get("replacei").getAndIncrement();
					counter++;
				}

				if (counter == 0) {
					secondTaskData.write(line);
					secondTaskData.write("\n");
					counterOfQueries++;
				}
				else {
					counterOfQueriesWithFunc++;
				}
				
				counter = 0;

			}
		}
		catch (IOException exc) {
			System.out.println("Problem with reader, the file could not be read!");
			exc.printStackTrace();
		}
		
		try {
			firstTaskData.close();
			secondTaskData.close();
		} catch (IOException e) {
			System.out.println("Either the reader or the writer could not be closed!");
			e.printStackTrace();
		}
		
	//	try {
	//		secondTaskData = new BufferedWriter(new FileWriter(new File(".\\secondTaskStatistics.txt")));
			
	//		userFunctions.forEach(
	//				(key, value) -> {try {
	//					secondTaskData.write(key + " : " + value + "\n");
	//					secondTaskData.write("woo");
	//				} catch (Exception e) {
	//					System.out.println("There was a problem with the population of the statistics file!");;
	//					e.printStackTrace();
	//				}});
			userFunctions.forEach(
					(key, value) -> {try {
						System.out.println(key + " : " + value);
					} catch (Exception e) {
						System.out.println("There was a problem with the population of the statistics file!");;
						e.printStackTrace();
					}});
			System.out.println("Number of filtered statements: " + counterOfQueries);
			System.out.println("Number of original statements: " + (counterOfQueries + counterOfQueriesWithFunc));
	//	} catch (IOException e) {
	//		System.out.println("There was a problem with the statistics file!");
	//		e.printStackTrace();
	//	}
	}
	
}