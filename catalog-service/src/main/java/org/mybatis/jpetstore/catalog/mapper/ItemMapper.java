package org.mybatis.jpetstore.catalog.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.jpetstore.catalog.domain.Item;

@Mapper
public interface ItemMapper {
  List<Item> getItemListByProduct(String productId);
  Item getItem(String itemId);
  int getInventoryQuantity(String itemId);
}
