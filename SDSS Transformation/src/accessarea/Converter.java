/* Written by FlorianB for SQLQueryLogTransformer:
 * This is the most important class, here we traverse the hierarchy of the current statement a first time and extract all the predicates (and relations and predicates from any subquery)
 * We start with the given expression and do a depth-first search and always keep a predicate or transform it or extract the access area form a subquery
 */
package accessarea;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.BooleanValue;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.FunctionItem;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;

public class Converter implements SelectVisitor, SelectItemVisitor, FromItemVisitor, ExpressionVisitor, ItemsListVisitor {
	
	private ExpressionVisitor expressionVisitor;
	private Stack<Expression> stack = new Stack<Expression>();
	private NullValue initExpression = new NullValue();
	private AccessArea accessArea = new AccessArea(new ArrayList<FromItem>(), initExpression);
	private Expression subWhere;
	private int fPhotoFlags = 0;
	private int fPrimTarget = 0;
	private int fPhotoStatus = 0;
	private int fPhotoType = 0;
	private int fCoordType = 0;
	
	
	
	private String alias = "";
	public AccessArea convertPredicate(FunctionItem func){
		Expression predicate = func.getFunction();
		alias = func.getAlias();
		predicate.accept(this); //here we traverse the hierarchy-> start!
		Expression where = stack.pop();
		//subwhere are the constraints from any subquery that was not in combination with any IN, EXISTS, ALL, ANY...
		if(subWhere != null){//have to merge it with the rest of the access area
			if(where != null && !(where instanceof BooleanValue)){
				AndExpression tempAndExpression = new AndExpression(null,null);
				if(where instanceof Parenthesis || !(where instanceof OrExpression)) tempAndExpression.setLeftExpression(where);
				else { 
					Parenthesis tempParenthesis = new Parenthesis();
					tempParenthesis.setExpression(where);
					tempAndExpression.setLeftExpression(tempParenthesis);
				}
				tempAndExpression.setRightExpression(subWhere);
				where = tempAndExpression;
			} else if (where instanceof BooleanValue) {
				if(((BooleanValue)where).getValue()) where = subWhere;
			} else where = subWhere;
		}
		if (!(where instanceof Parenthesis) && (where instanceof OrExpression)) {//Make a parenthesis around the where if it is an OR expression
			Parenthesis tempParenthesis = new Parenthesis();
			tempParenthesis.setExpression(where);
			where = tempParenthesis;
		}
		accessArea.setWhere(where);
		return accessArea;
	}

	public AccessArea convertPredicate(Expression predicate){
		predicate.accept(this); //here we traverse the hierarchy-> start!
		Expression where = stack.pop();
		//subwhere are the constraints from any subquery that was not in combination with any IN, EXISTS, ALL, ANY...
		if(subWhere != null){//have to merge it with the est of the access area
			if(where != null && !(where instanceof BooleanValue)){
				AndExpression tempAndExpression = new AndExpression(null,null);
				if(where instanceof Parenthesis || !(where instanceof OrExpression)) tempAndExpression.setLeftExpression(where);
				else { 
					Parenthesis tempParenthesis = new Parenthesis();
					tempParenthesis.setExpression(where);
					tempAndExpression.setLeftExpression(tempParenthesis);
				}
				tempAndExpression.setRightExpression(subWhere);
				where = tempAndExpression;
			} else if (where instanceof BooleanValue) {
				if(((BooleanValue)where).getValue()) where = subWhere;
			} else where = subWhere;
		}
		if (!(where instanceof Parenthesis) && (where instanceof OrExpression)) {//Make a parenthesis around the where if it is an OR expression
			Parenthesis tempParenthesis = new Parenthesis();
			tempParenthesis.setExpression(where);
			where = tempParenthesis;
		}
		accessArea.setWhere(where);
		return accessArea;
	}
	//for any binary expression this function processes the right hand side and left hand side of it
	public void visitBinaryExpression(BinaryExpression binaryExpression) {
		Expression tempLeftExpression = binaryExpression.getLeftExpression();
		Expression tempRightExpression = binaryExpression.getRightExpression();
		if(((BinaryExpression)stack.peek()).isNot()){//handle any NOT
			if(tempLeftExpression instanceof BinaryExpression) {
				((BinaryExpression)tempLeftExpression).toggleNot();
			}
			if(tempRightExpression instanceof BinaryExpression) {
				((BinaryExpression)tempRightExpression).toggleNot();
			}
		}
		//if left side is a function we need to reverse the order
		if(tempLeftExpression instanceof Function) stack.push(tempRightExpression); 
		tempLeftExpression.accept(this);
		if(!(tempLeftExpression instanceof Function)) tempRightExpression.accept(this);	
		
		//combine the parent element with its right hand side and left hand side:
		Expression right = stack.pop();
		Expression left = stack.pop();
		Expression parent = stack.pop();
		
		//we have to handle if any element was deleted or set to TRUE or FALSE. On the hierarchy we cannot change the type of an expression itself, therefore we have to handle it here and construct a new parent expression
		Expression expression = null;
		if(parent != null) {
			if(right == null) expression = left;
			else if (left == null) expression = right;
			else if(right instanceof BooleanValue && !(parent instanceof BooleanValue)) {
				if(((BooleanValue)right).getValue() == true){
					if(parent instanceof AndExpression  && (left instanceof MinorThanEquals || left instanceof MinorThan || left instanceof GreaterThan || left instanceof GreaterThanEquals || left instanceof IsNullExpression || left instanceof EqualsTo || left instanceof NotEqualsTo || left instanceof LikeExpression || left instanceof AndExpression || left instanceof OrExpression)) expression = left;
					else expression = right;
				} else {
					if(parent instanceof AndExpression) expression = right;
					else if(parent instanceof OrExpression && (left instanceof MinorThanEquals || left instanceof MinorThan || left instanceof GreaterThan || left instanceof GreaterThanEquals || left instanceof IsNullExpression || left instanceof EqualsTo || left instanceof NotEqualsTo || left instanceof LikeExpression || left instanceof AndExpression || left instanceof OrExpression)) expression = left;		
					else expression = right;
				}
			} else if(left instanceof BooleanValue && !(parent instanceof BooleanValue)) {
				if(((BooleanValue)left).getValue() == true){
					if(parent instanceof AndExpression && (right instanceof MinorThanEquals || right instanceof MinorThan || right instanceof GreaterThan || right instanceof GreaterThanEquals || right instanceof IsNullExpression || right instanceof EqualsTo || right instanceof NotEqualsTo || right instanceof LikeExpression || right instanceof AndExpression || right instanceof OrExpression)) expression = right;
					else expression = left;
				} else {
					if(parent instanceof AndExpression) expression = left;
					else if(parent instanceof OrExpression  && (right instanceof MinorThanEquals || right instanceof MinorThan || right instanceof GreaterThan || right instanceof GreaterThanEquals || right instanceof IsNullExpression || right instanceof EqualsTo || right instanceof NotEqualsTo || right instanceof LikeExpression || right instanceof AndExpression || right instanceof OrExpression)) expression = right;	
					else expression = left;
				}
			} else if(!(parent instanceof BooleanValue)){
				if(((BinaryExpression)parent).isNot()) ((BinaryExpression)parent).setNot(false);
				((BinaryExpression)parent).setLeftExpression(left);
				((BinaryExpression)parent).setRightExpression(right);
				expression = parent;
			} else expression = parent;
		} else expression = parent;
		stack.push(expression);
	}

	@Override
	public void visit(ExpressionList expressionList) { 
		stack.push((Expression)expressionList);
	}

	@Override
	public void visit(MultiExpressionList multiExprList) { 
		stack.push((Expression)multiExprList);
	}

	@Override
	public void visit(NullValue nullValue) {
		stack.push(nullValue);
	}

	@Override
	public void visit(Function function) {//Transform aggregate functions and SDSS special functions
		String name = function.getName().toLowerCase();
			if(name.startsWith("dbo.")) name = name.substring(4,name.length());
			Expression right = null;
			Expression parent = null;
			if(!stack.isEmpty()){
				right = stack.pop();
				parent = stack.pop();
			}
		if(name.toLowerCase().startsWith("f")) { //is SDSS special function
			Float ra1 = null;
			Float ra2 = null;
			Float dec1 = null;
			Float dec2 = null;
			Float r = null;
			Double rRangeMin = null;
			Double rRangeMax = null;
			Double rRange = null;
			Double threshold = null;
			String ObjectTable = null;
			int zoom = -1;
			switch (name) {
				//case	"fdistanceeq": 		//returns distance (arcmins) between two points (ra1,dec1) and (ra2,dec2), usually used with attribute as parameter, needs to be calculated for each value pair
											// only 105 times in FROM OR WHERE PART, else in SELECT
				//	break;
				case	"fdoccolumns":		//Return the list of Columns in a given table or view, i.e.: accesses the schema, returns no rows, only used in FROM part
											//Proposal: add table to list of accessed tables, add FALSE as no tuples would influence the result
											//This function ONLY occurs in statements of the following form: SELECT * FROM dbo.fDocColumns('[...]')
											//Where [..] is the name of some table or view. No other tables or predicates in these queries
											//Accesses DBColumns table to get descriptions
					String tableString = function.getParameters().getExpressions().get(0).toString();
					Table table = new Table(null,tableString.substring(1, tableString.length()-1));
					stack.push(new BooleanValue(false));
					List<FromItem> tempFromItemList = accessArea.getFrom();
					FromItem fromItem = table;
					tempFromItemList.add(fromItem);
					table = new Table(null,"DBColumns");
					fromItem = table;
					tempFromItemList.add(fromItem);
					accessArea.setFrom(tempFromItemList);
					//System.out.println(table);
					break;

				case	"fgetnearbyobjalleq":	//Returns a table of all objects within @r arcmins of the point. There is no limit on @r.
												//Note with respect to the following functions: Table All refers to primary, secondary and other objects
												//One arcmin is 0.0166666666667 degree
												//cos(A) = sin(dec1)sin(dec2) + cos(dec1)cos(dec2)cos(ra1-ra2) -> A = distance between the two points in degrees
												//Only 1 time occurs more than once in a query, therefore not handled
												//Rewritten as SuperSet: SELECT * FROM PhotoObjAll where ra between 185-0.05 and 185+0.05 and dec between -0.05 and 0.05
					if(ObjectTable == null) ObjectTable = "PhotoObjAll";
					
				case	"fgetnearbyspecobjalleq":	//Returns a table of spectrum objects within @r arcmins of an equatorial point (@ra, @dec). (Primary, Secondary, other)
													//Never occurs more than once in a query, therefore not handled
					if(ObjectTable == null) ObjectTable = "SpecObjAll"; //Only SuperSet
					
				case	"fgetobjfromrecteq"://Returns a table of objects inside a rectangle defined by two ra,dec pairs. Note the order of the parameters: @ra1, @dec1, @ra2, @dec2 
											//This is the same as fgetobjfromrect expect for the order of parameters!
											//Both can be converted to select * from PhotoPrimary where ra between 185 and 185.1 and dec between 0 and 0.1
											//Both occur only a total of 7 times more than once in one query. Therefore we leave the handling of multiple occurrences for now
					ra1 = Float.parseFloat(function.getParameters().getExpressions().get(0).toString());
					dec2 = Float.parseFloat(function.getParameters().getExpressions().get(3).toString());
					//System.out.println(ra2);
					if(ra2 == null) ra2 = Float.parseFloat(function.getParameters().getExpressions().get(2).toString());					
					if(dec1 == null) dec1 = Float.parseFloat(function.getParameters().getExpressions().get(1).toString());
					String subQueryR1 = "SELECT * FROM PhotoPrimary as "+alias+" where "+alias+".ra between "+ra1+" and "+ra2+" and "+alias+".dec between "+dec1+" and "+dec2;
					try { //Photoflags table never used in SDSS log, therefore no need to check for multiple occurrences
						stack.push(parent);
						Statement stmt = CCJSqlParserUtil.parse(subQueryR1);
						Select selectStatement = (Select) stmt;
						SubSelect subSelect = new SubSelect();
						subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
						subSelect.accept((ExpressionVisitor)this);
						if(right != null) right.accept(this);
						else stack.pop();
					} catch (JSQLParserException e) {
						//System.out.println("Could not parse Subquery from function: "+function);
						//e.printStackTrace();
					}		
					break;
				//case	"flambdafromeq": 	//http://www.sdss3.org/dr10/software/idlutils_doc.php#EQ2CSURVEY -> Appears ONLY in SELECT as flambdafromeq(ra,dec), no numbers given
										 	//Calculation: lambda= r2d*asin(cos(dec*d2r)*sin((ra-racen)*d2r)) where r2d= 180.0D/(!DPI) and d2r= 1.D/r2d and racen = RA of center
				//	break;//
				case	"fgetobjfromrect"://Returns a table of objects inside a rectangle defined by two ra,dec pairs. Note the order of the parameters: @ra1, @ra2, @dec1, @dec2 
											//This is the same as fgetobjfromrecteq expect for the order of parameters!
											//Both can be converted to select * from PhotoPrimary where ra between 185 and 185.1 and dec between 0 and 0.1
											//Both occur only a total of 7 times more than once in one query. Therefore we leave the handling of multiple occurrences for now
					ra1 = Float.parseFloat(function.getParameters().getExpressions().get(0).toString());
					dec2 = Float.parseFloat(function.getParameters().getExpressions().get(3).toString());
					//System.out.println(ra2);
					if(ra2 == null) ra2 = Float.parseFloat(function.getParameters().getExpressions().get(1).toString());					
					if(dec1 == null) dec1 = Float.parseFloat(function.getParameters().getExpressions().get(2).toString());
					String subQueryR2 = "SELECT * FROM PhotoPrimary as "+alias+" where "+alias+".ra between "+ra1+" and "+ra2+" and "+alias+".dec between "+dec1+" and "+dec2;
					try { //Photoflags table never used in SDSS log, therefore no need to check for multiple occurrences
						stack.push(parent);
						Statement stmt = CCJSqlParserUtil.parse(subQueryR2);
						Select selectStatement = (Select) stmt;
						SubSelect subSelect = new SubSelect();
						subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
						subSelect.accept((ExpressionVisitor)this);
						if(right != null) right.accept(this);
						else stack.pop();
					} catch (JSQLParserException e) {
					//System.out.println("Could not parse Subquery from function: "+function);
					//e.printStackTrace();
					}		
					break;
					//case	"flambdafromeq": 	//http://www.sdss3.org/dr10/software/idlutils_doc.php#EQ2CSURVEY -> Appears ONLY in SELECT as flambdafromeq(ra,dec), no numbers given
									 	//Calculation: lambda= r2d*asin(cos(dec*d2r)*sin((ra-racen)*d2r)) where r2d= 180.0D/(!DPI) and d2r= 1.D/r2d and racen = RA of center
					//	break;
				
				case	"fphotoflags":		//fPhotoFlags('[...]') where [...] is some String can always be rewritten as "SELECT value FROM PhotoFlags WHERE name = '[...]'"
					String subQuery = "SELECT \"PhotoFlags.value\" FROM PhotoFlags WHERE PhotoFlags.name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
					if (fPhotoFlags == 0)
						try { //Photoflags table never used in SDSS log, therefore no need to check for multiple occurrences //Problem: function used multiple times in some queries
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQuery);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							fPhotoFlags++;
							subSelect.accept((ExpressionVisitor)this);
							right.accept(this);
						} catch (JSQLParserException e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					else {
						try {
							subQuery = "SELECT \"PhotoFlags"+fPhotoFlags+".value\" FROM PhotoFlags as PhotoFlags"+fPhotoFlags+" WHERE PhotoFlags"+fPhotoFlags+".name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
							fPhotoFlags++;
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQuery);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							subSelect.accept((ExpressionVisitor)this);
							stack.pop();
							stack.pop();
							if(parent!=null) {
								stack.push(new BooleanValue(true));
								stack.push(new BooleanValue(true));	
								if(right != null) {
									right.accept(this);
									stack.pop();								
								}
							}
							stack.push(new BooleanValue(true));
						} catch (Exception e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					}
					break;
				case	"fprimtarget":		//fprimtarget('[...]') where [...] is some String can always be rewritten as "SELECT value FROM PhotoFlags WHERE name = '[...]'"
					String subQueryR3 = "SELECT \"PrimTarget.value\" FROM PrimTarget WHERE PrimTarget.name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
					if (fPrimTarget == 0)
						try { //PrimTarget table never used in SDSS log, therefore no need to check for multiple occurrences //Problem: function used multiple times in some queries
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR3);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							fPrimTarget++;
							subSelect.accept((ExpressionVisitor)this);
							right.accept(this);
						} catch (JSQLParserException e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					else {
						try {
							subQuery = "SELECT \"PrimTarget"+fPrimTarget+".value\" FROM PrimTarget as PrimTarget"+fPrimTarget+" WHERE PrimTarget"+fPrimTarget+".name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
							fPrimTarget++;
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR3);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							subSelect.accept((ExpressionVisitor)this);
							stack.pop();
							stack.pop();
							if(parent!=null) {
								stack.push(new BooleanValue(true));
								stack.push(new BooleanValue(true));	
								if(right != null) {
									right.accept(this);
									stack.pop();								
								}
							}
							stack.push(new BooleanValue(true));
						} catch (Exception e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					}
					break;

				case	"fphotostatus":		//fphotostatus('[...]') where [...] is some String can always be rewritten as "SELECT value FROM PhotoFlags WHERE name = '[...]'"
					String subQueryR4 = "SELECT \"PhotoStatus.value\" FROM PhotoStatus WHERE PhotoStatus.name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
					if (fPhotoStatus == 0)
						try { //PhotoStatus table never used in SDSS log, therefore no need to check for multiple occurrences //Problem: function used multiple times in some queries
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR4);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							fPhotoStatus++;
							subSelect.accept((ExpressionVisitor)this);
							right.accept(this);
						} catch (JSQLParserException e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					else {
						try {
							subQuery = "SELECT \"PhotoStatus"+fPhotoStatus+".value\" FROM PhotoStatus as PhotoStatus"+fPhotoStatus+" WHERE PhotoStatus"+fPhotoStatus+".name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
							fPhotoStatus++;
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR4);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							subSelect.accept((ExpressionVisitor)this);
							stack.pop();
							stack.pop();
							if(parent!=null) {
								stack.push(new BooleanValue(true));
								stack.push(new BooleanValue(true));	
								if(right != null) {
									right.accept(this);
									stack.pop();								
								}
							}
							stack.push(new BooleanValue(true));
						} catch (Exception e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					}
					break;

				case	"fphototype":		//fphototype('[...]') where [...] is some String can always be rewritten as "SELECT value FROM PhotoFlags WHERE name = '[...]'"
					String subQueryR5 = "SELECT \"PhotoType.value\" FROM PhotoType WHERE PhotoType.name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
					if (fPhotoType == 0)
						try { //PhotoType table never used in SDSS log, therefore no need to check for multiple occurrences //Problem: function used multiple times in some queries
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR5);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							fPhotoType++;
							subSelect.accept((ExpressionVisitor)this);
							right.accept(this);
						} catch (JSQLParserException e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					else {
						try {
							subQuery = "SELECT \"PhotoType"+fPhotoType+".value\" FROM PhotoType as PhotoType"+fPhotoType+" WHERE PhotoType"+fPhotoType+".name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
							fPhotoType++;
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR5);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							subSelect.accept((ExpressionVisitor)this);
							stack.pop();
							stack.pop();
							if(parent!=null) {
								stack.push(new BooleanValue(true));
								stack.push(new BooleanValue(true));	
								if(right != null) {
									right.accept(this);
									stack.pop();								
								}
							}
							stack.push(new BooleanValue(true));
						} catch (Exception e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					}
					break;	
				case	"fgetnearbyobjeq"://Returns table holding a record describing the closest primary object within @r arcminutes of (@ra,@dec)
					// Need to consider that r is given in arc/minutes we have to divide r/(60)
					// Example select * from dbo.fGetNearbyObjEq(185,0,.3) 
					//Can be converted to select * from PhotoObjAll where ra between 185-(.3/(60*(2^.5))) and 185+(.3/(60*(2^.5))) and dec between 0-(.3/(60*(2^.5))) and 0 + (.3/(60*(2^.5)))
					ra1 = Float.parseFloat(function.getParameters().getExpressions().get(0).toString());
					dec1 = Float.parseFloat(function.getParameters().getExpressions().get(1).toString());
					r = Float.parseFloat(function.getParameters().getExpressions().get(2).toString());
					rRange = (r/60)/(Math.cos(Math.abs(dec1)));
					rRangeMin= ra1 - rRange;
					rRangeMax= ra1 + rRange;
					//threshold= 4*Math.pow(Math.sin(Math.toRadians((r/60)/2)),2);
					//String subQueryR6 = "SELECT * FROM PhotoObjAll  where PhotoObjAll.ra between "+(ra1-(r*r_conversion))+" and "+(ra1+(r*r_conversion))+" and PhotoObjAll.dec between "+(dec1-(r*r_conversion))+" and "+(dec1+(r*r_conversion))+" and mode = 1";
					//String subQueryR6 = "SELECT * FROM PhotoObjAll  where PhotoObjAll.ra between "+ rRangeMin +" and "+ rRangeMax+ " and PhotoObjAll.dec between "+(dec1-(r/60))+" and "+(dec1+(r/60))
					//		+ " and PhotoObjAll.mode = 1 ";
							//+ "and "+threshold+"> (power(cx-(" + Math.cos(Math.toRadians(ra1))*Math.cos(Math.toRadians(dec1)) + "),2) + power(cy-("+ Math.sin(Math.toRadians(ra1))*Math.cos(Math.toRadians(dec1)) + "),2) + power(cz-(" + Math.sin(Math.toRadians(dec1)) +"),2))";
					//System.out.println("SubQueryR6: "+subQueryR6);
					if(alias == null) alias = "n";
					String subQueryR6 = "SELECT * FROM PhotoObjAll as "+alias+" where "+alias+".ra between "+ rRangeMin +" and "+ rRangeMax+ " and "+alias+".dec between "+(dec1-(r/60))+" and "+(dec1+(r/60))
							+ " and "+alias+".mode = 1 ";
					try {
						stack.push(parent);
						Statement stmt = CCJSqlParserUtil.parse(subQueryR6);
						Select selectStatement = (Select) stmt;
						SubSelect subSelect = new SubSelect();
						subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
						subSelect.accept((ExpressionVisitor)this);
						if(right != null) right.accept(this);
						else stack.pop();
					} catch (JSQLParserException e) {
					//System.out.println("Could not parse Subquery from function: "+function);
					//e.printStackTrace();
					}		
					break;	
				case	"fgetnearestobjeq"://Given an equatorial point (@ra,@dec), returns table of primary objects within @r arcmins of the point. There is no limit on @r.
					// Need to consider that r is given in arc/minutes we have to divide r/(60)
					// select * from dbo.fgetnearestobjeq(185,0,1)
					//select top 1 objID, run, camcol, field,	rerun, type, cx, cy, cz, htmID, 
					//DEGREES(2*ASIN(SQRT(POWER(cx-COS(185*asin(1)*2/180) * COS(0*asin(1)*2/180),2)+POWER(cy-SIN(185*asin(1)*2/180) * COS(0*asin(1)*2/180),2)+POWER(cz-SIN(0*asin(1)*2/180),2))/2))*60 as distance 
					//from PhotoObjAll where PhotoObjAll.dec >= -0.0166666667 AND PhotoObjAll.dec <= 0.0166666667 AND PhotoObjAll.ra >= 184.9832188617 AND PhotoObjAll.ra <= 185.0167811383 AND mode = 1
					//ORDER BY distance ASC
					ra1 = Float.parseFloat(function.getParameters().getExpressions().get(0).toString());
					dec1 = Float.parseFloat(function.getParameters().getExpressions().get(1).toString());
					r = Float.parseFloat(function.getParameters().getExpressions().get(2).toString());
					rRange = (r/60)/(Math.cos(Math.abs(dec1)));
					rRangeMin= ra1 - rRange;
					rRangeMax= ra1 + rRange;
					//threshold= 4*Math.pow(Math.sin(Math.toRadians((r/60)/2)),2);
					//String subQueryR7 = "select top 1 objID, run, camcol, field,	rerun, type, cx, cy, cz, htmID, "
					//		+ "DEGREES(2*ASIN(SQRT(POWER(cx-COS("+ra1+"*asin(1)*2/180) * COS("+dec1+"*asin(1)*2/180),2)+POWER(cy-SIN("+ra1+"*asin(1)*2/180) * COS("+dec1+"*asin(1)*2/180),2)+POWER(cz-SIN("+dec1+"*asin(1)*2/180),2))/2))*60 as distance " 
					//		+ "FROM PhotoObjAll  where PhotoObjAll.ra between "+ rRangeMin +" and "+ rRangeMax+ " and PhotoObjAll.dec between "+(dec1-(r/60))+" and "+(dec1+(r/60))
					//		+ " and PhotoObjAll.mode = 1 ";
							//+ "and "+threshold+"> (power(cx-(" + Math.cos(Math.toRadians(ra1))*Math.cos(Math.toRadians(dec1)) + "),2) + power(cy-("+ Math.sin(Math.toRadians(ra1))*Math.cos(Math.toRadians(dec1)) + "),2) + power(cz-(" + Math.sin(Math.toRadians(dec1)) +"),2))";
					if(alias == null) alias = "n";
					String subQueryR7 = "select top 1 objID, run, camcol, field,	rerun, type, cx, cy, cz, htmID, "
							+ "DEGREES(2*ASIN(SQRT(POWER(cx-COS("+ra1+"*asin(1)*2/180) * COS("+dec1+"*asin(1)*2/180),2)+POWER(cy-SIN("+ra1+"*asin(1)*2/180) * COS("+dec1+"*asin(1)*2/180),2)+POWER(cz-SIN("+dec1+"*asin(1)*2/180),2))/2))*60 as distance " 
							+ "FROM PhotoObjAll as "+alias+" where "+alias+".ra between "+ rRangeMin +" and "+ rRangeMax+ " and "+alias+".dec between "+(dec1-(r/60))+" and "+(dec1+(r/60))
							+ " and "+alias+".mode = 1 ";
					
					try {
						stack.push(parent);
						Statement stmt = CCJSqlParserUtil.parse(subQueryR7);
						Select selectStatement = (Select) stmt;
						SubSelect subSelect = new SubSelect();
						subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
						subSelect.accept((ExpressionVisitor)this);
						if(right != null) right.accept(this);
						else stack.pop();
					} catch (JSQLParserException e) {
					//System.out.println("Could not parse Subquery from function: "+function);
					//e.printStackTrace();
					}		
					break;	
				case	"fcoordtype":		//fcoordtype('[...]') where [...] is some String can always be rewritten as "SELECT value FROM CoordType WHERE name = '[...]'"
					String subQueryR8 = "SELECT \"CoordType.value\" FROM CoordType WHERE CoordType.name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
					if (fCoordType == 0)
						try { //PhotoType table never used in SDSS log, therefore no need to check for multiple occurrences //Problem: function used multiple times in some queries
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR8);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							fPhotoType++;
							subSelect.accept((ExpressionVisitor)this);
							right.accept(this);
						} catch (JSQLParserException e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					else {
						try {
							subQuery = "SELECT \"CoordType"+fCoordType+".value\" FROM CoordType as CoordType"+fCoordType+" WHERE CoordType"+fCoordType+".name = " + function.getParameters().getExpressions().get(0).toString(); //need " around value since it else could not be parsed (reserved expression)
							fCoordType++;
							stack.push(parent); 
							Statement stmt = CCJSqlParserUtil.parse(subQueryR8);
							Select selectStatement = (Select) stmt;
							SubSelect subSelect = new SubSelect();
							subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
							subSelect.accept((ExpressionVisitor)this);
							stack.pop();
							stack.pop();
							if(parent!=null) {
								stack.push(new BooleanValue(true));
								stack.push(new BooleanValue(true));	
								if(right != null) {
									right.accept(this);
									stack.pop();								
								}
							}
							stack.push(new BooleanValue(true));
						} catch (Exception e) {
							//System.out.println("Could not parse Subquery from function: "+function);
							//e.printStackTrace();
						}
					}
					break;					
				case	"fphototypen":		//Returns the PhotoType name, indexed by value (3-> Galaxy, 6-> Star,...) 
											//Can be rewritten as "select name from PhotoType where value = [...]" where [...] is the Parameter of the function
					String subQueryT = "SELECT name FROM PhotoType WHERE \"value\" = " + function.getParameters().getExpressions().get(0).toString(); 
					try { //PhotoType table never used in SDSS log, therefore no need to check for multiple occurrences
						stack.push(parent);
						Statement stmt = CCJSqlParserUtil.parse(subQueryT);
						Select selectStatement = (Select) stmt;
						SubSelect subSelect = new SubSelect();
						subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
						subSelect.accept((ExpressionVisitor)this);
						if(right != null) right.accept(this);
						else stack.pop();
					} catch (JSQLParserException e) {
						//System.out.println("Could not parse Subquery from function: "+function);
						//e.printStackTrace();
					}
					break;
				case    "fgetnearbyframeeq":
					float racenter = Float.parseFloat(function.getParameters().getExpressions().get(0).toString());
					float deccenter = Float.parseFloat(function.getParameters().getExpressions().get(1).toString());
					r = Float.parseFloat(function.getParameters().getExpressions().get(2).toString());
					rRange = (r/60)/(Math.cos(Math.abs(deccenter)));
					rRangeMin= racenter - rRange;
					rRangeMax= racenter + rRange;
					dec1 = (float) (deccenter + r/60);
					dec2 = (float) (deccenter - r/60);
					zoom = Integer.parseInt(function.getParameters().getExpressions().get(3).toString());
					System.out.println("Alias= "+ alias);
					String subQueryR9 = "SELECT fieldID, a, b, c, d, e, f, node, incl, distance "
					                                        +"FROM Frame as "+alias+" where "
					                                        +alias+".ra between "+rRangeMin+" and "+rRangeMax+" and "
					                                        +alias+".dec between "+Math.min(dec2, dec1)+" and "+Math.max(dec2, dec1)+" and "
					                                        +alias+".zoom = "+zoom;
					System.out.println(subQueryR9);
					try {
					        stack.push(parent);
					        Statement stmt = CCJSqlParserUtil.parse(subQueryR9);
					        Select selectStatement = (Select) stmt;
					        SubSelect subSelect = new SubSelect();
					        subSelect.setSelectBody((PlainSelect) selectStatement.getSelectBody());
					        subSelect.accept((ExpressionVisitor)this);
					        if(right != null) right.accept(this);
					        else stack.pop();
					} catch (JSQLParserException e) {
					        //System.out.println("Could not parse Subquery from function: "+function);
					        //e.printStackTrace();
					}

				default:
					if(parent!=null) {
						stack.push(null);
						stack.push(null);		
						right.accept(this);	//to preserve possible following subqueries
						stack.pop();
					}
					stack.push(null);
			}
			//Aggregate functions:
		//} else if(name.equalsIgnoreCase("sum")) { //Always true as long as no other constraints on the column		, change if you want to catch more types later
		//} else if(name.equalsIgnoreCase("avg")) { //Always true as long as no other constraints on the column		, change if you want to catch more types later
		//} else if(name.equalsIgnoreCase("count")) { //Always true as long as no other constraints on the column	, change if you want to catch more types later
		} else if(name.equalsIgnoreCase("min")) {//if the aggregate function is a MIN, we have to handle the different cases
			if(parent instanceof MinorThan || parent instanceof MinorThanEquals || parent instanceof NotEqualsTo) { //here it becomes TRUE
				stack.push(new BooleanValue(true));
				stack.push(new BooleanValue(true));
				right.accept(this);	//to preserve possible following subqueries
				stack.pop();
				stack.push(new BooleanValue(true));
			} else if(parent instanceof EqualsTo || parent instanceof GreaterThanEquals) {//here it becomes parameter >= x
				stack.push(new GreaterThanEquals());
				stack.push(function.getParameters().getExpressions().get(0));
				stack.push(right);
			} else if(parent instanceof GreaterThan){//here it becomes parameter > x
				stack.push(new GreaterThan());
				stack.push(function.getParameters().getExpressions().get(0));
				stack.push(right);				
			}
		} else if(name.equalsIgnoreCase("max")) {
			if(parent instanceof GreaterThan || parent instanceof GreaterThanEquals || parent instanceof NotEqualsTo) {//TRUE
				stack.push(new BooleanValue(true));
				stack.push(new BooleanValue(true));
				right.accept(this);
				stack.pop();
				stack.push(new BooleanValue(true));
			} else if(parent instanceof EqualsTo || parent instanceof MinorThanEquals) {//here it becomes parameter <= x 
				stack.push(new MinorThanEquals());
				stack.push(function.getParameters().getExpressions().get(0));
				stack.push(right);				
			} else if(parent instanceof MinorThan){//here it becomes parameter < x
				stack.push(new MinorThan());
				stack.push(function.getParameters().getExpressions().get(0));
				stack.push(right);
			}			
		} else {
			if(parent!=null) {
				stack.push(new BooleanValue(true));
				stack.push(new BooleanValue(true));
				right.accept(this);	
				stack.pop();
			}
			stack.push(new BooleanValue(true));
		}
	}

	@Override
	public void visit(InverseExpression inverseExpression) { //is a negative value, is just kept as-is
		stack.push(inverseExpression);
		inverseExpression.getExpression().accept(this);
		Expression tempExpression = stack.pop();
		inverseExpression = (InverseExpression)stack.pop();
		inverseExpression.setExpression(tempExpression);
		stack.push(inverseExpression);		
	}

	@Override
	public void visit(JdbcParameter jdbcParameter) { //should not be necessary here
	}

	@Override
	public void visit(JdbcNamedParameter jdbcNamedParameter) { //should not be necessary here
	}

	@Override
	public void visit(DoubleValue doubleValue) {
		stack.push(doubleValue);		
	}

	@Override
	public void visit(LongValue longValue) {
		stack.push(longValue);		
	}

	@Override
	public void visit(HexValue hexValue) {
		stack.push(hexValue);		
	}

	@Override
	public void visit(DateValue dateValue) {
		stack.push(dateValue);
	}

	@Override
	public void visit(TimeValue timeValue) {
		stack.push(timeValue);		
	}

	@Override
	public void visit(TimestampValue timestampValue) {
		stack.push(timestampValue);		
	}

	@Override
	public void visit(Parenthesis parenthesis) {//we proceed to the element inside the parenthesis and remove the parenthesis if the parent is an AND and the element inside is not an OR expression
		stack.push(parenthesis);
		Expression tempExpression = parenthesis.getExpression();
		if(parenthesis.isNot()) ((BinaryExpression)tempExpression).toggleNot();
		tempExpression.accept(this);
		tempExpression = stack.pop();
		parenthesis = (Parenthesis)stack.pop();
		if(stack.isEmpty()) stack.push(tempExpression);
		else if(((stack.peek() instanceof AndExpression) && !(tempExpression instanceof OrExpression)) || tempExpression == null || tempExpression instanceof BooleanValue) stack.push(tempExpression);
		else {
			parenthesis.setNot(false);
			parenthesis.setExpression(tempExpression);
			stack.push(parenthesis);
		}
	}

	@Override
	public void visit(StringValue stringValue) {
		stack.push(stringValue);
	}

	@Override
	public void visit(Addition addition) {
		stack.push(new Addition());
		visitBinaryExpression(addition);
	}

	@Override
	public void visit(Division division) {
		stack.push(new Division());
		visitBinaryExpression(division);
		
	}

	@Override
	public void visit(Multiplication multiplication) {
		stack.push(new Multiplication());
		visitBinaryExpression(multiplication);
		
	}

	@Override
	public void visit(Subtraction subtraction) {
		stack.push(new Subtraction());
		visitBinaryExpression(subtraction);
	}

	@Override
	public void visit(AndExpression andExpression) {
		if(andExpression.isNot()){//process the NOT
			OrExpression orExpression = new OrExpression(null,null);
			orExpression.setNot();
			stack.push(orExpression);
		} else {		
			stack.push(new AndExpression(null,null));
		}
		visitBinaryExpression(andExpression);
		
	}

	@Override
	public void visit(OrExpression orExpression) {
		if(orExpression.isNot()){//process the NOT
			AndExpression andExpression = new AndExpression(null,null);
			andExpression.setNot();
			stack.push(andExpression);
		} else {
			stack.push(new OrExpression(null,null));	
		}		
		visitBinaryExpression(orExpression);	
	}

	@Override
	public void visit(Between between) {//For the BETWEEN we need two new predicates combined by AND or OR
		if(between.isNot()){//greater than and minorthan, connected by OR
			Parenthesis parenthesis = new Parenthesis();
			GreaterThan greaterThan = new GreaterThan();
			greaterThan.setLeftExpression(between.getLeftExpression());
			greaterThan.setRightExpression(between.getBetweenExpressionEnd());
			MinorThan minorThan = new MinorThan();
			minorThan.setLeftExpression(between.getLeftExpression());
			minorThan.setRightExpression(between.getBetweenExpressionStart());
			
			OrExpression binaryExpression = new OrExpression(greaterThan,minorThan);
			parenthesis.setExpression(binaryExpression);
			stack.push(parenthesis);			
		} else {//greaterthanequals and minorthanequals, connected by AND
			GreaterThanEquals greaterThanEquals = new GreaterThanEquals();
			greaterThanEquals.setLeftExpression(between.getLeftExpression());
			greaterThanEquals.setRightExpression(between.getBetweenExpressionStart());
			MinorThanEquals minorThanEquals = new MinorThanEquals();
			minorThanEquals.setLeftExpression(between.getLeftExpression());
			minorThanEquals.setRightExpression(between.getBetweenExpressionEnd());
		
			AndExpression binaryExpression = new AndExpression(greaterThanEquals,minorThanEquals);	
			stack.push(binaryExpression);			
		}		
	}

	@Override
	public void visit(EqualsTo equalsTo) {
		if(equalsTo.isNot())stack.push(new NotEqualsTo());//if NOT it becomes notequalsto
		else stack.push(new EqualsTo());
		visitBinaryExpression(equalsTo);	
	}

	@Override
	public void visit(GreaterThan greaterThan) {
		//System.out.println(greaterThan);
		if(greaterThan.isNot())stack.push(new MinorThanEquals());//if NOT becomes minorthanequals
		else stack.push(new GreaterThan());
		visitBinaryExpression(greaterThan);			
	}

	@Override
	public void visit(GreaterThanEquals greaterThanEquals) {
		if(greaterThanEquals.isNot()) stack.push(new MinorThan());//if NOT becomes minorthan
		else stack.push(new GreaterThanEquals());
		visitBinaryExpression(greaterThanEquals);	
	}

	@Override
	public void visit(InExpression inExpression) { //two types, list or subquery, is list in sdss server often? NEVER since it is no MS SQL syntax. but anyway we catch it
		//need to extract the access area from the subquery and transform the rest according to our theory
		SQLParser parser = new SQLParser();
		if(inExpression.getRightItemsList() instanceof SubSelect){//Subquery
			try {//get access area of subquery
				PlainSelect tempSubSelect = (PlainSelect)((SubSelect)inExpression.getRightItemsList()).getSelectBody();
				AccessArea tempAccessArea = parser.parseStmt(tempSubSelect.toString());
				BinaryExpression tempEqualsTo;
				if(inExpression.isNot()) {//if NOT IN its a notquealsto
					tempEqualsTo = new NotEqualsTo();
				} else { 
					tempEqualsTo = new EqualsTo();
				}//left side of the expression stays the sime, right side is the attribute from the SELECT clause of the subquery
				tempEqualsTo.setLeftExpression(inExpression.getLeftExpression());
				tempEqualsTo.setRightExpression((Expression)((SelectExpressionItem)tempSubSelect.getSelectItems().get(0)).getExpression());
				
				//Merge the access areas from and where part
				if(tempAccessArea.getFrom() != null) {
					List<FromItem> tempFromItemList = accessArea.getFrom();
					tempFromItemList.addAll(tempAccessArea.getFrom());
					accessArea.setFrom(tempFromItemList);
				}
				if(tempAccessArea.getWhere() != null){
					AndExpression tempAndExpression = new AndExpression(null,null);
					tempAndExpression.setLeftExpression(tempEqualsTo);
					tempAndExpression.setRightExpression(tempAccessArea.getWhere());
					stack.push(tempAndExpression);				
				} else stack.push(tempEqualsTo);
			} catch (JSQLParserException e) {
				//System.out.println("Error: Could not parse SubSelect of IN Statement:" + inExpression);
				//e.printStackTrace();
			}
		} else {
			if(inExpression.getRightItemsList() != null) {//If right side is a list... create the predicates and connect by AND
				ExpressionList rightItemsList = (ExpressionList)inExpression.getRightItemsList();
				List<Expression> expressions = rightItemsList.getExpressions();
				
				ListIterator<Expression> it = expressions.listIterator();
				Expression expression = it.next();
				BinaryExpression tempEqualsTo;
				if(inExpression.isNot()) {//if not always notequalsto
					tempEqualsTo = new NotEqualsTo();
				} else {
					tempEqualsTo = new EqualsTo();
				}
				tempEqualsTo.setLeftExpression(inExpression.getLeftExpression());
				tempEqualsTo.setRightExpression(expression);
				if(it.hasNext()) {//iterate through the list
					expression = it.next();
					BinaryExpression tempBinExpression;
					BinaryExpression tempEqualsTo2;
					if(inExpression.isNot()) {
						tempEqualsTo2 = new NotEqualsTo();
						tempBinExpression = new AndExpression(null,null);
					} else {
						tempEqualsTo2 = new EqualsTo();
						tempBinExpression = new OrExpression(null,null);
					}
					tempBinExpression.setLeftExpression(tempEqualsTo);
					tempEqualsTo2.setLeftExpression(inExpression.getLeftExpression());
					tempEqualsTo2.setRightExpression(expression);
					tempBinExpression.setRightExpression(tempEqualsTo2);										
					while (it.hasNext()) {
						expression = it.next();
						BinaryExpression tempBinExpression2;
						BinaryExpression tempEqualsTo3;
						if(inExpression.isNot()) {
							tempEqualsTo3 = new NotEqualsTo();
							tempBinExpression2 = new AndExpression(null,null);
						} else {
							tempEqualsTo3 = new EqualsTo();
							tempBinExpression2 = new OrExpression(null,null);
						}
						tempBinExpression2.setLeftExpression(tempBinExpression);
						tempEqualsTo3.setLeftExpression(inExpression.getLeftExpression());
						tempEqualsTo3.setRightExpression(expression);
						tempBinExpression2.setRightExpression(tempEqualsTo3);
						tempBinExpression = tempBinExpression2;
					}
					Parenthesis parenthesis = new Parenthesis(tempBinExpression);//parethisis around it all
					stack.push(parenthesis);
				} else stack.push(tempEqualsTo);
			}
		}
	}

	@Override
	public void visit(IsNullExpression isNullExpression) {		
		stack.push(isNullExpression);			
	}

	@Override
	public void visit(LikeExpression likeExpression) {
		LikeExpression tempLike = new LikeExpression();
		if(likeExpression.isNot()) tempLike.setNot(true);
		if(likeExpression.getEscape() != null) tempLike.setEscape(likeExpression.getEscape());
		stack.push(tempLike);
		visitBinaryExpression(likeExpression);	
	}

	@Override
	public void visit(MinorThan minorThan) {
		if(minorThan.isNot()) stack.push(new GreaterThanEquals());//if not becomes greaterthanequals
		else stack.push(new MinorThan());		
		visitBinaryExpression(minorThan);		
	}

	@Override
	public void visit(MinorThanEquals minorThanEquals) {
		if(minorThanEquals.isNot()) stack.push(new GreaterThan());		//if NOT becomes greaterthan
		else stack.push(new MinorThanEquals());		
		visitBinaryExpression(minorThanEquals);	
	}

	@Override
	public void visit(NotEqualsTo notEqualsTo) {
		if(notEqualsTo.isNot()) stack.push(new EqualsTo());		//if NOT becomes equalsto
		else stack.push(new NotEqualsTo());		
		visitBinaryExpression(notEqualsTo);	
	}

	@Override
	public void visit(Column tableColumn) {
		stack.push(tableColumn);
	}

	@Override
	public void visit(CaseExpression caseExpression) {//only occurs mostly in select clause in our SDSS share, now information about cases
		stack.push(caseExpression);		
	}

	@Override
	public void visit(WhenClause whenClause) { //27 in SDSS: mostly in select part, nearly always as part of case expression
		 stack.push(whenClause);
	}

	@Override
	public void visit(ExistsExpression existsExpression) {
		if(existsExpression.isNot()) {//NOTEXISTS is always transformed to TRUE
			if(existsExpression.getRightExpression() instanceof SubSelect) {
				stack.push(new BooleanValue(true));
			} else stack.push(new BooleanValue(true));
		} else {
			if(existsExpression.getRightExpression() instanceof SubSelect) {//like with IN expression. extract access area of subquery and merge the access areas
				//here of course the EXISTS is not replaced by any new predicate except for those in the subquery
				SQLParser parser = new SQLParser();
				PlainSelect tempSubSelect = (PlainSelect)((SubSelect)existsExpression.getRightExpression()).getSelectBody();
				try {
					AccessArea tempAccessArea = parser.parseStmt(tempSubSelect.toString());
					if(tempAccessArea.getFrom() != null) {
						List<FromItem> tempFromItemList = accessArea.getFrom();
						tempFromItemList.addAll(tempAccessArea.getFrom());
						accessArea.setFrom(tempFromItemList);
					}
					if(tempAccessArea.getWhere() != null){
						stack.push(tempAccessArea.getWhere());				
					} else stack.push(new BooleanValue(true));
				} catch (JSQLParserException e) {
					//System.out.println("Error: Could not parse SubSelect of EXISTS Statement:" + existsExpression);
					//e.printStackTrace();
				}
			} else stack.push(new BooleanValue(true));
		}
	}

	@Override
	public void visit(AllComparisonExpression allComparisonExpression) {//ALL is converted to ANY and then we go on there
		Expression left = stack.pop();
		Expression parent = stack.pop();
		Expression tempExpression;
		if(parent instanceof MinorThanEquals) tempExpression = new GreaterThan();
		else if(parent instanceof MinorThan) tempExpression = new GreaterThanEquals();
		else if(parent instanceof GreaterThanEquals) tempExpression = new MinorThan();
		else if(parent instanceof GreaterThan) tempExpression = new MinorThanEquals();
		else if(parent instanceof EqualsTo) tempExpression = new NotEqualsTo();
		else if(parent instanceof NotEqualsTo) tempExpression = new EqualsTo();
		else tempExpression = parent;
		if(((BinaryExpression)parent).isNot()) ((BinaryExpression)tempExpression).setNot(false);//catch NOT
		else ((BinaryExpression)tempExpression).setNot(true);
		AnyComparisonExpression tempAnyComparisonExpression = new AnyComparisonExpression(allComparisonExpression.getSubSelect());
		
		stack.push(tempExpression);
		stack.push(left);

		
		tempAnyComparisonExpression.accept(this);
	}

	@Override
	public void visit(AnyComparisonExpression anyComparisonExpression) {//we get the access area of the subquery and merge it with the outer one
		SQLParser parser = new SQLParser();
		if(anyComparisonExpression.getSubSelect() instanceof SubSelect){
			try {
				PlainSelect tempSubSelect = (PlainSelect)(anyComparisonExpression.getSubSelect()).getSelectBody();
				AccessArea tempAccessArea = parser.parseStmt(tempSubSelect.toString());
				Expression tempExpression = (Expression)((SelectExpressionItem)tempSubSelect.getSelectItems().get(0)).getExpression();
				
				if(tempAccessArea.getFrom() != null) {
					List<FromItem> tempFromItemList = accessArea.getFrom();
					tempFromItemList.addAll(tempAccessArea.getFrom());
					accessArea.setFrom(tempFromItemList);
				}
				
				if(tempAccessArea.getWhere() != null && !(tempAccessArea.getWhere() instanceof BooleanValue)){
					AndExpression tempAndExpression = new AndExpression(null,null);
					tempAndExpression.setLeftExpression(tempExpression);
					tempAndExpression.setRightExpression(tempAccessArea.getWhere());
					stack.push(tempAndExpression);				
				} else stack.push(tempExpression);
			} catch (JSQLParserException e) {
				//System.out.println("Error: Could not parse SubSelect of ANY Statement:" + anyComparisonExpression);
				//e.printStackTrace();
			}
		}
	}

	@Override
	public void visit(Concat concat) {
		stack.push(new Concat());		
		visitBinaryExpression(concat);	
	}

	@Override
	public void visit(Matches matches) { //only seems to occur in comments and names
		stack.push(matches);		
	}

	@Override
	public void visit(BitwiseAnd bitwiseAnd) {
		stack.push(new BitwiseAnd());
		visitBinaryExpression(bitwiseAnd);
	}

	@Override
	public void visit(BitwiseOr bitwiseOr) {
		stack.push(new BitwiseOr());
		visitBinaryExpression(bitwiseOr);
	}

	@Override
	public void visit(BitwiseXor bitwiseXor) {
		stack.push(new BitwiseXor());
		visitBinaryExpression(bitwiseXor);
	}

	@Override
	public void visit(CastExpression cast) {
		stack.push(cast.getLeftExpression());
	}

	@Override
	public void visit(Modulo modulo) {	
		stack.push(new Modulo());		
		visitBinaryExpression(modulo);	
	}

	@Override
	public void visit(AnalyticExpression aexpr) {//should not occur here?
		//System.out.println("Test");
		stack.push(aexpr);
	}

	@Override
	public void visit(ExtractExpression eexpr) { //occurs zero times
	}

	@Override
	public void visit(IntervalExpression iexpr) { //Not in MS SQL?
	}

	@Override
	public void visit(Table tableName) { //Not necessary here as TableName is no Expression
	}

	@Override
	public void visit(SubSelect subSelect) { // should be handled already somewhere else (EXISTS, IN, ALL, ANY,...) but could occur in WHERE part anyway
		//we extract access area and save in subwhere. merge it with the outer access area later. at the current spot, the subquery is replaced by the attribute of the SELECT clause of the subquery
			SQLParser parser = new SQLParser();
			try {
				PlainSelect tempSubSelect = (PlainSelect)subSelect.getSelectBody();
				AccessArea tempAccessArea = parser.parseStmt(tempSubSelect.toString());
				Expression tempExpression;
				if((tempSubSelect.getSelectItems().get(0)) instanceof AllColumns) {
					tempExpression = new BooleanValue(true);
				} else {
					tempExpression = (Expression)((SelectExpressionItem)tempSubSelect.getSelectItems().get(0)).getExpression();
				}
				
				if(tempAccessArea.getFrom() != null) {
					List<FromItem> tempFromItemList = accessArea.getFrom();
					tempFromItemList.addAll(tempAccessArea.getFrom());
					accessArea.setFrom(tempFromItemList);
				}
				
				if(tempAccessArea.getWhere() != null){
					if(subWhere != null){
						AndExpression tempAndExpression = new AndExpression(null,null);
						if(subWhere instanceof Parenthesis) tempAndExpression.setLeftExpression(subWhere);
						else { 
							Parenthesis tempParenthesis = new Parenthesis();
							tempParenthesis.setExpression(subWhere);
							tempAndExpression.setLeftExpression(tempParenthesis);
						}
						tempAndExpression.setRightExpression(tempAccessArea.getWhere());
						subWhere = tempAndExpression;
					} else subWhere = tempAccessArea.getWhere();
				}
				stack.push(tempExpression);
			} catch (JSQLParserException e) {
				//System.out.println("Error: Could not parse SubSelect of SubSelect:" + subSelect);
				//e.printStackTrace();
			}
	}

	@Override
	public void visit(SubJoin subjoin) { //should already be handled elsewhere
	}

	@Override
	public void visit(LateralSubSelect lateralSubSelect) { //should already be handled elsewhere
	}

	@Override
	public void visit(ValuesList valuesList) { //should already be handled elsewhere
	}

	@Override
	public void visit(AllColumns allColumns) { //should already be handled elsewhere
	}

	@Override
	public void visit(AllTableColumns allTableColumns) { //should already be handled elsewhere
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) { //should already be handled elsewhere
		//selectExpressionItem.getExpression().accept(this);
	}

	public ExpressionVisitor getExpressionVisitor() {
		return expressionVisitor;
	}

	public void setExpressionVisitor(ExpressionVisitor visitor) {
		expressionVisitor = visitor;
	}

	@Override
	public void visit(PlainSelect plainSelect) { //should already be handled elsewhere
		
	}

	@Override
	public void visit(SetOperationList setOpList) { //should already be handled elsewhere
	}

	@Override
	public void visit(WithItem withItem) { //occurs only in FROM part, only rarely there and if handled it will be there, but there most are errors
	}

	@Override
	public void visit(BooleanValue booleanValue) {
		stack.push(booleanValue);
	}

	@Override
	public void visit(FunctionItem functionItem) { //should only occur in subquery and these are already handled
		functionItem.getFunction().accept(this);
	}
}
