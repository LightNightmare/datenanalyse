package largespace.clustering;

import largespace.business.GlobalCaches;
import largespace.business.Loader;
import largespace.business.Options;

import java.util.LinkedList;
import java.util.List;

import java.awt.Desktop.Action;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DBScan {
    private DBScan() {
    }

    public static void mineClusters(Options opt) throws Exception {
    	
    	if (opt.PREPROCESS)
    	{
    		System.out.println("!!! PREPROCESSING IS TURNED ON");
    		// This has already done, The Preprocess(opt) analyze query log and find pre-clusters based on FROM part of the statements
    		Preprocess(opt);
    	}
    	else
    	{
    		System.out.println("!!! PREPROCESSING IS TURNED OFF");
    		//from file read table names and the count of rows in them
    		Loader.readTables(opt);
    		
    		//from file read columns names and the distribution for the column
    		Loader.readColumns(opt,  opt.FILE_CLMN_OUTPUT);
    		
    		// get the list of input files (sample_{0})
	    		//List<String> files = Loader.fromClustersQueriesFiles(opt);
	    		int i = 0;
	    		//analyze every pre-cluster independantly
	    		//for (String file: files)
    		{
    			{
    			System.out.println("Processing i = " + i);
    			// get the list of queries from the file
    			List<Query> data = Loader.readInputData(opt, opt.FILE_INPUT);
    			System.out.println("Processing " + data.size() + " Queries");
    			GlobalCaches.printStats();
    			List<Query> outliers = new LinkedList<>();
    			// computeClusters
    			List<Cluster> clusters = computeClusters(data, outliers, opt);
    			//write the result to output files (sample_{0}_out)
    			Loader.writeQueries((opt.FILE_C_OUTPUT), clusters);
    			
    			}
    			i++;
    		}
    	}

    }

    public static void Preprocess(Options opt) throws Exception
    {
    	Loader.readTables(opt);
		Loader.readColumns(opt,  opt.FILE_CLMN_OUTPUT);
    	 // load data
    	Map<String, Column> columns = Loader.readColumnsFromInputData(opt);
    	// find FromClusters
    	Loader.Postprocess(opt);
    	Loader.CalculateQueryCountInEachCluster(opt);
    	
    	Loader.writeTables(opt);
    	Loader.writeColumns(opt.FILE_CLMN_OUTPUT, columns, opt); //XXX: THIS WAS COMMENTED OUT
    }
    
    public static List<Cluster> computeClusters(List<Query> data, List<Query> outliers, Options opt) {
        List<Cluster> ret = new LinkedList<>();

        boolean[] visited = new boolean[data.size()]; // == [false] * numPoints
        boolean[] isClusterMember = new boolean[data.size()]; // == [false] * numPoints

        // perform clustering
        int idx = 0;
        for (Query current : data) {
            if (idx++ % 10 == 0) {
                System.out.println("Processing query " + Integer.toString(current.id));
            }

            if (!visited[current.id] && !(isClusterMember[current.id])) {
                visited[current.id] = true;
                List<Query> tmpNeighbors = current.region(data, visited, isClusterMember, opt);

                if (tmpNeighbors.size() < opt.MIN_PTS) {
                    outliers.add(current);
                    isClusterMember[current.id] = true;
                } else {
                    Cluster C = new Cluster();
                    C.expand(current, tmpNeighbors, data, visited, isClusterMember, opt);

                    if (C.points.size() >= opt.MIN_PTS) {
                        ret.add(C);
                    }
                }
            }
        }
        System.out.println(outliers.size() + " Outliers");

        return ret;
    }
}