package im.vinci.server.naturelang.musicpick.util;

/**
 * @author admin
 *
 */
/**
 * @author admin
 *
 */
public class LCSdistance {

	public static String lcs(String a, String b) {
	    int[][] lengths = new int[a.length()+1][b.length()+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length(); i++)
	        for (int j = 0; j < b.length(); j++)
	            if (a.charAt(i) == b.charAt(j))
	                lengths[i+1][j+1] = lengths[i][j] + 1;
	            else
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	 
	    // read the substring out from the matrix
	    StringBuffer sb = new StringBuffer();
	    for (int x = a.length(), y = b.length();
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y])
	            x--;
	        else if (lengths[x][y] == lengths[x][y-1])
	            y--;
	        else {
	            assert a.charAt(x-1) == b.charAt(y-1);
	            sb.append(a.charAt(x-1));
	            x--;
	            y--;
	        }
	    }
	 
	    return sb.reverse().toString();
	}
	
	public static double lcs_distance(String a, String b){
		String commonString = lcs(a,b);
		if(a.length()<b.length()){
			return (double)(a.length()-commonString.length()+1)/(a.length()+1);
		}else{
			return (double)(b.length()-commonString.length()+1)/(b.length()+1);
		}
	}
	
	/**
	 * @param a the string of command
	 * @param b the string from jingxuanjiwords
	 * @return
	 */
	public static double lcs_distance2(String a, String b){
		String commonString = lcs(a,b);
		if(((double)commonString.length()/(double)b.length())>=0.75){
			
			if(a.contains(commonString)){
				//if the commonString appears in a as whole, reward
				return (double)((double)(commonString.length()+1)/(double)(a.length()+b.length()))+0.1;
			}
			
			return (double)((double)(commonString.length()+1)/(double)(a.length()+b.length()));
		}else{
			return 0.0;
		}
		
	
	}
	
	public static double lcs_distance3(String a, String b){
		String commonString = lcs(a,b);
		
		return ((double)commonString.length())/(((double)a.length()+b.length()/2));
		
	}
	
/*	public static void main(String[] args){
		System.out.println(lcs_distance("abcdedd","bcd"));
	}*/
}
