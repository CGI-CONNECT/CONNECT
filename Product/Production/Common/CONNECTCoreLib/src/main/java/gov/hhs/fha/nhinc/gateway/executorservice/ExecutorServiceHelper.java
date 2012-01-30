package gov.hhs.fha.nhinc.gateway.executorservice;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;



import java.io.CharArrayWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Singleton class that holds the ExecutorService configs as follows
 * - concurrentPoolSize is the size of the pool for the executor service
 * - largejobPoolSize is the size of the pool for the large job executor service
 * - largejobSizePercent is used in checkExecutorTaskIsLarge to determine if a
 * task should be executed using the large job executor service.  If the task will
 * fanout to a list of n targets, then the task is a large job if
 * n >= largejobSizePercent * concurrentPoolSize
 *
 * - timeoutValues Map
 * URLConnection offers setConnectTimeout() and setReadTimeout() methods
 * to set the web service urlconnection timeouts.
 * Connect timeout is time to establish the http/https urlconnection in millis
 * Request timeout is the read timeout for the http/https urlconnection
 * (after urlconnection established) in millis
 *
 * Note that the timeout settings are metro based
 * CONNECT_TIMEOUT_NAME = "com.sun.xml.ws.connect.timeout"
 * REQUEST_TIMEOUT_NAME = "com.sun.xml.ws.request.timeout"
 * 
 * @author paul.eftis
 */
public class ExecutorServiceHelper{

    private static Log log = LogFactory.getLog(ExecutorServiceHelper.class);

    private static ExecutorServiceHelper instance = null;
    private static final Object EXSYNC = new Object();

    // default pool size is 100
    private static int concurrentPoolSize;
    // default large job pool size is 200
    private static int largejobPoolSize;
    // default large job size percent is 75%
    private static double largejobSizePercent;
    // timeoutValues Map contains web service client timeouts
    private static Map timeoutValues = new HashMap();



    // private constructor to ensure singleton
    private ExecutorServiceHelper(){
        try{
            // get executor service pool sizes
            String concurrentPoolSizeStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.CONCURRENT_POOL_SIZE);
            concurrentPoolSize = Integer.parseInt(concurrentPoolSizeStr);
            String largejobPoolSizeStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.LARGEJOB_POOL_SIZE);
            largejobPoolSize = Integer.parseInt(largejobPoolSizeStr);
            // get large job percentage
            String largejobSizePercentStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.LARGEJOB_SIZE_PERCENT);
            largejobSizePercent = Double.parseDouble(largejobSizePercentStr);
            // get web service client timeouts
            String PDConnectTimeoutStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.PATIENT_DISCOVERY_CONNECT_TIMEOUT);
            timeoutValues.put(NhincConstants.PATIENT_DISCOVERY_CONNECT_TIMEOUT,
                    new Integer(PDConnectTimeoutStr));
            String PDRequestTimeoutStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.PATIENT_DISCOVERY_REQUEST_TIMEOUT);
            timeoutValues.put(NhincConstants.PATIENT_DISCOVERY_REQUEST_TIMEOUT,
                    new Integer(PDRequestTimeoutStr));
            String DQConnectTimeoutStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.DOC_QUERY_CONNECT_TIMEOUT);
            timeoutValues.put(NhincConstants.DOC_QUERY_CONNECT_TIMEOUT,
                    new Integer(DQConnectTimeoutStr));
            String DQRequestTimeoutStr = PropertyAccessor.getProperty(
                    NhincConstants.GATEWAY_PROPERTY_FILE,
                    NhincConstants.DOC_QUERY_REQUEST_TIMEOUT);
            timeoutValues.put(NhincConstants.DOC_QUERY_REQUEST_TIMEOUT,
                    new Integer(DQRequestTimeoutStr));
        }catch(Exception e){
            log.error("ExecutorServiceHelper exception loading config properties so using default values");
            outputCompleteException(e);
            // set default pool size to 100
            concurrentPoolSize = 100;
            // set default large job pool size to 200
            largejobPoolSize = 200;
            // set default large job size percent to 75%
            largejobSizePercent = .75;
            // set to default timeouts in this case
            timeoutValues.put(NhincConstants.PATIENT_DISCOVERY_CONNECT_TIMEOUT, new Integer(30000));
            timeoutValues.put(NhincConstants.PATIENT_DISCOVERY_REQUEST_TIMEOUT, new Integer(30000));
            timeoutValues.put(NhincConstants.DOC_QUERY_CONNECT_TIMEOUT, new Integer(30000));
            timeoutValues.put(NhincConstants.DOC_QUERY_REQUEST_TIMEOUT, new Integer(60000));
        }
        log.debug("ExecutorServiceHelper created singleton instance and " +
                "set executor service configuration parameters: "
                + "concurrentPoolSize=" + concurrentPoolSize
                + " largejobPoolSize=" + largejobPoolSize
                + " largejobSizePercent=" + largejobSizePercent
                + " PDConnectTimeout=" + timeoutValues.get(NhincConstants.PATIENT_DISCOVERY_CONNECT_TIMEOUT)
                + " PDRequestTimeout=" + timeoutValues.get(NhincConstants.PATIENT_DISCOVERY_REQUEST_TIMEOUT)
                + " DQConnectTimeout=" + timeoutValues.get(NhincConstants.DOC_QUERY_CONNECT_TIMEOUT)
                + " DQRequestTimeout=" + timeoutValues.get(NhincConstants.DOC_QUERY_REQUEST_TIMEOUT)
                );
    }



    // singleton using double null check pattern
    public static ExecutorServiceHelper getInstance(){
        if(instance != null){
            return instance;
        }else{
            synchronized(EXSYNC){
                if(instance == null){
                    instance = new ExecutorServiceHelper();
                }
            }
            return instance;
        }
    }


    public static int getExecutorPoolSize(){
        return concurrentPoolSize;
    }


    public static int getLargeJobExecutorPoolSize(){
        return largejobPoolSize;
    }


    public static double getLargeJobPercentage(){
        return largejobSizePercent;
    }


    public static Map getTimeoutValues(){
        return timeoutValues;
    }


    /**
     * Used to determine if a task should be executed using the large job executor
     * service.  If targetListCount >= largejobSizePercent * concurrentPoolSize
     * then it is a large job.
     *
     * @param targetListCount is the fan-out count for the task
     * @return boolean true if task should be run using large job executor service
     */
    public static boolean checkExecutorTaskIsLarge(int targetListCount){
        boolean bigJob = false;
        Double maxSize = new Double(largejobSizePercent * concurrentPoolSize);
        if(targetListCount >= maxSize.intValue()){
            bigJob = true;
            log.debug("checkExecutorTaskIsLarge has large job size=" + targetListCount
                    + " so returning LargeJobExecutor");
        }
        return bigJob;
    }


    /**
     * Useful util to dump complete exception stack trace
     * @param ex
     */
    public static void outputCompleteException(Exception ex){
        String err = "EXCEPTION:" + ex.getMessage() + "\r\n";
        CharArrayWriter caw = new CharArrayWriter();
        ex.printStackTrace(new PrintWriter(caw));
        err += caw.toString();
        log.error(err);
    }


    /**
     * Useful util to output exception info in a formatted string
     * @param ex
     */
    public static String getFormattedExceptionInfo(Exception ex, NhinTargetSystemType target,
            String serviceName){
        String err = "EXCEPTION: " + ex.getClass().getCanonicalName() + "\r\n";
        err += "EXCEPTION Cause: " + ex.getCause().getClass().getCanonicalName() + "\r\n";
        if(ex.getCause() instanceof com.sun.xml.ws.client.ClientTransportException){
            try{
                NhincConstants.GATEWAY_API_LEVEL apiLevel =
                        ConnectionManagerCache.getInstance().getApiVersion(
                            target.getHomeCommunity().getHomeCommunityId(),
                            serviceName);
                String url = (new WebServiceProxyHelper()).getUrlFromTargetSystemByGatewayAPILevel(
                        target, serviceName, apiLevel);
                err += "EXCEPTION Message: Unable to connect to endpoint url=" + url + "\r\n";
            }catch(Exception e){}
        }
        err += "EXCEPTION Cause Message: " + ex.getCause().getMessage();
        return err;
    }

}
