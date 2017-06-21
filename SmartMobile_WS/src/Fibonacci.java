import java.sql.SQLException;


public class Fibonacci {
	
	public String retornoXXX(String xml){
		
		return "Cliente WEBSERVER" + xml;
	}
	
	public int calculate(int n)   
	    {  
		
		
	        if (n < 0)  
	            throw new IllegalArgumentException("number must be >= 0");  
	        switch (n)   
	        {  
	            case 0:  
	                return 0;  
	            case 1:  
	                return 1;  
	            default:  
	                return calculate(n - 2) + calculate(n - 1);  
	        }  
	    }  
	
	
	public int getSoma(int n1, int n2) {

           return n1+n2;

     }
	
	
public static void main(String[] args) throws SQLException { 
		
		System.out.println("Eu sou o seu primeiro programa.");
		System.out.println("Testando execução da procedure montar fucker");
		
		String data = "15/04/2012";
		System.out.println(data.substring(6,10) +"x"+data.substring(3,5)+"x"+data.substring(0,2));
		//System.out.println(data.substring(0,2) +"."+data.substring(2,5) +"."+data.substring(5,8)+"/"+data.substring(8,12)+"-"+data.substring(12,14));
		
}
	
}
