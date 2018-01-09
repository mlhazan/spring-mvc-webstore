package com.packt.webstore.domain.repository;

import java.util.*;
import com.packt.webstore.domain.*;

public interface ProductRepository {
	List<Product> getAllProducts();
	Product getProductById(String productID);
	List<Product> getProductsByCategory(String  category);
	Set<Product> getProductsByFilter(Map<String, List<String>> filterParams);
	void addProduct(Product product);

}
