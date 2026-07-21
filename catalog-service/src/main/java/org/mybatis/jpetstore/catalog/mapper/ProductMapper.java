package org.mybatis.jpetstore.catalog.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.jpetstore.catalog.domain.Product;

@Mapper
public interface ProductMapper {
  Product getProduct(String productId);
  List<Product> getProductListByCategory(String categoryId);
  List<Product> searchProductList(String keyword);
}
