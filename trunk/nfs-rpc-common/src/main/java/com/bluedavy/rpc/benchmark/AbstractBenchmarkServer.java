package com.bluedavy.rpc.benchmark;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.bluedavy.rpc.NamedThreadFactory;
import com.bluedavy.rpc.server.Server;

/**
 * ����RPC Benchmark���Եķ������� ����������ʱ���ڿͻ����ռ����Կͻ�������Ϊ׼ Usage: BenchmarkServer
 * listenPort maxThreads responseSize
 */
public abstract class AbstractBenchmarkServer {

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/**
	 * �ⲿ�ɴ���Ĳ���Ϊ�� args[0]: �����˿� args[1]: server�߳��� args[2]: ���ص���Ӧ����Ĵ�С
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void run(String[] args) throws Exception {
		if (args == null || args.length != 3) {
			throw new IllegalArgumentException(
					"must give three args: listenPort | maxThreads | responseSize");
		}
		int listenPort = Integer.parseInt(args[0]);
		int maxThreads = Integer.parseInt(args[1]);
		final int responseSize = Integer.parseInt(args[2]);
		System.out.println(dateFormat.format(new Date())
				+ " ready to start server,listenPort is: " + listenPort
				+ ",maxThreads is:" + maxThreads + ",responseSize is:"
				+ responseSize + " bytes");

		Server server = getServer();
		server.registerProcessor("testservice", getServerProcessor(responseSize));
		ThreadFactory tf = new NamedThreadFactory("BUSINESSTHREADPOOL");
		ExecutorService threadPool = new ThreadPoolExecutor(20, maxThreads,
				300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), tf);
		server.start(listenPort, threadPool);
	}

	public abstract Object getServerProcessor(int responseSize);
	
	/**
	 * ��ȡ�����Server Instance
	 */
	public abstract Server getServer();

}
