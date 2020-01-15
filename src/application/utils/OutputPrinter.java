package application.utils;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class OutputPrinter implements Printable 
{
    private String printData;

    public OutputPrinter(String printDataIn)
    {
    this.printData = printDataIn;
    }   
    
    
    @Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException
	{
	    // Should only have one page, and page # is zero-based.
	    if (page > 0)
	    {
	        return NO_SUCH_PAGE;
	    }
	
	    // Adding the "Imageable" to the x and y puts the margins on the page.
	    // To make it safe for printing.
	    Graphics2D g2d = (Graphics2D)g;
	    int x = (int) pf.getImageableX();
	    int y = (int) pf.getImageableY();  
	    int pageWidth=(int) pf.getImageableWidth();
	    int pageHeight= (int) pf.getImageableHeight();
	    g2d.translate(x, y); 
	
	    // Calculate the line height
	    Font font = new Font("Courier", Font.PLAIN, 10);
	    FontMetrics metrics = g.getFontMetrics(font);
	    int lineHeight = metrics.getHeight();	    
	    
	    BufferedReader br = new BufferedReader(new StringReader(printData));
	
	    // Draw the page:
	    try
	    {
	        String line;       
	        
	        g2d.setFont(font);
	        
	        while ((line = br.readLine()) != null)
	        {
	        	String[] lines=cutLine(line,metrics,(int) pageWidth);
	        	for(String currentLine:lines)
	        	{
	        		y += lineHeight;	        		
	        		g2d.drawString(currentLine, x, y);
	        	}
	        }
	    }
	    catch (IOException e)
	    {
	        // 
	    }
	
	    return PAGE_EXISTS;
	}
    
    String[] cutLine(String line,FontMetrics metrics,int maxWidth)
    {
    	ArrayList<String> outStrings=new ArrayList<String>();
    	ArrayList<String> splitStrings=new ArrayList<String>();
    	String indent="";
    	
    	while(line.indexOf(" ")==0)
    	{
    		indent=indent+" ";
    		line=line.substring(1);
    	}
    	
    	while(line.lastIndexOf(" ")>0)
    	{
    		splitStrings.add(line.substring(0, line.indexOf(" ")+1));
    		line=line.substring(line.indexOf(" ")+1,line.length());
    	}
    	if(line.length()>0)
    	{
    		splitStrings.add(line);
    	}
    	
    	String currentLine="";
    	for(String part:splitStrings)
    	{
    		int sw=metrics.stringWidth(currentLine+part);
    		if(sw>maxWidth
    				&& currentLine.length()>0)
    		{    			
    				outStrings.add(indent+currentLine);
    				currentLine="";    			
    		}
    		currentLine=currentLine+part;
    	}
    	
    	if(currentLine.length()>0)
    	{
    		outStrings.add(indent+currentLine);
    	}
    	
    	
    	return outStrings.toArray(new String[outStrings.size()]);
    }
}
