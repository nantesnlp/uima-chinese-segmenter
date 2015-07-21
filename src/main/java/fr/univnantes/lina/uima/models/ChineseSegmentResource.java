package fr.univnantes.lina.uima.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;

public class ChineseSegmentResource implements ChineseSegment {

	private Set<String> segments;
	
	public ChineseSegmentResource() {
		this.segments = new HashSet<String>();
	}
	
	@Override
	public Set<String> get() {
		return this.segments;
	}

	private void load(InputStream inputStream) throws IOException {
		Scanner scanner = new Scanner(inputStream);
		while (scanner.hasNextLine()) {
			String segment = scanner.nextLine();
			if (segment.indexOf("#") > -1 || segment.length() == 0) {
				
			} else {
				this.segments.add(segment);
			}
		}
	}
	
	@Override
	public void load(DataResource data) throws ResourceInitializationException {
		try {
			this.load(data.getInputStream());
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
}
