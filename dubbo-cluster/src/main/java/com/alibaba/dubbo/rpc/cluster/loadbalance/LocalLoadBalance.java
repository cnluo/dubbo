package com.alibaba.dubbo.rpc.cluster.loadbalance;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;

/**
 * 本地负载均衡 dubbo.properties === dubbo.dofun.local.loadbalance=local
 */
public class LocalLoadBalance extends AbstractLoadBalance {

	public static final String NAME = "local";
	private static List<String> ipAddrList = new ArrayList<String>();

	protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		try {
			// 获取所有IP地址
			initIpAddrList();
		} catch (Exception e) {
			ipAddrList.add("127.0.0.1");
		}
		for (int i = 0; i < invokers.size(); i++) {
			Invoker<T> invoker = invokers.get(i);
			URL invokerUrl = invoker.getUrl();
			String serverIp = invokerUrl.getIp();
			for (String localIp : ipAddrList) {
				if (localIp.equals(serverIp)) {
					if (!invoker.isAvailable()){
						continue;
					}
					return invoker;
				}
			}
		}
		// 没有服务，走最少活跃LeastActiveLoadBalance
		return new LeastActiveLoadBalance().doSelect(invokers, url, invocation);
	}

	/**
	 * 获取本地IP地址
	 */
	private static void initIpAddrList() throws SocketException {
		if (!ipAddrList.isEmpty()) {
			return;
		}
		Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
		while (netInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = (NetworkInterface) netInterfaces.nextElement();
			Enumeration<?> cardipaddress = networkInterface.getInetAddresses();
			try {
				InetAddress ip = (InetAddress) cardipaddress.nextElement();
				ipAddrList.add(ip.getHostAddress());
			} catch (Exception e) {
				continue;
			}
		}
	}
}