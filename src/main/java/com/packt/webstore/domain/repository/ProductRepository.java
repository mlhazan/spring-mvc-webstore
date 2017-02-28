package com.packt.webstore.domain.repository;

import java.util.*;
import com.packt.webstore.domain.*;

public interface ProductRepository {
	List<Product> getAllProducts();
	Product getProductById(String productID);
	
}
