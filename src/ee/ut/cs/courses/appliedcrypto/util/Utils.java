package ee.ut.cs.courses.appliedcrypto.util;

public class Utils {
    
    /**
     * Converts a byte array to hex string
     * @param arr byte array input data
     * @return hex string
     */
    // Taken from JDigiDoc 2.3.19
    public static String bin2hex(byte[] arr)
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < arr.length; i++) {
                String str = Integer.toHexString((int)arr[i]);
                if(str.length() == 2)
                        sb.append(str);
                if(str.length() < 2) {
                        sb.append("0");
                        sb.append(str);
                }
                if(str.length() > 2)
                        sb.append(str.substring(str.length()-2));
        }
        return sb.toString();
    }
    
    public static void terminate(String msg, Exception e) {
    	terminate(msg + " " + e.getMessage());
    }
    
    public static void terminate(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
