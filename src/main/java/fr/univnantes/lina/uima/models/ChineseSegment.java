package fr.univnantes.lina.uima.models;

import java.util.Set;

import org.apache.uima.resource.SharedResourceObject;

public interface ChineseSegment extends SharedResourceObject {

	public Set<String> get();
	
}
