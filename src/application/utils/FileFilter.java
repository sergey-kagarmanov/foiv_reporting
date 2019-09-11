package application.utils;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 

public class FileFilter implements FilenameFilter{
    
    private String mask;
     
    public FileFilter(String mask){
        this.mask = mask;
        
        this.mask=this.mask.replace("*", "\\w+");        
        this.mask=this.mask.replace("?", "\\w");
        this.mask=this.mask.replace(".", "\\.");
    }
    
    @Override
    public boolean accept(File dir, String name) {  
    	name = name.toLowerCase();
    	Pattern sp=Pattern.compile(mask);
    	String shortName=name;
    	if(shortName.contains("\\")){
    		shortName=shortName.substring(shortName.lastIndexOf("\\")+1);
    	}
    	
    	Matcher matcher = sp.matcher(shortName);
    	boolean f1 = matcher.find();
    	boolean f2 = matcher.group().equals(shortName);
    	if(f1 && f2){
    		return true;
    	}
    	return false;    		
    }
    
    public static boolean maskFilter(String mask, String FileName)
    {
    	String regMask=mask;
    	regMask=regMask.replace("*", "\\w+");        
    	regMask=regMask.replace("?", "\\w");
    	regMask=regMask.replace(".", "\\.");
    	
    	Pattern sp=Pattern.compile(regMask);
    	String shortName=FileName;
    	if(shortName.contains("\\")){
    		shortName=shortName.substring(shortName.lastIndexOf("\\")+1);
    	}
    	
    	Matcher matcher = sp.matcher(shortName);
    	if(matcher.find()
    			&& matcher.group().equals(shortName)){
    		return true;
    	}
    	return false;
    }
}
