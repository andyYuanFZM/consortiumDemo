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