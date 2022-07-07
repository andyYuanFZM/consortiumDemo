package com.chain33.cn;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

import cn.chain33.javasdk.client.RpcClient;
import cn.chain33.javasdk.model.AccountInfo;
import cn.chain33.javasdk.model.decode.DecodeRawTransaction;
import cn.chain33.javasdk.model.rpcresult.QueryTransactionResult;
import cn.chain33.javasdk.utils.ByteUtil;
import cn.chain33.javasdk.utils.EvmUtil;
import cn.chain33.javasdk.utils.HexUtil;
import cn.chain33.javasdk.utils.TransactionUtil;

/**
 * NFT ERC721 发行和转让
 * @author fkeit
 *
 */
public class ERC721Test {

		// TODO:需要设置参数 平行链所在服务器IP地址
		String ip = "172.22.16.179";
		// 平行链服务端口
		int port = 8901;
		RpcClient client = new RpcClient(ip, port);
		
	    // TODO:需要设置参数 平行链名称，固定格式user.p.xxxx.样例中使用的名称叫mbaas， 根据自己平行链名称变化。  这个名称一定要和平行链配置文件中的名称完全一致。
		String paraName = "user.p.mbaas.";

		// TODO:需要设置参数 合约部署人（管理员）地址和私钥,地址下需要有BTY来缴纳手续费
		// 生成方式参考下面testCreateAccount方法，私钥和地址一一对应
		String managerAddress = "14nh6p7CUNtLXAHEiVkSd5mLUWynzafHBx";
		String managerPrivateKey = "7dfe80684f7007b2829a28c85be681304f7f4cf6081303dbace925826e2891d1";
//		String managerAddress = "替换成自己的地址，用下面createAccount方法生成";
	//  String managerPrivateKey = "替换成自己的私钥，用下面createAccount方法生成,注意私钥千万不能泄漏";
	    
	    // TODO:需要设置参数 用户手续费代扣地址和私钥,地址下需要有BTY来缴纳手续费
		// 生成方式参考下面testCreateAccount方法，私钥和地址一一对应
		String withholdAddress = "17RH6oiMbUjat3AAyQeifNiACPFefvz3Au";
	    String withholdPrivateKey = "56d1272fcf806c3c5105f3536e39c8b33f88cb8971011dfe5886159201884763";
//		String withholdAddress = "替换成自己的地址，用下面createAccount方法生成";
//	    String withholdPrivateKey = "替换成自己的私钥，用下面createAccount方法生成,注意私钥千万不能泄漏";
	    
	    // 用户A地址和私钥
		String useraAddress;
	    String useraPrivateKey;
	    
	    // 用户B地址和私钥
		String userbAddress;
	    String userbPrivateKey;
	    
	    /**
	     * ERC721合约部署，调用测试
	     * @throws Exception 
	     */
	    @Test
	    public void testERC721() throws Exception {
	    	
	    	// =======> step1： 为用户A和B生成私钥和地址
	    	AccountInfo infoA = CommonUtil.createAccount();
	    	useraAddress = infoA.getAddress();
	    	useraPrivateKey = infoA.getPrivateKey();
	    	
	    	AccountInfo infoB = CommonUtil.createAccount();
	    	userbAddress = infoB.getAddress();
	    	userbPrivateKey = infoB.getPrivateKey();
	    	
	    	// =======>  通过管理员部署合约
	        // 部署合约, 参数： 平行链合约名， 签名地址，签名私钥
	        String hash = deployContract(paraName, managerAddress, managerPrivateKey);
	        
	        // 计算上一步部署到链上的合约地址
	        String contractAddress = TransactionUtil.convertExectoAddr(managerAddress + hash.substring(2));
	        System.out.println("部署好的合约地址 = " + contractAddress);
	        
	        // =======>  调用合约发行NFT,ERC721只支持一物一token
	        // 构造合约调用, mint对应solidity合约里的方法名， 后面三个参数分别代表:NFT发在用户A下, NFT编号, URI信息，比如可以存储NFT图片存储的网址或ipfs地址等
	        int tokenId = 10000;
	        byte[] initNFT = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "mint", useraAddress, tokenId, "{\"图片描述\":\"由xxx创作\";\"创作时间\":\"2022/12/25\";\"图片存放路径\":\"http://www.baidu.com\"}");

	        hash = callContract(initNFT, contractAddress, managerAddress, managerPrivateKey, paraName);
	        
	        // =======>  查询用户A地址下的余额
	        byte[] packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "balanceOf", useraAddress);
	        queryContract(packAbiGet, contractAddress, "转账前用户A,NFTID=" + tokenId + "余额");
	        	        
	        // =======>  从A地址向B地址转账,使用代扣交易
	        // 代扣交易需要对平行链合约地址做一个处理
	        String execer = paraName + "evm";
	        // 平行链合约地址计算(平行链title前缀+合约名称)
	        String paracontractAddress = client.convertExectoAddr(execer);
	        // 用户A将第1个NFT转给用户B
	    	byte[] transfer = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "safeTransferFrom", useraAddress, userbAddress, tokenId);
	    	// 构造转账交易体，先用用户A对此笔交易签名，
	    	String txEncode = EvmUtil.callEvmContractWithhold(transfer,"", 0, execer, useraPrivateKey, contractAddress);
	    	// 再调用代扣交易方法，用代扣私钥对交易组做签名
	    	createNobalance(txEncode, paracontractAddress, useraPrivateKey, withholdPrivateKey);

	        
	        // =======>  查询用户A和用户B地址下的余额
	        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "balanceOf", useraAddress);
	        queryContract(packAbiGet, contractAddress, "转账后用户A,NFTID=" + tokenId + "余额");
	        
	        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "balanceOf", userbAddress);
	        queryContract(packAbiGet, contractAddress, "转账后用户B,NFTID=" + tokenId + "余额");
	        
	        // =======>   查询token URI
	        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_721, "tokenURI", tokenId);
	        queryContractString(packAbiGet, contractAddress, "NFTID=" + tokenId + "的URI信息");
	    }
	    
	    
	    /**
	     * Step2:部署合约
	     * @throws Exception
	     */
	    private String deployContract(String execer, String address, String privateKey) throws Exception {

	        // 部署合约
	        String txEncode;
	        String txhash = "";
	        QueryTransactionResult txResult = new QueryTransactionResult();
	        
	        // 此合约的构造参数有参数，此处为构造方式
	        byte[] evmWithCode = EvmUtil.encodeContructor(CommonUtil.abi_Manager_721, "NFT name", "ART");  
            byte[] code = ByteUtil.merge(HexUtil.fromHexString(CommonUtil.byteCode_Manager_721), evmWithCode);
	        	        
	    	// 估算合约GAS费
	        String evmCode = EvmUtil.getCreateEvmEncode(code, "", "deploy ERC721 contract", execer);
	        long gas = client.queryEVMGas("evm", evmCode, address);
	        System.out.println("Gas fee is:" + gas);
	        
	        // 通过合约code, 管理员私钥，平行链名称+evm,手续费等参数构造部署合约交易，并签名
	        txEncode = EvmUtil.createEvmContract(code, "", "deploy ERC721 contract", privateKey, execer, gas);
	        // 将构造并签好名的交易通过rpc接口发送到平行链上
	        txhash = client.submitTransaction(txEncode);
	        System.out.println("部署合约交易hash = " + txhash);
	        
	        // BTY平均3-5秒一个区块确认， 需要延时去查结果
	        Thread.sleep(5000);
			for (int tick = 0; tick < 20; tick++){
				txResult = client.queryTransaction(txhash);
				if(txResult == null) {
					Thread.sleep(3000);
					continue;
				}			
				break;
			}
			
			if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
				System.out.println("合约部署成功");

			} else {
				System.out.println("合约部署失败,请通过区块链浏览器或命令行查看失败原因");
			}
			
			return txhash;
	    }
	    
	    /**
	     * Step3: 调用合约
	     * @param contractAddr
	     * @param address
	     * @param privateKey
	     * @throws IOException 
	     * @throws InterruptedException 
	     */
	    private String callContract(byte[] code, String contractAddr, String address, String privateKey, String execer) throws Exception {
	    	
	        // 调用合约
	        String txEncode;
	        String txhash = "";
	        QueryTransactionResult txResult = new QueryTransactionResult();
	        
	    	// 估算部署合约GAS费
	        String evmCode = EvmUtil.getCallEvmEncode(code, "", 0, contractAddr, execer);
	        long gas = client.queryEVMGas("evm", evmCode, address);
	        System.out.println("Gas fee is:" + gas);
	        long fee = gas + 100000;
	        
	    	txEncode = EvmUtil.callEvmContract(code,"", 0, contractAddr, privateKey, execer, fee);
	        txhash = client.submitTransaction(txEncode);
	        System.out.println("调用合约hash = " + txhash);
	        
	        // BTY平均3-5秒一个区块确认， 需要延时去查结果
	        Thread.sleep(5000);
			for (int tick = 0; tick < 20; tick++){
				txResult = client.queryTransaction(txhash);
				if(txResult == null) {
					Thread.sleep(3000);
					continue;
				}			
				break;
			}
			
			if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
				System.out.println("合约调用成功");
				
			} else {
				System.out.println("合约部署失败,请通过区块链浏览器或命令行查看失败原因");
			}
			
			return txhash;
	    	
	    }
	    
	    /**
	     * 查询方法
	     * @param queryAbi
	     * @param contractAddress
	     * @throws Exception 
	     */
	    private void queryContract(byte[] queryAbi, String contractAddress, String title) throws Exception {
	        // 查询用户A和用户B地址下的资产余额
	        JSONObject query = client.callEVMAbi(contractAddress, HexUtil.toHexString(queryAbi));
	        JSONObject output = query.getJSONObject("result");
	        String rawData = output.getString("rawData");
	        System.out.println(title + ": " + HexUtil.hexStringToAlgorism(HexUtil.removeHexHeader(rawData)));
	    }
	    
	    /**
	     * 查询方法
	     * @param queryAbi
	     * @param contractAddress
	     * @throws Exception 
	     */
	    private void queryContractString(byte[] queryAbi, String contractAddress, String title) throws Exception {
	        // 查询用户A和用户B地址下的资产余额
	        JSONObject query = client.callEVMAbi(contractAddress, HexUtil.toHexString(queryAbi));
	        JSONObject output = query.getJSONObject("result");
	        String rawData = output.getString("rawData");
	        System.out.println(title + ": " + HexUtil.hexStringToString(HexUtil.removeHexHeader(rawData)).replaceAll("\u0000",""));
	    }
	    
	    
	    /**
	     * 构建代扣手续费交易
	     * 
	     * @param txEncode
	     * @param contranctAddress
	     * @return
	     * @throws InterruptedException
	     * @throws IOException 
	     */
	    private String createNobalance(String txEncode, String contranctAddress, String userPrivatekey, String withHoldPrivateKey) throws Exception {
	        String createNoBalanceTx = client.createNoBalanceTx(txEncode, "");
		    // 解析交易
		    List<DecodeRawTransaction> decodeRawTransactions = client.decodeRawTransaction(createNoBalanceTx);
		    
		    String hexString = TransactionUtil.signDecodeTx(decodeRawTransactions, contranctAddress, userPrivatekey, withHoldPrivateKey);
		    String submitTransaction = client.submitTransaction(hexString);
		    System.out.println("代扣hash= " + submitTransaction);
		    
		    String nextString = null;
	        QueryTransactionResult txResult = new QueryTransactionResult();

			Thread.sleep(5000);
			for (int tick = 0; tick < 20; tick++){
				QueryTransactionResult result = client.queryTransaction(submitTransaction);
				if(result == null) {
					Thread.sleep(3000);
					continue;
				}

				System.out.println("next:" + result.getTx().getNext());
				QueryTransactionResult nextResult = client.queryTransaction(result.getTx().getNext());
				System.out.println("ty:" + nextResult.getReceipt().getTyname());
				nextString = result.getTx().getNext();
				break;
			}
			
			
			txResult = client.queryTransaction(nextString);
			if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
				System.out.println("合约调用成功");
				
			} else {
				System.out.println("合约调用失败，一般失败原因可能是因为地址下手续费不够");
			}
			return nextString;
	    }
}
