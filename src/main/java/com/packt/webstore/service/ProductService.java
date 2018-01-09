package com.packt.webstore.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.packt.webstore.domain.*;
public interface ProductService {
	List <Product> getAllProducts();
	List<Product> getProductsByCategory(String category) ;
	Product getProductById(String productId);
	Set<Product> getProductsByFilter(Map<String, List<String>> filterParams);
	void addProduct(Product product);
	
}
