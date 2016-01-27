package fr.univnantes.lina.uima.engines;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.ResourceInitializationException;

import fr.univnantes.lina.uima.models.ChineseSegment;

public class ChineseSegmenter extends JCasAnnotator_ImplBase {

	public static final String CHINESE_WORD_SEGMENTS = "ChineseWordSegments";
	@ExternalResource(key = CHINESE_WORD_SEGMENTS, mandatory = true)
	private ChineseSegment chineseWordSegments;
	
	public static final String CHINESE_FOREIGN_NAME_SEGMENTS = "ChineseForeignNameSegments";
	@ExternalResource(key = CHINESE_FOREIGN_NAME_SEGMENTS, mandatory = true)
	private ChineseSegment chineseForeignNameSegments;
	
	public static final String CHINESE_NUMBER_SEGMENTS = "ChineseNumberSegments";
	@ExternalResource(key = CHINESE_NUMBER_SEGMENTS, mandatory = true)
	private ChineseSegment chineseNumberSegments;

	public static final String ANNOTATION_TYPE = "AnnotationType";
	@ConfigurationParameter(mandatory = true, name=ANNOTATION_TYPE)
	private String annotationType;
	
	private boolean contains(ChineseSegment segments,String segment) {
		boolean result = true;
		for (int i = 0; i < segment.length(); i++) {
		    if (segments.get().contains(segment.substring(i, i+1)) == false) {
			result = false;
			break;
		    }
		}
		return result;
	}
	
	private Type getAnnotationType(JCas cas) {
		return cas.getTypeSystem().getType(this.annotationType);
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		try {
			this.chineseWordSegments = (ChineseSegment) context.getResourceObject(CHINESE_WORD_SEGMENTS);
			this.chineseForeignNameSegments = (ChineseSegment) context.getResourceObject(CHINESE_FOREIGN_NAME_SEGMENTS);
			this.chineseNumberSegments = (ChineseSegment) context.getResourceObject(CHINESE_NUMBER_SEGMENTS);
			this.annotationType = (String) context.getConfigParameterValue(ANNOTATION_TYPE);
		} catch (ResourceAccessException e) {
			throw new ResourceInitializationException(e);
		}
	}
	
	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
		Type type = this.getAnnotationType(cas);
		String text = cas.getDocumentText();
		int len = text.length();
		int[] boundaries = new int[len];
		this.split(text, boundaries, len);
		this.merge(this.chineseForeignNameSegments, text, boundaries, len);
		this.merge(this.chineseNumberSegments, text, boundaries, len);
		for (int i = 0; i < boundaries.length; i++) {
		    if (boundaries[i] > 0 && !this.areSpaces(text, i, i + boundaries[i])) {
		    	this.annotate(cas, type, i, i + boundaries[i]);
		    }
		}
	}
		
	private boolean areSpaces(String text, int begin, int end) {
		boolean space = true;
		for (int index = begin; index < end; index++) {
			char current = text.charAt(index);
			space = space && Character.isWhitespace(current);			
		}
		return space;
	}
	
	private void annotate(JCas cas,Type type,int begin,int end) {
		AnnotationFS annotation = cas.getCas().createAnnotation(type, begin, end);
		cas.getCas().addFsToIndexes(annotation);
	}
	
	private void split(String text,int[] offsets,int len) {
		int i = 0;
		int j = 0;
		while (i < len) {
		    if (Character.UnicodeBlock.of(text.charAt(i)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
		    	j = 8;
		    	if (i + j > len) { 
		    		j = len - i; 
		    	}
		    	while (i + j <= len && j > 1) {
		    		if (this.chineseWordSegments.get().contains(text.substring(i, i + j))) {
		    			break;
		    		}
		    		j--;
		    	}
		    	offsets[i] = j;
		    	i += j;			
		    } else if (Character.isWhitespace(text.charAt(i))) {
		    	j = 1;
		    	while (i+j < len && Character.isWhitespace(text.charAt(i + j))) {
		    		j++;
		    	}
		    	offsets[i] = j;
		    	i += j;
		    } else if (Character.isLetter(text.charAt(i))) {
		    	j = 1;
		    	while (i + j < len && Character.isLetter(text.charAt(i + j))) {
		    		j++;
		    	}
		    	offsets[i] = j;
		    	i += j;
		    } else if (Character.isDigit(text.charAt(i))) {
		    	j = 1;
		    	while (i + j < len && Character.isDigit(text.charAt(i + j))) {
		    		j++;
		    	}
		    	offsets[i] = j;
		    	i += j;
		    } else {
		    	offsets[i] = 1;
		    	i++;
		    }
		}
	}
	
	private void merge(ChineseSegment segments, String text,int[] offsets,int len) {
		int i = 0;
		while (i < len) {
			if (offsets[i] > 0 ) {
				while (i+offsets[i] < len 
						&& i+offsets[i]+offsets[i+offsets[i]] < len 
						&& this.contains(segments, text.substring(i, i + offsets[i] + offsets[i + offsets[i]]))) {
					int k = offsets[i + offsets[i]];
					offsets[i + offsets[i]] = 0;
					offsets[i] = offsets[i] + k;
				}
			}
			i++;
		}
	}	
		
}
