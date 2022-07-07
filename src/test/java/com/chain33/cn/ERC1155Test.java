package com.chain33.cn;

import java.io.IOException;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

import cn.chain33.javasdk.client.RpcClient;
import cn.chain33.javasdk.model.AccountInfo;
import cn.chain33.javasdk.model.rpcresult.QueryTransactionResult;
import cn.chain33.javasdk.utils.ByteUtil;
import cn.chain33.javasdk.utils.EvmUtil;
import cn.chain33.javasdk.utils.HexUtil;
import cn.chain33.javasdk.utils.TransactionUtil;

/**
 * 联盟链主链+平行链下发行ERC1155合约
 * 联盟链和公链在sdk使用上区别在于联盟链默认关闭手续费,所以可以不用关心燃料的问题; 而公链需要手续费,所以会在sdk调用时需要构造代扣交易
 *
 */
public class ERC1155Test {

	// TODO:需要设置参数，平行链所在服务器IP地址
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
//	String managerAddress = "替换成自己的地址，用下面createAccount方法生成";
//  String managerPrivateKey = "替换成自己的私钥，用下面createAccount方法生成,注意私钥千万不能泄漏";
    
    // 用户A地址和私钥
	String useraAddress;
    String useraPrivateKey;
    
    // 用户B地址和私钥
	String userbAddress;
    String userbPrivateKey;
    
    /**
     * ERC1155合约部署，调用测试
     * @throws Exception 
     */
    @Test
    public void testERC1155() throws Exception {
    	
    	
    	// =======> step1： 为用户A和B生成私钥和地址
    	AccountInfo infoA = CommonUtil.createAccount();
    	useraAddress = infoA.getAddress();
    	useraPrivateKey = infoA.getPrivateKey();
    	
    	AccountInfo infoB = CommonUtil.createAccount();
    	userbAddress = infoB.getAddress();
    	userbPrivateKey = infoB.getPrivateKey();
    	
    	// =======>  step2: 通过管理员部署合约，部署好之后，合约就运行区块链内存中，后续可以直接调用，不用每次都调用部署合约这一步操作（除非业务上有需要）
        // 部署合约, 参数： 平行链合约名， 签名地址，签名私钥
        String hash = deployContract(paraName, managerAddress, managerPrivateKey);
        
        // 计算上一步部署到链上的合约地址
        String contractAddress = TransactionUtil.convertExectoAddr(managerAddress + hash.substring(2));
        System.out.println("部署好的合约地址 = " + contractAddress);
        
        // =======>  step3: 调用合约发行NFT,假设为2件游戏道具各生成100个NFT资产, id从10000开始
        int lenght = 2;
        int[] ids = new int[lenght];
        int[] amounts = new int[lenght];
        // 每一个tokenid对应的URI信息（一般对应存放图片的描述信息，图片内容的一个url）
        String[] uris = new String[lenght];
        for (int i = 0; i < lenght; i++) {
        	ids[i] = 10000 + i;
        	amounts[i] = 100;
        	// 例子为了简化处理，让所有ID都固定一个地址，
        	uris[i] = "{\"图片描述\":\"由xxx创作\";\"创作时间\":\"2022/12/25\";\"图片存放路径\":\"http://www.baidu.com\"}";
        }
        // 构造合约调用, mint对应solidity合约里的方法名， useraAddress, ids, amounts这三项对应合约里的参数。  将NFT发行在用户A地址下
        byte[] initNFT = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "mint", useraAddress, ids, amounts, uris);

        hash = callContract(initNFT, contractAddress, managerAddress, managerPrivateKey, paraName);
        
        // =======>  查询用户A地址下的余额
        byte[] packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "balanceOf", useraAddress, ids[0]);
        queryContract(packAbiGet, contractAddress, "转账前用户A,NFTID=" + ids[0] + "余额");
        
        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "balanceOf", useraAddress, ids[1]);
        queryContract(packAbiGet, contractAddress, "转账前用户A,NFTID=" + ids[1] + "余额");
        
        // =======>  从A地址向B地址转账,使用代扣交易
        // 用户A将第1个NFT中的50个转给用户B
    	byte[] transfer = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "transferArtNFT", userbAddress, ids[0], 50);
        // 用户A将第1个NFT转给用户B
    	hash = callContract(transfer, contractAddress, useraAddress, useraPrivateKey, paraName);
    	
        // =======>  查询用户A地址下的余额
        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "balanceOf", useraAddress, ids[0]);
        queryContract(packAbiGet, contractAddress, "转账后用户A,NFTID=" + ids[0] + "余额");
        
        // =======>  查询用户B地址下的余额
        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "balanceOf", userbAddress, ids[0]);
        queryContract(packAbiGet, contractAddress, "转账后用户B,NFTID=" + ids[0] + "余额");
        
        // =======>  查询指定tokenid的uri信息
        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "uri", ids[0]);
        queryContractString(packAbiGet, contractAddress, "NFTID=" + ids[0] + "的URI信息");
        
        // =======>  查询指定tokenid的uri信息
        packAbiGet = EvmUtil.encodeParameter(CommonUtil.abi_Manager_1155, "uri", ids[1]);
        queryContractString(packAbiGet, contractAddress, "NFTID=" + ids[1] + "的URI信息");
        
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
        
        byte[] code = ByteUtil.merge(HexUtil.fromHexString(CommonUtil.byteCode_Manager_1155), CommonUtil.abi_Manager_1155.getBytes());
        
    	// 部署合约GAS费估算
        String evmCode = EvmUtil.getCreateEvmEncode(code, "", "deploy ERC1155 contract", execer);
        long gas = client.queryEVMGas("evm", evmCode, address);
        System.out.println("部署合约交易的Gas是: " + gas);
        
        // 通过合约code, 管理员私钥，平行链名称+evm,手续费等参数构造部署合约交易，并签名
        txEncode = EvmUtil.createEvmContract(code, "", "evm-sdk-test", privateKey, execer, gas);
        // 将构造并签好名的交易通过rpc接口发送到平行链上
        txhash = client.submitTransaction(txEncode);
        System.out.println("部署合约交易hash = " + txhash);
        
        // 联盟链平均1-2秒一个区块确认， 需要延时去查结果
        Thread.sleep(5000);
		for (int tick = 0; tick < 20; tick++){
			txResult = client.queryTransaction(txhash);
			if(txResult == null) {
				Thread.sleep(1000);
				continue;
			}			
			break;
		}
		
		if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
			System.out.println("合约部署成功");

		} else {
			System.out.println("合约部署失败,请通过区块链浏览器或命令行查看失败原因" );
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
        
        // 估算合约执行GAS费
        String evmCode = EvmUtil.getCallEvmEncode(code, "", 0, contractAddr, execer);
        long gas = client.queryEVMGas("evm", evmCode, address);
        System.out.println("调用合约交易的GAS是： " + gas);
    	
    	txEncode = EvmUtil.callEvmContract(code,"", 0, contractAddr, privateKey, execer, gas);
        txhash = client.submitTransaction(txEncode);
        System.out.println("调用合约hash = " + txhash);
        
        // 联盟链平均1-2秒一个区块确认， 需要延时去查结果
        Thread.sleep(5000);
		for (int tick = 0; tick < 20; tick++){
			txResult = client.queryTransaction(txhash);
			if(txResult == null) {
				Thread.sleep(1000);
				continue;
			}			
			break;
		}
		
		if ("ExecOk".equals(txResult.getReceipt().getTyname())) {
			System.out.println("合约调用成功");
			
		} else {
			System.out.println("合约部署失败,请通过区块链浏览器或命令行查看失败原因" );
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
    
}
