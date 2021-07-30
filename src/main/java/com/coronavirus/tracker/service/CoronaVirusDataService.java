package com.coronavirus.tracker.service;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.coronavirus.tracker.model.LocationStats;

@Service
public class CoronaVirusDataService {
	
	@Value("${VIRUS_DATA_URL}")
	private String virusDataUrl;
	
	private List<LocationStats> allStats = new ArrayList<>();
	
	public List<LocationStats> getAllStats() {
		return allStats;
	}

	@PostConstruct
	@Scheduled(cron = "* * 1 * * *")
	public void fetchVirusData() {
		
		try
		{
			List<LocationStats> newStats = new ArrayList<>();
			
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(virusDataUrl))
					.build();
			
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			
			StringReader reader = new StringReader(response.body());
			Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
			for (CSVRecord record : records) {
			    LocationStats locationStats = new LocationStats();
			    locationStats.setState(record.get("Province/State"));
			    locationStats.setCountry(record.get("Country/Region"));
			    int latestCases = Integer.parseInt(record.get(record.size() - 1));
			    int prevDayCases = Integer.parseInt(record.get(record.size() - 2));
			    locationStats.setLatestTotalCase(latestCases);
			    locationStats.setDiffFromPrivDay(latestCases - prevDayCases);
			    newStats.add(locationStats);
			}
			this.allStats = newStats;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}