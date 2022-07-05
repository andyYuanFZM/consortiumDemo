package com.chain33.cn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.chain33.javasdk.client.Account;
import cn.chain33.javasdk.client.RpcClient;
import cn.chain33.javasdk.model.AccountInfo;
import cn.chain33.javasdk.model.rpcresult.AccountAccResult;
import cn.chain33.javasdk.model.rpcresult.BlockOverViewResult;
import cn.chain33.javasdk.model.rpcresult.BlockResult;
import cn.chain33.javasdk.model.rpcresult.BlocksResult;
import cn.chain33.javasdk.utils.TransactionUtil;

/**
 * 一些区块链接口的查询
 *
 */
public class BlockChainTest {
	
	// 主链所在服务器IP地址,后续项目方部署自己主链后，替换掉此URL
	RpcClient mclient = new RpcClient("https://jiedian2.bityuan.com:8801");
	
	// 平行链所在服务器IP地址
	String paraIp = "172.22.16.179";
	// 平行链服务端口
	int paraPort = 8901;
	RpcClient pclient = new RpcClient(paraIp, paraPort);
	
	/**
	 * 获取代扣地址下的燃料余额(燃料在主链上，只能取主链的rpc链接)
	 * @throws IOException 
	 */
	@Test
	public void getBalance() throws IOException {
		List<String> list = new ArrayList<String>();
		list.add("1AKAm9vV6m4TbTzHKwirdygakF5HNus8Bg");
		list.add("1L26eqrBgZanXqosSLrzM9ad77B6KwYZov");

		List<AccountAccResult> queryBtyBalance;
		queryBtyBalance = mclient.getCoinsBalance(list, "coins");
		if (queryBtyBalance != null) {
			for (AccountAccResult accountAccResult : queryBtyBalance) {
				System.out.println(accountAccResult.getBalance()/1e8);
			}
		}
	}
	
	
	/**
	 * 获取当前平行链的最大高度(取平行链的rpc链接)
	 * @throws IOException 
	 */
	@Test
	public void getLastHeight() throws IOException {
    	BlockResult blockResult = pclient.getLastHeader();
    	System.out.println("当前最大区块高度为： " + blockResult.getHeight());
	}
	
	
	/**
	 * 根据区块高度区间获取区块详情信息
	 * @throws IOException
	 */
	@Test
	public void getBlockByHeight() throws IOException {
		// 取区块信息, 第三个参数为true：获取交易的详情；第三个参数为false:不获取交易详情
		List<BlocksResult> blockResultList = pclient.getBlocks(10l, 10l, true);
		if (blockResultList.size() >= 0) {
			System.out.println("区块中交易数目为： " + blockResultList.get(0).getBlock().getTxs().size());
			for (int i = 0; i < blockResultList.size(); i++) {
				System.out.println("区块hash:" + blockResultList.get(i).getBlock().getHash());
				System.out.println("前一个区块hash:" + blockResultList.get(i).getBlock().getParentHash());
				System.out.println("默克尔根hash:" + blockResultList.get(i).getBlock().getTxHash());
				System.out.println("区块高度:" + blockResultList.get(i).getBlock().getHeight());
				System.out.println("区块时间戳:" + blockResultList.get(i).getBlock().getBlockTime());
				System.out.println("区块中交易数:" + blockResultList.get(i).getBlock().getTxcount());
				System.out.println("区块中交易详情列表:" + blockResultList.get(i).getBlock().getTxs());
			}
		}
	}
	
	
	/**
	 * 根据区块高度获取当前区块的hash值
	 * @throws IOException
	 */
	@Test
	public void getBlockHashByHeight() throws IOException {
		String blockHash = pclient.getBlockHash(10l);
		System.out.println("指定高度的区块hash值是：" + blockHash);
	}
	
	/**
	 * 根据区块hash获取区块头详情信息（此接口只查区块头，不含交易详情信息）
	 * @throws IOException
	 */
	@Test
	public void getBlockByHash() throws IOException {
		//String hash = "区块hash值";
		String hash = "6bb5593798fc376ef905f2adeff103fe191beb2f6cd6c802a755b2245088255d";
		BlockOverViewResult blockResult = pclient.getBlockOverview(hash);
		System.out.println("区块hash:" + blockResult.getHead().getHash());
		System.out.println("前一个区块hash:" + blockResult.getHead().getParentHash());
		System.out.println("默克尔根hash:" + blockResult.getHead().getTxHash());
		System.out.println("区块高度:" + blockResult.getHead().getHeight());
		System.out.println("区块时间戳:" + blockResult.getHead().getBlockTime());
		System.out.println("区块中交易数:" + blockResult.getHead().getTxcount());
	}

	
	/**
	 * 生成用户私钥和地址
	 */
	@Test
	public void createAccount() {
    	Account account = new Account();
		AccountInfo accountInfo = account.newAccountLocal();
		// 生成用户私钥
		System.out.println(accountInfo.getPrivateKey());
		// 生成用户地址
		System.out.println(accountInfo.getAddress());
    }
	
	/**
	 * 生成YCC用户私钥和地址(以太坊形式，地址以0x开头)
	 */
	@Test
	public void createAccountYCC() {
		Account account = new Account();
		AccountInfo accountInfo = account.newAccountLocalYCC();
		// 生成用户私钥
		System.out.println(accountInfo.getPrivateKey());
		// 生成用户地址
		System.out.println(accountInfo.getAddress());
    }
	
	/**
	 * @description 验证地址的合法性
	 */
	@Test
	public void validateAddress() {
		String address = "1G1L2M1w1c1gpV6SP8tk8gBPGsJe2RfTks";
		boolean validAddressResult = TransactionUtil.validAddress(address);
		System.out.printf("validate result is:%s", validAddressResult);
	}
	
}
