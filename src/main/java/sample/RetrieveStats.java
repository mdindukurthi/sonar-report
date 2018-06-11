/**
 * @author Marc Oriol, Mirko Morandini
 */

package sample;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.text.SimpleDateFormat;

import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.services.TimeMachine;
import org.sonar.wsclient.services.TimeMachineCell;
import org.sonar.wsclient.services.TimeMachineQuery;

public class RetrieveStats {

	
	/**
	 * Get Sonar values for a project, per class
	 * @param host Sonar host
	 * @param resourceKey the "project" to analyse
	 * @param metricKey the metric to retrieve
	 * @return
	 */
	public static ArrayList<Double> getValuesPerClass(String host, String resourceKey, String metricKey){
		ArrayList<Double> values = new ArrayList<Double>();
		Sonar sonar = Sonar.create(host);
		ResourceQuery query  = new ResourceQuery(resourceKey);
		query.setMetrics(metricKey.trim());
		query.setScopes("FIL");
		query.setDepth(-1);
		for (Resource resource : sonar.findAll(query)){
		   Measure m =resource.getMeasure(metricKey);
		   if(m!=null) values.add(m.getValue());
		}
		return values;
	}
	
	/**
	 * Calculates from a list of numbers a distribution (sum=1) of numbers in the ranges defined by distributionValues
	 * @param values e.g. output of getValuesPerClass()
	 * @param distributionValues values that delimit ranges. -inf and +inf are added automatically.
	 * @return distribution array with size = distributionValues.size+1
	 */
	public static Double[] getDistribution(ArrayList<Double> values, ArrayList<Double> distributionValues){
		int[] countDistribution = new int[distributionValues.size()+1];
		
		for (Double value: values){
			boolean valueInserted =false;
			Iterator<Double> it = distributionValues.iterator();
			int index = 0;
			
			while(!valueInserted){
				if(it.hasNext()){
					Double distributionValue = it.next();
					if( value.doubleValue() < distributionValue.doubleValue()){
						countDistribution[index]++;
						valueInserted=true;
					}else{
						index++;
					}
				}else{ //last value to +infinite
					countDistribution[index]++;
					valueInserted=true;
				}
			}
		}
		Double[] distribution = new Double[countDistribution.length];
		for (int i = 0; i < distribution.length; i ++){
			distribution[i] = new Double((double) countDistribution[i] / values.size());
		}
		return distribution;
	}
	
	/**
	 * Gets Sonar values for a project, per class, and calculates the desired distribution
	 * @param host
	 * @param resourceKey
	 * @param metricKey
	 * @param distributionValues
	 * @return
	 */
	public static Double[] getDistributionOfClassMeasures(String host, String resourceKey, String metricKey, ArrayList<Double> distributionValues){
		return getDistribution(getValuesPerClass(host, resourceKey, metricKey), distributionValues);
	}
	

	/**
	 * Retrieves a single value by a Sonar request.
	 * @param host Sonar host
	 * @param resourceKey the "project" to analyse
	 * @param metricKey the metric to retrieve
	 * @param stats
	 * @return true if metric value found and put to stats
	 */
	public static boolean getStoreCurrentValue(String host, String resourceKey, String metricKey, SonarStatistics stats){
		Sonar sonar = Sonar.create(host);
		ResourceQuery query = ResourceQuery.createForMetrics(resourceKey, metricKey);
		Resource resource = null;
		try {
			resource = sonar.find(query);
		} catch (org.sonar.wsclient.connectors.ConnectionException e) {
			System.err.println("ConnectionException: Not able to connect to server "+host);
		}
		Measure measure;
		if (resource != null) {
			if ((measure = resource.getMeasure(metricKey)) != null) {
//				System.out.println(measure);
				stats.singleValues.put(metricKey, measure.getValue());
				return true;
//				return measure.getValue();
			}
		}
		System.err.println("Warning[getCurrentValue]: Measure \""+ metricKey +"\" is empty." );
		return false;  //return 0.0;
	}
	
	/**
	 * returns history values retrieved from the Sonar timeMachine
	 * @param host Sonar host
	 * @param resourceKey the "project" to analyse
	 * @param metricKey the metric to retrieve
	 * @param from starting date
	 * @param to ending date
	 * @return a list of values retrieved from the Sonar timeMachine in this interval. The frequency depends on the Sonar installation. Returns NULL if no value found.
	 */

	public static ArrayList<Double> getHistoricValues(String host,String userName, String password ,String resourceKey, String metricKey, Date from, Date to){
		ArrayList<Double> dateValues = new ArrayList<Double>();
		Sonar sonar = Sonar.create(host,userName,password);
		TimeMachineQuery timeQuery = TimeMachineQuery.createForMetrics(resourceKey, metricKey);
		//timeQuery.setFrom(from);
		//timeQuery.setTo(to);
		timeQuery.setMetrics(metricKey);

		TimeMachine timeMachine = null;
		try {
			timeMachine = sonar.find(timeQuery);
		} catch (org.sonar.wsclient.connectors.ConnectionException e) {
			System.err.println("ConnectionException: Not able to connect to server "+host);
		}
		
		if (timeMachine == null) {
			
			System.err.println("Warning[getHistoricValues]: Not able to retrieve TimeMachine.");
			return null;
			
		} else for (TimeMachineCell cell : timeMachine.getCells()) {
			Object o = cell.getValues()[0];
			// System.out.println(o.getClass().getName());
			if (o instanceof Long) {
				dateValues.add(((Long) o).doubleValue());
			} else if (o instanceof Integer) {
				dateValues.add(((Integer) o).doubleValue());
			} else try {
				dateValues.add((Double) cell.getValues()[0]);
			} catch (ClassCastException e) {
				System.err.println("Warning[getHistoricValues]: Value " + cell.getValues()[0]
					+ " is not a number.");
			}
		}
		return dateValues;
	}


	/**public static void main(String[] args) {
		String resource = "org.ow2.bonita:bonita-server";
		String metric = "ncloc";
		String host="https://sonarqube-isl-dev-01.ca.com";
		String userName="mahdi02";
		String password="mahesH123#";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date from = null;
		Date to = null;
		try {
			from = df.parse("21/12/2010");
			to = df.parse("10/06/2014");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//getCurrentValue(resource, metric);
		getHistoricValues(host,userName,password,resource, metric, from, to);
	}*/

}

/*
 * The resourcekey is the identifier for your project in Sonar. You create it when you setup your project in
 * Sonar??? typically it???s of the format domain:projectname. For a given project, you can figure out what it is
 * by going to the project in Sonar then clicking the the little link icon in the upper left then look at th
 * far right of the URL. For example, hsi project in sonar,
 * https://analysis.apache.org/dashboard/index/org.apache.directmemory.server:server has a resourcekey of
 * org.apache.directmemory.server:server
 */