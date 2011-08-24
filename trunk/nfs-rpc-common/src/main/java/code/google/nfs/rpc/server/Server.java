package code.google.nfs.rpc.server;
/**
 * nfs-rpc
 *   Apache License
 *   
 *   http://code.google.com/p/nfs-rpc (c) 2011
 */
import java.util.concurrent.ExecutorService;
/**
 * RPC Server Interface
 * 
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public interface Server {

	/**
	 * ָ��listenPort����Server
	 * 
	 * @param listenPort �����˿�
	 * @param businessThreadPool ҵ���̳߳�
	 * @throws Exception
	 */
	public void start(int listenPort,ExecutorService businessThreadPool) throws Exception;
	
	/**
	 * ע��ҵ������
	 * 
	 * @param serviceName
	 * @param serviceInstance
	 */
	public void registerProcessor(String serviceName,Object serviceInstance);
	
	/**
	 * ֹͣServer
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
}
