package largespace.clustering;

import largespace.business.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;

public class Query {
	public final int id;
    public final List<String> fromTables; // from -> tables{table}
    public final List<List<Predicate>> whereClausesTerms; // where -> clauses -> terms 
    public final long seq;
    public final int[] d = new int[4];
    public boolean hasOrPredicates = false;
    public boolean SomethingWrong = false;
    String  fromPart= "";
    String wherePart = "";
    public Query(int id, String from, String where, long seq) throws ParseException {
        this.id = id;
        this.seq = seq;

        this.fromPart = from;
        this.wherePart = where;
        // parse from part
        String[] tables = Regex.commaRegex.split(from);
        List<String> tablesFiltered = new LinkedList<>();
        for (String table1 : tables) {
            String table = table1.trim().toLowerCase();

            if (table.contains(".")) {
                throw new ParseException("Table name contains dot!", table);
            }

            if (table.contains("(")||table.contains(")")) {
                throw new ParseException("Table name contains parentheses!", table);
            }
            
            if (!table.isEmpty()) {
                tablesFiltered.add(GlobalCaches.strings().deduplicate(table));
            }
        }
        if (tablesFiltered.isEmpty()) {
            throw new ParseException("No tables found!", from);
        }
        Collections.sort(tablesFiltered);
        this.fromTables = GlobalCaches.fromParts().deduplicate(new ArrayList<>(tablesFiltered));

        // parse where part
        String onlyTable = null;
        if (this.fromTables.size() == 1) {
            onlyTable = this.fromTables.get(0);
        }
        String[] clauses = Regex.andRegex.split(where);
        if (clauses.length == 0) {
            throw new ParseException("No clauses!", where);
        }
        List<List<Predicate>> whereParts = new ArrayList<>(clauses.length);
        for (String clause : clauses) {
        	clause= clause.replaceAll("[()]","");
        	String[] terms = Regex.orRegex.split(clause);
            /// corrections
            if (terms.length > 1)
            	this.hasOrPredicates = true;
          /// corrections
            ArrayList<Predicate> predicates = new ArrayList<>(terms.length);

            for (String term : terms) {
                Predicate p = new Predicate(term, onlyTable);

                boolean found = false;
                for (String table : fromTables) {
                    if (table.equals(p.table)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new ParseException("Predicate uses illegal table!", p.toString() + " @ " + fromTables.toString());
                }

                predicates.add(GlobalCaches.predicates().deduplicate(p));
            }

            Collections.sort(predicates);
            whereParts.add(GlobalCaches.clauses().deduplicate(predicates));
        }
        Collections.sort(whereParts, Utils::listCompare);
        this.whereClausesTerms = GlobalCaches.whereParts().deduplicate(whereParts);
    }

    private static <T> double computeDistance(List<T> coll1, List<T> coll2, Comparator<T> comp, ToDoubleBiFunction<T, T> diff) {
        if (coll1.equals(coll2)) {
            return 0;
        } else {
            double difference = 0;
            int items = 0;
            int index1 = 0;
            int index2 = 0;
            while (index1 < coll1.size() && index2 < coll2.size()) {
                T sub1 = coll1.get(index1);
                T sub2 = coll2.get(index2);
                int comparison = comp.compare(sub1, sub2);
                if (comparison == 0) {
                    index1++;
                    index2++;
                    items += 2;
                } else {
                    double lDiff = diff.applyAsDouble(sub1, sub2);
                    if (comparison < 0) {
                        if (index2 > 0) {
                            lDiff = Math.min(lDiff, diff.applyAsDouble(sub1, coll2.get(index2 - 1)));
                        }
                        index1++;
                    } else {
                        if (index1 > 0) {
                            lDiff = Math.min(lDiff, diff.applyAsDouble(coll1.get(index1 - 1), sub2));
                        }
                        index2++;
                    }
                    difference += lDiff;
                    items++;
                }
            }
            for (int i = index1; i < coll1.size(); i++) {
                difference += diff.applyAsDouble(coll1.get(i), coll2.get(index2 - 1));
                items++;
            }
            for (int i = index2; i < coll2.size(); i++) {
                difference += diff.applyAsDouble(coll1.get(index1 - 1), coll2.get(i));
                items++;
            }
            return difference / items;
        }
    }

    public List<Query> region(List<Query> data, boolean[] visited, boolean[] isClusterMember,  Options opt) {
        AtomicInteger[] dTmp = new AtomicInteger[d.length];
        for (int i = 0; i < dTmp.length; ++i) {
            dTmp[i] = new AtomicInteger();
        }
        
        //Added Parallel Stream to filter the queries for which we want to get the Neighborhood
        //We remove visited and already cluster members
        List<Query> filterData = data.parallelStream().map((query) -> {
        	int curIDFilter = query.id;
        	if (!visited[curIDFilter] && !isClusterMember[curIDFilter]) {//Uncomment to avoid comparing to already clustered queries.
        		return query;
        	}
	        else {
	            return null;
	        }
        }).filter((query) -> query != null).collect(Collectors.toCollection(LinkedList::new));
        
        
        List<Query> result = filterData.parallelStream().map((query) -> {
            double dist = computeDistance(query, opt);

            // distance counter
            // Keeps track of the amount of queries in regions of decreasing size (with radius epsilon/2^j)
            for (int j = 0; j < d.length; ++j) {
                if (dist <= opt.EPSILON / Math.pow(2, j)) {
                    dTmp[j].incrementAndGet();
                }
            }

            if (dist <= opt.EPSILON) {
                return query;
            } else {
                return null;
            }
        }).filter((query) -> query != null)
                .collect(Collectors.toCollection(LinkedList::new));

        for (int i = 0; i < dTmp.length; ++i) {
            d[i] = dTmp[i].get();
        }

        return result;
    }

    @Override
    public String toString() {
        return getFromString() + "(" + getWhereString() + ")";
    }

    public String getFromString() {
        return Utils.join(fromTables, ", ");
    }

    public String getWhereString() {
        List<String> clauses = new LinkedList<>();
        for (List<Predicate> clause : whereClausesTerms) {
            clauses.add(Utils.join(clause, " OR "));
        }
        return Utils.join(clauses, " AND ");
    }

    public double computeDistance(Query other, Options opt) {
        assert other != null;
        if ((other == this) || (other.fromPart.equals(this.fromPart) && other.wherePart.equals(this.wherePart))) {
            return 0;
        } else {
            return computeWhereDistance(other, opt);
        }
    }

    public List<String> getCommonTables(List<String> one, List<String> two)
    {
    	List<String> res = new ArrayList<String>();
    	for (String oneStr : one)
    	{
    		if (two.contains(oneStr))
    			res.add(oneStr);
    	}
    	return res;
    }
    
    public Table returnLargestCommonTable(List<String> commonTables, Options opt)
    {
    	Table res = opt.TABLESWITHCOUNT.get(commonTables.get(0));
    	try
    	{
    	if (commonTables != null)
    	{
    	for (String comTable: commonTables)
    	{
    		Table tbl = opt.TABLESWITHCOUNT.get(comTable);
    		if (tbl.Count > res.Count)
    		{
    			res = tbl;
    		}
    	}
    	}
    	}
    	catch (Exception ex)
    	{
    		String str = ex.getMessage();
    		str = str + "!";
    	}
    	
    	

    	
    	return res;
    }
    

    
    private double computeWhereDistance(Query other, Options opt) {
    	/*
    	if (getCommonTables(this.fromTables, other.fromTables).isEmpty())
    		return 1;
    	
    	//deduplicating the names of tables in queries
    	Set<String> queryTables = new HashSet<>();
    	queryTables.addAll(fromTables);
    	queryTables.addAll(other.fromTables);
    	if (queryTables.size() == 0)
    		return 1;
    	
    	long overallTableSize = 0;
    	double finalResult = 0;
    	double currentDistance = 0;
    	double scaleDownFactor = 1;
    	int currentIndex = 0;
    	int indexOfSmallestDistance = -1;
    	double[] distances = new double[queryTables.size()];
    	
    	for (String tableName : queryTables) {
    		Table currentTable = opt.TABLESWITHCOUNT.get(tableName);
    		
    		if (currentTable == null)
    			continue;
    		
    		overallTableSize += currentTable.Count;
    		
    		//calculating the overlaps needed to compute the distance
    		long overlap = QueriesComparision.GetEstimatedRowsOverlap(this, other, currentTable, opt);
        	long overall = QueriesComparision.GetEstimatedRowsOverall(this, other, currentTable, opt);
        	
        	//if there was an error while computing
        	if (overall == -1)
        		return 1;
        	
        	//final distance calculation
        	overall = overall - overlap;
        	currentDistance = 1 - ((double)overlap)/overall;
        	
        	if ((currentDistance != 0) && (currentDistance <= (opt.EPSILON/2))) {
        		double possibleFactor = Math.sqrt(currentDistance);
        		if (possibleFactor < scaleDownFactor) {
        			scaleDownFactor = possibleFactor;
        			indexOfSmallestDistance = currentIndex;
        		}
        	}
        	else if (currentDistance == 0) {
        		scaleDownFactor = Math.sqrt(opt.EPSILON/2);
    			indexOfSmallestDistance = currentIndex;
        	}
        	
    		distances[currentIndex] = currentTable.Count * currentDistance;
    		currentIndex++;
    	}
    	
    	for (int i = 0; i < distances.length; i++) {
    		if (indexOfSmallestDistance != -1 && indexOfSmallestDistance != i)
    			finalResult += ((distances[i] * scaleDownFactor) / overallTableSize);
    		else
    			finalResult += (distances[i] / overallTableSize);
    	}
    	
    	return finalResult;*/
    	
    	//starting code, here just for a check
    	
    	List<String> commonTables = getCommonTables(this.fromTables, other.fromTables);
    	if (commonTables.size() == 0)
    		return 1;
    	
    	Table largestTable = returnLargestCommonTable(commonTables, opt);
    	
    	
    	Long overlap = QueriesComparision.GetEstimatedRowsOverlap(this, other, largestTable, opt);
    	
    	Long overall = QueriesComparision.GetEstimatedRowsOverall(this, other, largestTable, opt);
    	if (overall == -1)
    		return 1;
    	overall = overall - overlap;
    	Double res = 1 - overlap.doubleValue()/overall;
    	if (res <= opt.EPSILON)
    	{
    		//System.out.println("Query1 = " + this.getFromString() + " " + this.getWhereString());
    		//System.out.println("Query2 = " + other.getFromString() + " " + other.getWhereString());
    	}
    	return res;
        
    }
}
