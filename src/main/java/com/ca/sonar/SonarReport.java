package com.ca.sonar;

/**import org.sonar.wsclient.Host;
import org.sonar.wsclient.Sonar;
import org.sonar.wsclient.connectors.HttpClient4Connector;
import org.sonar.wsclient.services.*;*/
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.WsMeasures;
import org.sonarqube.ws.client.HttpConnector;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;
import org.sonarqube.ws.client.measure.SearchRequest;
import org.sonar.wsclient.services.Measure;
import org.sonar.wsclient.services.Resource;
import org.sonar.wsclient.services.ResourceQuery;
import org.sonar.wsclient.Sonar;

import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SonarReport {

  /**public static void main(String args[]) {
    String url = "https://sonarqube-isl-dev-01.ca.com";
    String login = "mahdi02";
    String password = "mahesH123$";
    Sonar sonar = new Sonar(new HttpClient4Connector(new Host(url, login, password)));

    String projectKey = "infrabot";
    String manualMetricKey = "burned_budget";

    sonar.create(MetricQuery.create(projectKey, manualMetricKey).setValue(50.0));

    for (ManualMeasure manualMeasure : sonar.findAll(ManualMeasureQuery.create(projectKey))) {
      System.out.println("Manual measure on project: " + manualMeasure);
    }
  }*/
  
  /**public static void main(String[] args) {
		String resource = "infrabot:infrabot";
		String metric = "ncloc";
		String host="https://sonarqube-isl-dev-01.ca.com";
		String userName="mahdi02";
		String password="mahesH123#";
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		Date from = null;
		Date to = null;
		try {
			from = df.parse("21/12/2017");
			to = df.parse("10/05/2018");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//getCurrentValue(resource, metric);
		new RetrieveStats().getHistoricValues(host,userName,password,resource, metric, from, to);
	}**/
	
	 /**
     * Main method..
     *
     * @param args
     */
    public static void main(String... args) {
		
		String login = "";
		String password = "";
		String url = "";
        // execute search Issues 
        HttpConnector httpConnector = HttpConnector.newBuilder().url(url).credentials(login, password).token("80986c14aca7bf6062abfc6329e8a11ba9383128").build();
        WsClient wsClient = WsClientFactories.getDefault().newClient(httpConnector);
        SearchWsRequest searchWsRequest = new SearchWsRequest();
		List componentKeys = new ArrayList();
		componentKeys.add("infrabot");
		searchWsRequest.setComponentKeys(componentKeys);
		List statuses = new ArrayList();
		statuses.add("OPEN");
		searchWsRequest.setStatuses(statuses);
		List severities = new ArrayList();
		severities.add("BLOCKER");
		severities.add("CRITICAL");
		severities.add("MAJOR");
		severities.add("MINOR");		
		searchWsRequest.setSeverities(severities);
		List types = new ArrayList();
		types.add("CODE_SMELL");
		types.add("BUG");
		types.add("VULNERABILITY");
		searchWsRequest.setTypes(types);
		searchWsRequest.setPageSize(1000);	
        Issues.SearchWsResponse response = wsClient.issues().search(searchWsRequest);
        
        // STD Out Paging Object
        //Common.Paging paging = response.getPaging();
        //.out.println("------------------------------------------------------------");
       // System.out.println(ToStringBuilder.reflectionToString(paging, ToStringStyle.JSON_STYLE));
        //System.out.println("------------------------------------------------------------");

        // STD Out issue
        List<Issues.Issue> issuesList = response.getIssuesList();
		//int i = 0;
        /**for (Issues.Issue issue : issuesList) {
            System.out.println(issue.project);
          //  System.out.println(ToStringBuilder.reflectionToString(issue, ToStringStyle.JSON_STYLE));
            System.out.println("------------------------------------------------------------");
        }**/
		//String json = new Gson().toJson(issuesList);
		//System.out.println(json.toString());
		try {
			int major = 0;
			int minor = 0;
			int critical = 0;
			int blocker = 0;
		for (int i = 0; i < issuesList.size(); i++) {
			

			 BufferedReader stdin = new BufferedReader(new StringReader(issuesList.get(i).toString())); 
        String line; 

        while ((line = stdin.readLine()) != null && line.length()!= 0) { 
            String[] input = line.split(":"); 
            if (input.length == 2) { 
                //System.out.println(input[0] + " === " + input[1]);
				//if(input[0].toString() == "severity" && input[1].toString() == "MAJOR") {
					//System.out.println(input[0] + " === " + input[1]);
					//System.out.println("------------------------------------------------------------");
				//}
				
				if (line.contains("MAJOR")) {
					major++;
				}
				if (line.contains("MINOR")) {
					minor++;
				}
				if (line.contains("CRITICAL")) {
					critical++;
				}
				if (line.contains("BLOCKER")) {
					blocker++;
				}
            } 
        } 
			
			
			//System.out.println("------------------------------------------------------------");
		}
		System.out.println("Minor Count : " + minor);
		System.out.println("Major Count : " + major);
		System.out.println("CRITICAL Count : " + critical);
		System.out.println("BLOCKER Count : " + blocker);
		System.out.println(blocker+critical+major+minor);
		
			/**url = url + "/api/measures/search_history?component=infrabot&metrics=coverage";
			Process p = Runtime.getRuntime().exec("curl -u " + "80986c14aca7bf6062abfc6329e8a11ba9383128" + ": " + url);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String s = null;
            while ((s = stdInput.readLine()) != null) { 
				System.out.println(s);
				System.out.println("---------------------------------------------");
			}**/
			/**SearchRequest searchRequest = new SearchRequest (HttpConnector.newBuilder());
			List metrics = new ArrayList();
			metrics.add("coverage");
			searchRequest.setMetricKeys(metrics);
			WsMeasures.SearchHistoryResponse historyResponse = wsClient. measures().search(searchRequest);**/	
			/**Sonar sonar = Sonar.create(url,login, password);			
			ResourceQuery query = ResourceQuery.createForMetrics("infrabot:infrabot", "coverage", "lines", "violations");
			System.out.println(query.getUrl());
			Resource res = sonar.find(query);
			System.out.println(res);
			System.out.println(res.getMeasure("coverage"));**/
			
			int totalissues=blocker+critical+major+minor;
			Properties properties = new Properties();
			properties.setProperty("MINOR", minor+"");
			properties.setProperty("MAJOR", major+"");
			properties.setProperty("CRITICAL", critical+"");
			properties.setProperty("BLOCKER", blocker+"");
			properties.setProperty("COVERAGE", "0");
			properties.setProperty("TOTAL", totalissues+"");

			File file = new File("sonarreport.properties");
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "Sonar Report");
			fileOut.close();
	} catch(Exception e) {
		e.printStackTrace();
	}


    }


}
