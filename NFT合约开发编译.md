# NFT合约开发编译

## 目录
	- [NFT合约说明](#NFT合约说明)
	- [ERC721合约](#ERC721合约)
	- [ERC1155合约](#ERC1155合约)
	- [NFT合约编译 ](#NFT合约编译 )
	- [合约参考例子 ](#合约参考例子 )
	- [NFT合约部署调用流程 ](#NFT合约部署调用流程 )

### NFT合约说明
NFT(非同质化数字资产)是具有唯一且彼此不可替换属性的数字资产，具有标准化、通用性、流动性以及可编程特性，常见的应用场景包括收藏品、游戏物品、数字艺术、证书、域名等。NFT不像BTC或ETH这些可以分割成0.1或是0.02，NFT的单位永远是1，唯一性和稀缺性是它的典型特征。  
NFT因为2017年的以太猫而大火，同时开创了第一个NFT的标准：ERC721。 之后又由Enjin公司在ERC721的基础上制订了ERC1155的标准，它支持在一个合约中存储多个数字资产，支持一次性批量发行多个不同类型的的数字资产，支持在一次转账过程中转多个不同类型的数字资产。  
Chain33区块链上的EVM虚拟机也完全兼容上述的NFT标准，下文提供ERC721和ERC1155两类合约的样例，它们实现了最基础的NFT发行，转让以及NFT相关属性的查询等功能, 在此基础上, 如果用户有更多的业务需求，可以基于样例再结合实际的业务需求自行进行二次开发。  

### ERC721合约
#### 场景设计
假设对一批艺术品（数字画作）发行链上NFT资产通证， 每一幅画有且仅有一个NFT通证对应。 每一个NFT通证都可以在区块链中流转。  
可以根据用户地址查询他NFT资产的持有数量。  
可以通过NFT的ID查看到数字画在网络/区块链上的存储位置。  

#### 合约编写思路
1. 合约初始化时设定通证名称（name）和符号(symbol)，以及定义此合约的拥有者（可以限制哪些接口只能拥有者才能调用）。  name和symbol可以和某类作品对应，比如【敦煌飞天数字藏品】或【无聊猿NFT】等，而每一类下又可以发行多个通证，每个通证都有唯一的编号。  
2. 提供NFT通证发行的接口，在发行接口中可以定义是否只能允许合约拥有者才能调用， 同时在发行时要指定通证发行到哪个用户地址下，以及当前通证对应的数字画作本身以及描述信息存储位置的URL信息。   
3. 其它： NFT的转让，NFT的查询等接口。 这些接口在import的sol文件中都有了定义，在没有业务定制情况下，可以不需要重写接口。   

#### ERC721合约代码
```
// SPDX-License-Identifier: SimPL-2.0
pragma solidity 0.8.0;

import "https://github.com/nibbstack/erc721/src/contracts/tokens/nf-token-metadata.sol";

contract newERC721 is NFTokenMetadata {
address public _owner;
  /**
   * @dev 构造函数,可设定token名称和token symbol.
   */
  constructor(string memory _name, string memory _symbol) {
    nftName = _name; 
    nftSymbol = _symbol;
    _owner = msg.sender;
  }

  /**
   * @dev 发行NFT,限定只有合约部署人才可以调用
   * @param _to NFT发行在哪个地址下
   * @param _tokenId NFT的tokenid(整型)
   * @param _uri token uri信息
   */
  function mint(address _to, uint256 _tokenId, string calldata _uri) external {
    require(msg.sender == _owner, "only authorized owner can mint nft.");
    super._mint(_to, _tokenId);
    super._setTokenUri(_tokenId, _uri);
  }
}
```

### ERC1155合约
#### 场景设计
假设对一批艺术品（数字画作）发行链上NFT资产通证,支持同一个NFT ID下有多份数量（完全一样的一幅画发行了超过1份的数量）， 且支持对NFT资产的批量转移到另一个用户名下。 

#### 合约编写思路
1. 支持批量发行NFT的合约接口，用户可传入NFT的编号列表，每个NFT的数量列表，以及NFT的属性（描述信息，图片存放位置等）来批量发行NFT,同时合约限定只有合约部署者才可以发行资产。  
2. 其它：NFT的转让，NFT的查询等接口。     

#### ERC1155合约代码
```
// SPDX-License-Identifier: SimPL-2.0
pragma solidity ^0.8.1;
import "github.com/OpenZeppelin/openzeppelin-contracts/blob/master/contracts/token/ERC1155/ERC1155.sol";

contract newERC1155 is ERC1155 {

    address public _owner;
    mapping(uint256 => string) private _tokenURI;
    
    constructor() public  ERC1155("") {
        _owner = msg.sender;
    }
    
    /**
     * 初始化NFT资产
     * _to:NFT发行在哪个地址下
     * ids: NFT资产数组
     * amounts: NFT数额，和上面的ids长度要保持一致，并且一一对应
     * uris: NFT的URI信息，和上面的ids长度要保持一致，并且一一对应
     */
    function mint(address _to, uint256[] memory ids, uint256[] memory amounts, string[] memory uris) external {
        require(msg.sender == _owner, "only authorized owner can mint NFT.");
        require(ids.length == amounts.length, "The ids and amounts are not match");
        require(ids.length == uris.length, "The ids and uris are not match");
        _mintBatch(_to, ids, amounts, "");
        if (uris.length > 0) {
            for (uint256 i = 0; i < ids.length; i++) {
                _setURI(ids[i], uris[i]);
            }
        }
        
    }

    /**
     * 转让NFT
     * to: 转让的去向地址
     * id: NFT编号
     * amount: 转让数量
     */
    function transferArtNFT(address to, uint256 id, uint256 amount) external {
        // 转账
        safeTransferFrom(msg.sender, to, id, amount, "");
    }

    /**
     * 设置NFT URI信息
     * id: NFT编号
     * uri: URI信息
     */
    function _setURI(uint256 _id, string memory _uri) internal {
        _tokenURI[_id] = _uri;
    }

    function uri(uint256 _id) public view virtual override returns (string memory) {
        return _tokenURI[_id];
    }
    
}
```

### NFT合约编译
将上述两份合约代码分别拷贝到在线IDE中, 使用[[remix]](https://remix.ethereum.org/), 注意编译器版本要和代码上的一致（选用0.8.1）  
![Image text](https://github.com/andyYuanFZM/btyDemo/blob/master/src/test/resources/compilation.png)  
如上图，从IDE中编译获取到ABI和bytecode（红框中按钮点一下就可以将信息拷贝出来, bytecode里包含了很多信息,只需要复制其下"object"里的信息），为下一步合约部署上链做准备。  

- ABI含义：   合约接口说明，即应用程序二进制接口（Application Binary Interface，ABI）。可以理解为合约的接口说明。当合约被编译后，其对应的ABI也会一起生成，它类似于程序的接口说明文档，描述了属性和方法签名相关信息，包括字段名称、字段类型、方法名称、参数名称、参数类型、方法返回值等。  

| 参数| 描述                              | 
| ------- | -------------------------------------- | 
| name   | 函数名称| 
| type   | 方法类型，包括function、constructor、fallback（缺省方法），默认function| 
| constant   | 布尔值，如果为true，则表示方法不会修改合约字段的状态变量| 
| payable   | 布尔值，表示方法是否可以接收系统转账| 
| stateMutability   | 状态类型，包括pure（不读取区块链状态）、view（和constant类型相同，只能查看，不会修改合约字段）、nonpayable（和payable含义相同）、payable（和payable含义相同）| 
| inputs   | 数组，描述参数的名称和类型| 
| name   | 参数名称| 
| type   | 参数类型| 
| outputs   | 和inputs相同，如果没有返回值，缺省是一个空数组|   

如果需要了解更多合约接口说明（ABI）相关信息，请参见[[ABI官方介绍]](https://docs.soliditylang.org/en/v0.4.24/abi-spec.html?spm=a2c4g.11186623.2.13.1c865bd2fv0E7S)  

- bytecode含义： 字节码，即合约代码的编译结果，也是合约部署时使用的关键数据，通过SDK可以将合约字节码部署到目标的生产链上使用。  

### 合约参考例子
[[管理员发行ERC1155]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC1155ByManager.sol): 限制NFT发行这个动作只能由合约的部署人（管理员）来执行, 适用于平台对于NFT发行有限制的业务场景。   
[[用户发行ERC1155]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC1155ByUser.sol): NFT合约不限制只有管理员才能发行，任何用户都可以调用mint方法发行NFT， 适用于平台任意作者都可以发行NFT的业务场景。  
[[管理员发行ERC721]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC721ByManager.sol): 限制NFT发行这个动作只能由合约的部署人（管理员）来执行, 适用于平台对于NFT发行有限制的业务场景。   
[[用户发行ERC1155]](https://github.com/andyYuanFZM/btyDemo/tree/master/src/test/solidity/ERC721ByUser.sol): NFT合约不限制只有管理员才能发行，任何用户都可以调用mint方法发行NFT， 适用于平台任意作者都可以发行NFT的业务场景。  

**备注：**
- 限制是否由管理员发行,实际就是在合约的mint方法中简单加了以下限制条件判断,其它逻辑没有任何区别。  
```  
 require(msg.sender == _owner, "only authorized owner can mint NFT.");
```  
- ***2. 如果在不清楚是选择用ERC721还是ERC1155情况下,建议首选使用ERC1155。***  

### NFT合约部署调用流程
应用和区块链交互流程： 
![Image text](https://github.com/andyYuanFZM/btyDemo/blob/master/src/test/resources/flow.png)   
1. 生成用户公私钥和区块链地址，应用层调用SDK中的创建公私钥方法，生成私钥和区块链地址。 然后将它们在应用层数据库中和用户关联，这样每一个用户都有了一个区块链上的身份。  
2. NFT合约部署，调用SDK构造部署合约的交易并签名该交易上链，一般而言NFT合约的部署是一次性动作（在没有合约逻辑变动，部署好后就固定运行在区块链上）， 建议合约的部署由专门的管理员来操作。管理员私钥和地址，也需要安全的存放在数据库中。  
3. NFT资产发行，调用SDK构造并签名发行NFT的交易。  
4. NFT资产转移，调用SDK构造并使用用户私钥签名转让NFT资产的交易。 
5. 查询动作，调用SDK查询NFT相关信息。 
6. 合约部署,资产发行,资产转移这些步骤都对应了一笔上链的交易,交易上链后接口会实时的返回一个交易hash，但此时交易并没有写入账本（因为共识，交易执行这些动作还在执行过程中）, 所以此时还无法判断交易能不能成功, 因此应用层在实现的时候需要异步的去根据这个hash查询上链结果，确认是成功的，才能在业务层判断成功,否则就要考虑异常处理（比如交易重传等）。    

