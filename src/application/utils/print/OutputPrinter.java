package application.utils.print;

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
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
		

		Graphics2D g2d = (Graphics2D) g;
		int x = (int) pf.getImageableX();
		int y = (int) pf.getImageableY();
		int pageWidth = (int) pf.getImageableWidth()-50;
		int pageHeight = (int) pf.getImageableHeight()-50;
		g2d.translate(x, y);

		
		Font font = new Font("Courier", Font.PLAIN, 10);
		FontMetrics metrics = g.getFontMetrics(font);
		int lineHeight = metrics.getHeight();

		BufferedReader br = new BufferedReader(new StringReader(printData));
		ArrayList<String> all_strings = new ArrayList<String>();
		try {
			String line;
			while ((line = br.readLine()) != null) {
				all_strings.addAll(cutLine(line, metrics, (int) pageWidth));
			}
		} catch (IOException e) {
			//
		}
		
		int first_line = ((pageHeight) / lineHeight) * page;
		int next_page_line = ((pageHeight)/ lineHeight) * (page + 1);
		if (first_line > all_strings.size()) {
			return NO_SUCH_PAGE;
		}

		g2d.setFont(font);

		for (int i = first_line; i < Math.min(next_page_line, all_strings.size()); i++) {
			y += lineHeight;
			g2d.drawString(all_strings.get(i), x, y);
		}

		return PAGE_EXISTS;
	}
    
    ArrayList<String> cutLine(String line,FontMetrics metrics,int maxWidth)
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
    		int sw=metrics.stringWidth(indent+currentLine+part);
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
    	
    	
    	return outStrings;
    }
}
