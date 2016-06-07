package largespace.clustering;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.SortedMap;

import largespace.business.Operator;
import largespace.business.Options;
import largespace.business.Table;
import largespace.clustering.Column.GlobalColumnType;
import largespace.clustering.Interval;

public final class QueriesComparision {

	
	
	private static Interval GetTntervalForEq(Object val, Column column, Query q, Options opt, Table t)
	{
		Interval interval = new Interval();
		
		if (val.toString().contains("."))
		{
			Boolean isNeedToCalculateInterval = IsNeedToCalculateInterval(val, opt, t);
			if (!isNeedToCalculateInterval)
				return null;
			// this is JOIN, we can't measure interval, only estimated intervals row count
			interval.HasOnlyIntervalsEstimatedRowCount = true;
			Long estTCount = GetEstimetedRowCountInParentTable(column, t);
			interval.EstimatedRowCount = GetEstimetedRowCountInJoin(val, column, q, opt, estTCount);
		}
		interval.MinVal = val;
		interval.MaxVal = val;
		interval.StricktMinBorder = true;
		interval.StricktMaxBorder = true;

		return interval;
	}
	
	public static Long GetEstimetedRowCountInParentTable(Column column, Table t)
	{
		Long res = t.Count;
		if (column.GlobalColumnType == GlobalColumnType.DistributedFieldWithEmissions)
		{
			for (ValueState vs : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
			{
				res = vs.ValuesCount;
			}
		}
		return res;
	}
	public static Boolean IsNeedToCalculateInterval(Object val, Options opt, Table t)
	{
		Boolean res = false;
		String[] vals = val.toString().split("\\.");
		String tableName = vals[0];
		String columnName = vals[1];
		Table t1 = opt.TABLESWITHCOUNT.get(tableName);
		if (t1 == null)
			return false;
		if (t.Count > t1.Count)
			res = true;
		return res;
	}
	
	public static long GetEstimetedRowCountInJoin(Object val, Column column, Query q, Options opt, Long estTCount)
	{
		Long res = new Long(0);
		String[] vals = val.toString().split("\\.");
		String tableName = vals[0];
		String columnName = vals[1];
		Table t = opt.TABLESWITHCOUNT.get(tableName);
		res = t.Count;
		Column c = opt.COLUMNS_DISTRIBUTION.get(tableName + "." + columnName);
		if (c == null)
		{
			//System.out.println("column = " + columnName + "is null");
			return 0;
		}
		else
		{
			Map<String, List<Interval>> mapColIntervals1 = GetIntervalForQureryAndColumn(q, t, opt);
			if (mapColIntervals1 == null)
	    		return -1;
	    	Set<String> columnsInQuery1_ =  mapColIntervals1.keySet();
	    	
	    	ArrayList<String> columnsInQuery1 = new ArrayList<String>();
	    	columnsInQuery1.addAll(columnsInQuery1_);
	    	
	    	Map<String, List<Interval>> mapColIntervalsAll = new TreeMap<String, List<Interval>>();
	    	for (String columnstr : columnsInQuery1)
	    	{
	    		List<Interval> intervals = mapColIntervals1.get(columnstr);
	    		Column c1 = opt.COLUMNS_DISTRIBUTION.get(columnstr);
	    		Long tmp = GetEstimatedRowsCount(intervals, c1, t);
	    		if (tmp < res)
	    			res = tmp;	
	    	}
	    	
		}
		Double k = ((Long)t.Count).doubleValue() / estTCount;
		return Math.round(res * k);
	}
	
	private static Interval GetTntervalForLe(Object val, Column column)
	{
		Interval interval = new Interval();
		GlobalColumnType columnType = column.GlobalColumnType;
		switch (columnType)
		{
			case DictionaryField:
			{
				interval.MinVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[0]).Value;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedField:
			{
				interval.MinVal = ((DistributedField)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedFieldWithEmissions:
			{
				interval.MinVal = ((DistributedFieldWithEmissions)column.Distribution).MinValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval.MinVal > emissionVal)
						interval.MinVal = emissionVal;
				}
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case Identificator:
			{
				interval.MinVal = ((Identificator)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
		}
		return interval;
	}
	private static Interval GetTntervalForLt(Object val, Column column)
	{
		Interval interval = new Interval();
		GlobalColumnType columnType = column.GlobalColumnType;
		switch (columnType)
		{
			case DictionaryField:
			{
				interval.MinVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[0]).Value;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
			}
			break;
			case DistributedField:
			{
				interval.MinVal = ((DistributedField)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
			}
			break;
			case DistributedFieldWithEmissions:
			{
				interval.MinVal = ((DistributedFieldWithEmissions)column.Distribution).MinValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval.MinVal > emissionVal)
						interval.MinVal = emissionVal;
				}
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
			}
			break;
			case Identificator:
			{
				interval.MinVal = ((Identificator)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
			}
			break;
		}
		return interval;
	}
	
	private static Interval GetTntervalForGt(Object val, Column column)
	{
		Interval interval = new Interval();
		GlobalColumnType columnType = column.GlobalColumnType;
		switch (columnType)
		{
			case DictionaryField:
			{
				int valuesCount = ((DictionaryField)column.Distribution).Values.values().size();
				interval.MaxVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[valuesCount-1]).Value;
				interval.MinVal = val;
				interval.StricktMinBorder = false;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedField:
			{
				interval.MaxVal = ((DistributedField)column.Distribution).MaxValue;
				interval.MinVal = val;
				interval.StricktMinBorder = false;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedFieldWithEmissions:
			{
				interval.MaxVal = ((DistributedFieldWithEmissions)column.Distribution).MaxValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval.MaxVal < emissionVal)
						interval.MaxVal = emissionVal;
				}
				interval.MinVal = val;
				interval.StricktMinBorder = false;
				interval.StricktMaxBorder = true;
			}
			break;
			case Identificator:
			{
				interval.MaxVal = ((Identificator)column.Distribution).MaxValue;
				interval.MinVal = val;
				interval.StricktMinBorder = false;
				interval.StricktMaxBorder = true;
			}
			break;
		}
		return interval;
	}
	
	private static Interval GetTntervalForGe(Object val, Column column)
	{
		Interval interval = new Interval();
		GlobalColumnType columnType = column.GlobalColumnType;
		switch (columnType)
		{
			case DictionaryField:
			{
				int valuesCount = ((DictionaryField)column.Distribution).Values.values().size();
				interval.MaxVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[valuesCount-1]).Value;
				interval.MinVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedField:
			{
				interval.MaxVal = ((DistributedField)column.Distribution).MaxValue;
				interval.MinVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case DistributedFieldWithEmissions:
			{
				interval.MaxVal = ((DistributedFieldWithEmissions)column.Distribution).MaxValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval.MaxVal < emissionVal)
						interval.MaxVal = emissionVal;
				}
				interval.MinVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
			case Identificator:
			{
				interval.MaxVal = ((Identificator)column.Distribution).MaxValue;
				interval.MinVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = true;
			}
			break;
		}
		return interval;
	}
	
	private static List<Interval> GetTntervalForNe(Object val, Column column)
	{
		List<Interval> intervals = new ArrayList<Interval>();

		GlobalColumnType columnType = column.GlobalColumnType;
		Interval interval = new Interval();
		Interval interval2 = new Interval();
		switch (columnType)
		{
			case DictionaryField:
			{
				int valuesCount = ((DictionaryField)column.Distribution).Values.values().size();
				interval.MinVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[0]).Value;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
				
				interval2.MinVal = val;
				interval2.MaxVal = ((ValueState)((DictionaryField)column.Distribution).Values.values().toArray()[valuesCount - 1]).Value;
				interval2.StricktMinBorder = false;
				interval2.StricktMaxBorder = true;
			}
			break;
			case DistributedField:
			{
				interval.MinVal = ((DistributedField)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
				
				interval2.MinVal = val;
				interval2.MaxVal = ((DistributedField)column.Distribution).MaxValue;
				interval2.StricktMinBorder = false;
				interval2.StricktMaxBorder = true;
			}
			break;
			case DistributedFieldWithEmissions:
			{
				interval.MinVal = ((DistributedFieldWithEmissions)column.Distribution).MinValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval.MinVal > emissionVal)
						interval.MinVal = emissionVal;
				}
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
				
				interval2.MaxVal = ((DistributedFieldWithEmissions)column.Distribution).MaxValue;
				for(ValueState emission : ((DistributedFieldWithEmissions)column.Distribution).Values.values())
				{
					Double emissionVal = Double.parseDouble(emission.Value.toString());
					if ((Double)interval2.MaxVal < emissionVal)
						interval2.MaxVal = emission.Value;
				}
				interval2.MinVal = val;
				interval2.StricktMinBorder = false;
				interval2.StricktMaxBorder = true;
			}
			break;
			case Identificator:
			{
				interval.MinVal = ((Identificator)column.Distribution).MinValue;
				interval.MaxVal = val;
				interval.StricktMinBorder = true;
				interval.StricktMaxBorder = false;
				
				interval2.MinVal = val;
				interval2.MaxVal = ((Identificator)column.Distribution).MaxValue;
				interval2.StricktMinBorder = false;
				interval2.StricktMaxBorder = true;
			}
			break;
		}
		intervals.add(interval);
		intervals.add(interval2);
		return intervals;
	}
	
	private static List<Interval> GetIntervalForPredicate(Operator op, Object val, Column column, Query q, Options opt, Table t)
	{
		List<Interval> intervals = new ArrayList<Interval>();
		
		switch (op) 
    	{
	        case EQ :  
	        {

	        	Interval interval = GetTntervalForEq(val, column, q, opt, t);
	        	if (interval != null)
	        		intervals.add(interval);
	        }
	       		break;
	        case LT :  
	        {
	        	Interval interval = GetTntervalForLt(val, column);
	        	intervals.add(interval);
	        }
	       		break;
	        case GT :  
	        {
	        	Interval interval = GetTntervalForGt(val, column);
	        	intervals.add(interval);
	        }
	       		break;
	        case LE :  
	        {
	        	Interval interval = GetTntervalForLe(val, column);
	        	intervals.add(interval);
	        }
	       		break;
	        case GE :  
	        {
	        	Interval interval = GetTntervalForGe(val, column);
	        	intervals.add(interval);
	        }
	       		break;
	        case NE :  
	        {
	        	intervals = GetTntervalForNe(val, column);
	        }
	       		break;
    	}
		return intervals;
	}
	
	private static List<Interval> MergeIntervalsByDisjunctionForDictionaryField(List<Interval> originalIntervals, Column column)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<String, Point> points = new TreeMap<String, Point>();
		for (Interval interval : originalIntervals)
		{
			if (!interval.HasOnlyIntervalsEstimatedRowCount)
			{
				try
				{
				Point p = new Point();
				p.Value = interval.MinVal;
				p.IsMinBorder = true;
				p.IsStrict = interval.StricktMinBorder;
				points.put((String)interval.MinVal, p);
				
				Point p2 = new Point();
				p2.Value = (String)interval.MaxVal;
				p2.IsMinBorder = false;
				p2.IsStrict = interval.StricktMaxBorder;
				points.put((String)interval.MaxVal, p2);
				}
				catch (Exception e)
				{
					String exstr =  e.toString();
				}
			}
			else
				resIntervals.add(interval);
		}
		
		
		List<Interval> r  = (GetFinalIntervalsForDisjunction(points));
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByDisjunctionForDistributedField(List<Interval> originalIntervals, Column column)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<Double, Point> points = new TreeMap<Double, Point>();
		for (Interval interval : originalIntervals)
		{
			if (!interval.HasOnlyIntervalsEstimatedRowCount)
			{
				try
				{
				Point p = new Point();
				Double val = Double.parseDouble(interval.MinVal.toString().replace("'", ""));
				p.Value = val;
				p.IsMinBorder = true;
				p.IsStrict = interval.StricktMinBorder;
				points.put(val, p);
				
				Point p2 = new Point();
				val = Double.parseDouble(interval.MaxVal.toString().replace("'", ""));
				p2.Value = val;
				p2.IsMinBorder = false;
				p2.IsStrict = interval.StricktMaxBorder;
				points.put(val, p2);
				}
				catch (Exception ex)
				{
					/////System.out.println("column.Name = " + column.Name + "; ex = " + ex);
					return null;
				}
			}
			else
				resIntervals.add(interval);
		}
		
		
		List<Interval> r  = (GetFinalIntervalsForDisjunction(points));
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByDisjunctionForIdentificator(List<Interval> originalIntervals, Column column)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<Long, Point> points = new TreeMap<Long, Point>();
		for (Interval interval : originalIntervals)
		{
			if (!interval.HasOnlyIntervalsEstimatedRowCount)
			{
				Point p = new Point();
				
				
				Long val = new Long(0);
				if (interval.MinVal.toString().contains("x"))
					val = Long.parseLong(interval.MinVal.toString().replace("0x", "").replace("x", ""), 16);
				else
				{
					try
					{
					val = Long.parseLong(interval.MinVal.toString());
					}
					catch (Exception ex)
					{
						/////System.out.println("column.Name = " + column.Name + "; ex = " + ex);
						return null;
					}
				}
				p.Value = val;
				p.IsMinBorder = true;
				p.IsStrict = interval.StricktMinBorder;
				points.put(val, p);
				
				Point p2 = new Point();
				if (interval.MaxVal.toString().contains("x"))
					val = Long.parseLong(interval.MaxVal.toString().replace("0x", "").replace("x", ""), 16);
				else
				{
					try
					{
					val = Long.parseLong(interval.MaxVal.toString());
					}
					catch (Exception ex)
					{
						//System.out.println("column.Name = " + column.Name + "; ex = " + ex);
						return null;
					}
				}
				p2.Value = val;
				p2.IsMinBorder = false;
				p2.IsStrict = interval.StricktMaxBorder;
				points.put(val, p2);
			}
			else
				resIntervals.add(interval);
		}
		
		
		List<Interval> r  = (GetFinalIntervalsForDisjunction(points));
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	private static <T> List<Interval> GetFinalIntervalsForDisjunction(SortedMap<T, Point> points)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		Boolean isStart = true;
		Interval intl = new Interval();
		Integer iInt = 0;
		// now we have sorted points
		for (Point p : points.values())
		{
			if (isStart)
			{
				intl = new Interval();
				intl.MinVal = p.Value;
				intl.StricktMinBorder = p.IsStrict;
				iInt++;
			}
			isStart = false;
			
			if (p.IsMinBorder)
				iInt++;
			else
				iInt--;
			
			if (iInt == 0)
			{
				intl.MaxVal = p.Value;
				intl.StricktMaxBorder = p.IsStrict;
				
				Interval intl2 = new Interval();
				intl2.MaxVal = intl.MaxVal;
				intl2.MinVal = intl.MinVal;
				intl2.StricktMaxBorder = intl.StricktMaxBorder;
				intl2.StricktMinBorder = intl.StricktMinBorder;
				resIntervals.add(intl2);
				isStart = true;
			}
				
		}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByDisjunction(List<Interval> originalIntervals, Column column)
	{
		Object points = null;
		List<Interval> resIntervals = new ArrayList<Interval>();
		points = new TreeMap<String, Point>();
		switch (column.GlobalColumnType)
    	{
    		case DictionaryField:
    		{
    			resIntervals = MergeIntervalsByDisjunctionForDictionaryField(originalIntervals, column);
    		}
    		break;
    		case DistributedField:
    		{
    			resIntervals = MergeIntervalsByDisjunctionForDistributedField(originalIntervals, column);
    		}
    		break;
    		case DistributedFieldWithEmissions:
    		{
    			resIntervals = MergeIntervalsByDisjunctionForDistributedField(originalIntervals, column);
    		}
    		break;
    		case Identificator:
    		{
    			resIntervals = MergeIntervalsByDisjunctionForIdentificator(originalIntervals, column);
    		}
    		break;
    	}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByConjunction(List<List<Interval>> originalIntervals, Column column)
	{
		Object points = null;
		List<Interval> resIntervals = new ArrayList<Interval>();
		points = new TreeMap<String, Point>();
		switch (column.GlobalColumnType)
    	{
    		case DictionaryField:
    		{
    			resIntervals = MergeIntervalsByConjunctionForDictionaryField(originalIntervals, column);
    		}
    		break;
    		case DistributedField:
    		{
    			resIntervals = MergeIntervalsByConjunctionForDistributedField(originalIntervals, column);
    		}
    		break;
    		case DistributedFieldWithEmissions:
    		{
    			resIntervals = MergeIntervalsByConjunctionForDistributedField(originalIntervals, column);
    		}
    		break;
    		case Identificator:
    		{
    			resIntervals = MergeIntervalsByConjunctionForIdentificator(originalIntervals, column);
    		}
    		break;
    	}
		return resIntervals;
	}
	
	private static <T> List<Interval> GetFinalIntervalsForConjunction(List<List<Interval>> originalIntervals, SortedMap<T, List<Point>> points)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		Interval intl = new Interval();
		int needHasCount = originalIntervals.size();
		List<String> has = new ArrayList<String>();
		// now we have sorted points
		for (List<Point> point: points.values())
		{
			for (Point p : point)
			{
				
				if (p.IsMinBorder)
				{
					has.add(p.Index.toString());
					if (intl.MaxVal != null)
					{
					if (intl.MaxVal.equals(p.Value))
						has.add(intl.Index);
					}
					intl = new Interval();
					intl.MinVal = p.Value;
					intl.Index = p.Index.toString();
					intl.StricktMinBorder = p.IsStrict;
				}
				else
				{
					intl.MaxVal = p.Value;
					intl.StricktMaxBorder = p.IsStrict;
					if (has.size() == needHasCount)
					{
						
						Interval intl2 = new Interval();
						intl2.MaxVal = intl.MaxVal;
						intl2.MinVal = intl.MinVal;
						intl2.StricktMaxBorder = intl.StricktMaxBorder;
						intl2.StricktMinBorder = intl.StricktMinBorder;
						resIntervals.add(intl2);
						
						
					}
					has.remove(p.Index.toString());
				}	
			}
		}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByConjunctionForIdentificator(List<List<Interval>>  originalIntervals, Column column)
	{	
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<Long, List<Point>> points = new TreeMap<Long, List<Point>>();
		
		int i = 0;
		for (List<Interval> listIntervals : originalIntervals)
		{
			for (Interval interval: listIntervals)
			{
				if (!interval.HasOnlyIntervalsEstimatedRowCount)
				{
					Point p = new Point();
					p.Value = (Long)interval.MinVal;
					p.IsMinBorder = true;
					p.IsStrict = interval.StricktMinBorder;
					p.Index = i;
					if (!points.containsKey(interval.MinVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p);
						points.put((Long)interval.MinVal, point);
					}
					else
						points.get((Long)interval.MinVal).add(p);
					
					Point p2 = new Point();
					p2.Value = (Long)interval.MaxVal;
					p2.IsMinBorder = false;
					p2.IsStrict = interval.StricktMaxBorder;
					p2.Index = i;
					if (!points.containsKey(interval.MaxVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p2);
						points.put((Long)interval.MaxVal, point);
					}
					else
						points.get((Long)interval.MaxVal).add(p2);
				}
			}
			i++;
		}
		
		List<Interval> r = GetFinalIntervalsForConjunction(originalIntervals, points);
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	private static List<Interval> MergeIntervalsByConjunctionForDistributedField(List<List<Interval>> originalIntervals, Column column)
	{		
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<Double, List<Point>> points = new TreeMap<Double, List<Point>>();
		
		int i = 0;
		for (List<Interval> listIntervals : originalIntervals)
		{
			for (Interval interval: listIntervals)
			{
				if (!interval.HasOnlyIntervalsEstimatedRowCount)
				{
					Point p = new Point();
					p.Value = (Double)interval.MinVal;
					p.IsMinBorder = true;
					p.IsStrict = interval.StricktMinBorder;
					p.Index = i;
					if (!points.containsKey(interval.MinVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p);
						points.put((Double)interval.MinVal, point);
					}
					else
						points.get((Double)interval.MinVal).add(p);
					
					Point p2 = new Point();
					p2.Value = (Double)interval.MaxVal;
					p2.IsMinBorder = false;
					p2.IsStrict = interval.StricktMaxBorder;
					p2.Index = i;
					if (!points.containsKey(interval.MaxVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p2);
						points.put((Double)interval.MaxVal, point);
					}
					else
						points.get((Double)interval.MaxVal).add(p2);
				}
				
				
			}
			i++;
		}
		
		List<Interval> r = GetFinalIntervalsForConjunction(originalIntervals, points);
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	
 	private static List<Interval> MergeIntervalsByConjunctionForDictionaryField(List<List<Interval>> originalIntervals, Column column)
	{
		List<Interval> resIntervals = new ArrayList<Interval>();
		SortedMap<String, List<Point>> points = new TreeMap<String, List<Point>>();
		
		int i = 0;
		for (List<Interval> listIntervals : originalIntervals)
		{
			for (Interval interval: listIntervals)
			{
				if (!interval.HasOnlyIntervalsEstimatedRowCount)
				{
					Point p = new Point();
					p.Value = (String)interval.MinVal;
					p.IsMinBorder = true;
					p.IsStrict = interval.StricktMinBorder;
					p.Index = i;
					if (!points.containsKey(interval.MinVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p);
						points.put((String)interval.MinVal, point);
					}
					else
						points.get((String)interval.MinVal).add(p);
					
					Point p2 = new Point();
					p2.Value = (String)interval.MaxVal;
					p2.IsMinBorder = false;
					p2.IsStrict = interval.StricktMaxBorder;
					p2.Index = i;
					if (!points.containsKey(interval.MaxVal))
					{
						List<Point> point = new ArrayList<Point>();
						point.add(p2);
						points.put((String)interval.MaxVal, point);
					}
					else
						points.get((String)interval.MaxVal).add(p2);
				}
				
				
			}
			i++;
		}
		
		List<Interval> r = GetFinalIntervalsForConjunction(originalIntervals, points);
		for (Interval intl: r)
		{
			resIntervals.add(intl);
		}
		return resIntervals;
	}
	
	private static Map<String, List<Interval>> GetIntervalForQureryAndColumn(Query q1, Table t, Options opt)
	{
		Map<String, List<Interval>> res = new TreeMap<String, List<Interval>>();
		Map<String, Map<Integer, List<Predicate>>> columnsListpred1 = GetPredicateForEachColumn(q1, t, opt);
		
		for (Map<Integer, List<Predicate>> columnWithPredicate : columnsListpred1.values())
    	{
			Column c = null;
    		List<List<Interval>> intervalsAll = new ArrayList<List<Interval>>();
    		for (List<Predicate> disjPred : columnWithPredicate.values())
    		{
    			
    			List<Interval> intervals = new ArrayList<Interval>();
    			for (Predicate prediacte: disjPred)
    			{
    				Operator op = prediacte.op;
    				Object val = prediacte.value.toLowerCase();
    				
    	    		String columnName = prediacte.table + "." + prediacte.column;
    	    		// c is column, we are working with it now
    	    		c = opt.COLUMNS_DISTRIBUTION.get(columnName);
    	    		if (c == null)
        			{
    	    			//System.out.println("column = " + columnName + "is null");
    	    			return null;
        			}
    	    		else
    	    		{
	    				List<Interval> tmp = GetIntervalForPredicate(op, val, c, q1, opt, t);
	    				for (Interval intl : tmp)
	    				{
	    					intervals.add(intl);
	    				}
    	    		}
    			}
    			if (c != null)
    			{
    				
	    			List<Interval> intervals2 = MergeIntervalsByDisjunction(intervals, c);
	    			if (intervals2 == null)
	    				return null;
	    			intervalsAll.add(intervals2);
    			}
    			

    		}
    		if (c != null)
			{
	    		List<Interval> intervalsInQuery1 = MergeIntervalsByConjunction(intervalsAll, c);
	    		
	    		res.put(c.Name, intervalsInQuery1);
			}
    	}
		return res;
	}
	
	public static long GetEstimatedRowsOverall(Query q1, Query q2, Table t, Options opt)
    {
		if (t == null)
			return 0;
		long res1 = t.Count;
		long res2 = t.Count;
    	
    	Map<String, List<Interval>> mapColIntervals1 = GetIntervalForQureryAndColumn(q1, t, opt);
    	Map<String, List<Interval>> mapColIntervals2 = GetIntervalForQureryAndColumn(q2, t, opt);
    	
    	if ((mapColIntervals1 == null) || (mapColIntervals2 == null))
    		return -1;
    	Set<String> columnsInQuery1_ =  mapColIntervals1.keySet();
    	Set<String> columnsInQuery2_ =  mapColIntervals2.keySet();
    	
    	ArrayList<String> columnsInQuery1 = new ArrayList<String>();
    	columnsInQuery1.addAll(columnsInQuery1_);
    	ArrayList<String> columnsInQuery2 = new ArrayList<String>();
    	columnsInQuery2.addAll(columnsInQuery2_);
    	
    	Map<String, List<Interval>> mapColIntervalsAll = new TreeMap<String, List<Interval>>();
    	for (String column : columnsInQuery1)
    	{
    		List<Interval> intervals = mapColIntervals1.get(column);
    		Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    		Long tmp = new Long(0);
    		if (intervals.size() != 0)
    		tmp = GetEstimatedRowsCount(intervals, c, t);
    		if (tmp < res1)
    			res1 = tmp;	
    	}
    	
    	
    	for (String column : columnsInQuery2)
    	{
    		List<Interval> intervals = mapColIntervals2.get(column);
    		Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    		Long tmp = new Long(0);
    		if (intervals.size() != 0)
    		tmp = GetEstimatedRowsCount(intervals, c, t);
    		if (tmp < res2)
    			res2 = tmp;	
    	}

    	return res1 + res2;
    }
	
	public static Long GetEstimatedRowsOverlap(Query q1, Query q2, Table t, Options opt)
    {
		if (t == null)
			return new Long(0);
    	Long res = t.Count;
    	Double result = new Double(0);
    	Map<String, List<Interval>> mapColIntervals1 = GetIntervalForQureryAndColumn(q1, t, opt);
    	Map<String, List<Interval>> mapColIntervals2 = GetIntervalForQureryAndColumn(q2, t, opt);
    	
    	if ((mapColIntervals1 == null) || (mapColIntervals2 == null))
    		return new Long(0);
    	Set<String> columnsInQuery1_ =  mapColIntervals1.keySet();
    	Set<String> columnsInQuery2_ =  mapColIntervals2.keySet();
    	
    	ArrayList<String> columnsInQuery1 = new ArrayList<String>();
    	columnsInQuery1.addAll(columnsInQuery1_);
    	ArrayList<String> columnsInQuery2 = new ArrayList<String>();
    	columnsInQuery2.addAll(columnsInQuery2_);
    	
    	Map<String, List<Interval>> mapColIntervalsAll = new TreeMap<String, List<Interval>>();
    	Map<String, List<Interval>> mapColIntervalsOnlyIn1 = new TreeMap<String, List<Interval>>();
    	Map<String, List<Interval>> mapColIntervalsOnlyIn2 = new TreeMap<String, List<Interval>>();
    	for (String column : columnsInQuery1)
    	{

    		if (columnsInQuery2.contains(column))
    		{
    			List<List<Interval>> intervalAll = new ArrayList<List<Interval>>();
    			List<Interval> intervals1 = mapColIntervals1.get(column);
    			List<Interval> intervals2 = mapColIntervals2.get(column);
    			intervalAll.add(intervals1);
    			intervalAll.add(intervals2);
    			Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    			List<Interval> intervalsall = MergeIntervalsByConjunction(intervalAll, c);
    			mapColIntervalsAll.put(column, intervalsall);
    		}
    		else
    		{
    			List<Interval> intervals1 = mapColIntervals1.get(column);
    			mapColIntervalsOnlyIn1.put(column, intervals1);
    		}
    			
    	}
    	
    	for (String column : columnsInQuery2)
    	{

    		if (columnsInQuery1.contains(column))
    		{
    			
    		}
    		else
    		{
    			List<Interval> intervals2 = mapColIntervals2.get(column);
    			mapColIntervalsOnlyIn2.put(column, intervals2);
    		}	
    	}

    	Set<String> columnsInQueryAll_ =  mapColIntervalsAll.keySet();

    	
    	ArrayList<String> columnsInQueryAll = new ArrayList<String>();
    	columnsInQueryAll.addAll(columnsInQueryAll_);
    	
    	
    	for (String column : columnsInQueryAll)
    	{
    		List<Interval> intervals = mapColIntervalsAll.get(column);
    		Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    		Long tmp = new Long(0);
    		if (intervals.size() != 0)
    			tmp = GetEstimatedRowsCount(intervals, c, t);
    		if (tmp < res)
    			res = tmp;
    	}
    	result = res.doubleValue()/t.Count;
    	
    	Long res1 = t.Count;
    	Set<String> columnsInQueryOnlyIn1_ =  mapColIntervalsOnlyIn1.keySet();

    	ArrayList<String> columnsInQueryOnlyIn1 = new ArrayList<String>();
    	columnsInQueryOnlyIn1.addAll(columnsInQueryOnlyIn1_);
    	for (String column : columnsInQueryOnlyIn1)
    	{
    		List<Interval> intervals = mapColIntervalsOnlyIn1.get(column);
    		Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    		Long tmp = new Long(0);
    		if (intervals.size() != 0)
    			tmp = GetEstimatedRowsCount(intervals, c, t);
    		if (tmp < res1)
    			res1 = tmp;
    	}
    	
    	Long res2 = t.Count;
    	Set<String> columnsInQueryOnlyIn2_ =  mapColIntervalsOnlyIn2.keySet();

    	ArrayList<String> columnsInQueryOnlyIn2 = new ArrayList<String>();
    	columnsInQueryOnlyIn2.addAll(columnsInQueryOnlyIn2_);
    	for (String column : columnsInQueryOnlyIn2)
    	{
    		List<Interval> intervals = mapColIntervalsOnlyIn2.get(column);
    		Column c = opt.COLUMNS_DISTRIBUTION.get(column);
    		Long tmp = new Long(0);
    		if (intervals.size() != 0)
    			tmp = GetEstimatedRowsCount(intervals, c, t);
    		if (tmp < res2)
    			res2 = tmp;
    	}
    	
    	Double result2 = (res1.doubleValue() * res2.doubleValue()) / (t.Count * t.Count);
    	if (result2 < result)
    	result = result2;
    	Long finalRes = Math.round(result * t.Count);
    	return finalRes;
    }
	
	public static Long GetEstimatedRowsCount(List<Interval> intervals, Column column, Table t)
	{
		Long res= new Long(0);
		Long res2 = t.Count;
		// TODO: implementation
		for (Interval interval : intervals)
		{
			if (!interval.HasOnlyIntervalsEstimatedRowCount)
				// calculate the estimated row count inside the interval
				res = res + GetEstimatedRowsCountInsideTheInterval(interval, column, t);
			else
			{
				Long r = GetEstimatedRowsCountInsideTheInterval(interval, column, t);
				if (res2 > r)
					res2 = r;
			}
		}
		if (res == 0)
			res = t.Count;
		return res < res2 ? res : res2;
	}
	
	public static Long GetEstimatedRowsCountInsideTheIntervalForDictionaryField(Interval interval, Column column, Table t)
	{
		Long res= new Long(0);
		String minVal = interval.MinVal.toString().replace("'", "");
		String maxVal = interval.MaxVal.toString().replace("'", "");
		
		Boolean stricktMinBorder = interval.StricktMinBorder;
		Boolean stricktMaxBorder = interval.StricktMaxBorder;
		
		DictionaryField dictionaryField = (DictionaryField)column.Distribution;
		ValueState minValueState = dictionaryField.Values.get(minVal);
		ValueState maxValueState = dictionaryField.Values.get(maxVal);
		
		
		if (minVal.equals(maxVal))
		{
			if (minValueState == null)
			{
				res= new Long(0);
			}
			else
				res = minValueState.ValuesCount;
		}
		else
		{
			Long minValCount = new Long(0);
			Long maxValCount = new Long(0);
			if (stricktMinBorder)
				minValCount = minValueState.ValuesLessOrEqualCount - minValueState.ValuesCount;
			else
				minValCount = minValueState.ValuesLessOrEqualCount;
			
			if (stricktMaxBorder)
				maxValCount = maxValueState.ValuesLessOrEqualCount;
			else
				maxValCount = maxValueState.ValuesLessOrEqualCount - maxValueState.ValuesCount;
			res = Math.abs( maxValCount - minValCount);
				
		}
		return res;
	}
	
	public static Long GetEstimatedRowsCountInsideTheIntervalForDistributedField(Interval interval, Column column, Table t)
	{
		Long res= new Long(0);
		if (interval.HasOnlyIntervalsEstimatedRowCount)
			return interval.EstimatedRowCount;
		Double minVal = (Double)interval.MinVal;
		Double maxVal = (Double)interval.MaxVal;
		
		
		DistributedField distributedField = (DistributedField)column.Distribution;
		Double minValueState = distributedField.MinValue;
		Double maxValueState = distributedField.MaxValue;
		
		if (minVal.equals(maxVal))
		{
			// this is a place to discuss 
			res = new Long(1);
		}
		else
		{
			Double intervalWidth = (Double)Math.abs(maxVal - minVal);
			if (intervalWidth == 0)
				intervalWidth = (double) 1;
			Double columnWidth = (Double)Math.abs(maxValueState - minValueState);
			if (columnWidth == 0)
				columnWidth = (double) 1;
			res = Math.round( t.Count * intervalWidth / columnWidth);
		}
		return res;
	}
	
	public static Long GetEstimatedRowsCountInsideTheIntervalForDistributedFieldWithEmissions(Interval interval, Column column, Table t)
	{
		Long res= new Long(0);
		if (interval.HasOnlyIntervalsEstimatedRowCount)
			return interval.EstimatedRowCount;
		Double minVal = (Double)interval.MinVal;
		Double maxVal = (Double)interval.MaxVal;
		
		Boolean stricktMinBorder = interval.StricktMinBorder;
		Boolean stricktMaxBorder = interval.StricktMaxBorder;
		
		DistributedFieldWithEmissions distributedFieldWithEmissions = (DistributedFieldWithEmissions)column.Distribution;
		Double minValueState = distributedFieldWithEmissions.MinValue;
		Double maxValueState = distributedFieldWithEmissions.MaxValue;
		
		if (minVal.equals(maxVal))
		{
			// this is a place to discuss 
			res = new Long(1);
		}
		else
		{
			Double intervalWidth = (Double)Math.abs(maxVal - minVal);
			if (intervalWidth == 0)
				intervalWidth = (double) 1;
			Double columnWidth = (Double)Math.abs(maxValueState - minValueState);
			if (columnWidth == 0)
				columnWidth = (double) 1;
			res = Math.round( t.Count * intervalWidth / columnWidth);
		}
		
		for (ValueState emission :  distributedFieldWithEmissions.Values.values())
		{
			Double emissionVal = Double.parseDouble(emission.Value.toString());
			if (stricktMinBorder && stricktMaxBorder)
			{
				if ((emissionVal >= minVal) && (emissionVal <= maxVal))
					res = res + emission.ValuesCount;
			}
			if (stricktMinBorder && !stricktMaxBorder)
			{
				if ((emissionVal >= minVal) && (emissionVal < maxVal))
					res = res + emission.ValuesCount;
			}
			if (!stricktMinBorder && stricktMaxBorder)
			{
				if ((emissionVal > minVal) && (emissionVal <= maxVal))
					res = res + emission.ValuesCount;
			}
			if (!stricktMinBorder && !stricktMaxBorder)
			{
				if ((emissionVal >=minVal) && (emissionVal < maxVal))
					res = res + emission.ValuesCount;
			}
		}
		return res;
	}
	
	public static Long GetEstimatedRowsCountInsideTheIntervalForIdentificator(Interval interval, Column column, Table t)
	{
		Long res= new Long(0);
		if (interval.HasOnlyIntervalsEstimatedRowCount)
			return interval.EstimatedRowCount;
		Long minVal = (Long)interval.MinVal;
		Long maxVal = (Long)interval.MaxVal;
		
		
		Identificator identificatorField = (Identificator)column.Distribution;
		Long minValueState = identificatorField.MinValue;
		Long maxValueState = identificatorField.MaxValue;
		
		if (minVal.equals(maxVal))
		{
			res = new Long(1);
		}
		else
		{
			Long intervalWidth = (Long)Math.abs(maxVal - minVal);
			if (intervalWidth == 0)
				intervalWidth = (long) 1;
			Long columnWidth = (Long)Math.abs(maxValueState - minValueState);
			if (columnWidth == 0)
				columnWidth = (long) 1;
			res = Math.round( t.Count * (double)intervalWidth / columnWidth);
		}
		return res;
	}
	
	public static Long GetEstimatedRowsCountInsideTheInterval(Interval interval, Column column, Table t)
	{
		Long res= new Long(0);
		
		switch (column.GlobalColumnType)
    	{
    		case DictionaryField:
    		{
    			res = GetEstimatedRowsCountInsideTheIntervalForDictionaryField(interval, column, t);
    		}
    		break;
    		case DistributedField:
    		{
    			res = GetEstimatedRowsCountInsideTheIntervalForDistributedField(interval, column, t);
    		}
    		break;
    		case DistributedFieldWithEmissions:
    		{
    			res = GetEstimatedRowsCountInsideTheIntervalForDistributedFieldWithEmissions(interval, column, t);
    		}
    		break;
    		case Identificator:
    		{
    			res = GetEstimatedRowsCountInsideTheIntervalForIdentificator(interval, column, t);
    		}
    		break;
    	}
		return res;
	}
	
	public static Map<String, Map<Integer, List<Predicate>>> GetPredicateForEachColumn(Query q, Table t, Options opt)
	{
		Map<String, Map<Integer, List<Predicate>>> res = new TreeMap<String, Map<Integer, List<Predicate>>>();
		List<List<Predicate>> predicatesInThisQuery = q.whereClausesTerms;
    	long minimumRowCount = t.Count;

    	int iList = 0;
    	for (List<Predicate> lispredicate : predicatesInThisQuery)
    	{
    		int i = 0;
    		for (Predicate orPredicate : lispredicate)
    		{
    			String column = orPredicate.column;
    			String table = orPredicate.table;
    			String value = orPredicate.value;
    			Operator operator = orPredicate.op;
    			
    			if (table.equals(t.Name))
    			{
    				String fullColumnName = table + "." + column;
    				Column col = opt.COLUMNS_DISTRIBUTION.get(fullColumnName);
    				Map<Integer, List<Predicate>> maplistPred = res.get(fullColumnName);
    				if (maplistPred == null)
    				{
    					maplistPred = new TreeMap<Integer, List<Predicate>>();
    					List<Predicate> listPred = new ArrayList<Predicate>();
    					listPred.add(orPredicate);
    					maplistPred.put(iList, listPred);
    					res.put(fullColumnName, maplistPred);
    				}
    				else
    				{
    					List<Predicate> listPred =  maplistPred.get(iList);
    					if (listPred == null)
    					{
    						listPred = new ArrayList<Predicate>();
        					listPred.add(orPredicate);
        					maplistPred.put(iList, listPred);
    					}
    					else
    					{
    						listPred.add(orPredicate);
    					}
    					
    				}
    			}
    		}
    		iList++;
    	}
		return res;
	}
}
