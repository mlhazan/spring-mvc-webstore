package com.packt.webstore.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.InternalResourceView;

import com.packt.webstore.domain.Product;
import com.packt.webstore.exception.NoProductsFoundUnderCategoryException;
import com.packt.webstore.exception.ProductNotFoundException;
import com.packt.webstore.service.ProductService;
import com.packt.webstore.validator.ProductValidator;
import com.packt.webstore.validator.UnitsInStockValidator;
//import com.packt.webstore.domain.Product;

@Controller
@RequestMapping("/products")
public class ProductController {

	@Autowired
	private ProductService productService;
	// @Autowired
	// private UnitsInStockValidator unitsInStockValidator;
	@Autowired
	private ProductValidator productValidator;

	/**
	 * ProductService is an Interface and InmemoryProductRepository is Repository
	 * which get autowired
	 ***/
	// private ProductService productService = new
	// InMemoryProductRepository()

	// http://localhost:8080/webstore/products/
	@RequestMapping
	public String list(Model model) {
		model.addAttribute("products", productService.getAllProducts());
		return "products";
	}

	// http://localhost:8080/webstore/products/all
	@RequestMapping("/all")
	public String allProducts(Model model) {
		model.addAttribute("products", productService.getAllProducts());
		return "products";
	}

	// Same way we can write
	@RequestMapping("/alls")
	public ModelAndView allProducts() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("products", productService.getAllProducts());
		modelAndView.setViewName("products");
		return modelAndView;
	}

	// http://localhost:8080/webstore/products/tablet
	/**
	 * public String getProductsByCategory(Model model, @PathVariable("category")
	 * String productCategory) { model.addAttribute("products",
	 * productService.getProductsByCategory(productCategory)); return "products"; }
	 */
	@RequestMapping("/{category}")
	public String getProductsByCategory(Model model, @PathVariable("category") String category) {
		List<Product> products = productService.getProductsByCategory(category);
		if (products == null || products.isEmpty()) {
			throw new NoProductsFoundUnderCategoryException();
		}
		model.addAttribute("products", products);
		return "products";
	}

	// http://localhost:8080/webstore/products/filter/ByCriteria;category=tablet,laptop;brand=google,dell;
	// http://localhost:8080/webstore/products/filter/ByCriteria;brand=google,dell;category=tablet,laptop;
	@RequestMapping("/filter/{ByCriteria}")
	public String getProductsByFilter(@MatrixVariable(pathVar = "ByCriteria") Map<String, List<String>> filterParams,
			Model model) {
		model.addAttribute("products", productService.getProductsByFilter(filterParams));
		return "products";
	}

	@RequestMapping("/product")
	public String getProductById(@RequestParam("id") String productId, Model model) {
		model.addAttribute("product", productService.getProductById(productId));
		return "product";
	}

	// http://localhost:8080/webstore/products/add
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String getAddNewProductForm(Model model) {
		Product newProduct = new Product();
		model.addAttribute("newProduct", newProduct);
		return "addProduct";
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public String processAddNewProductForm(@ModelAttribute("newProduct") @Valid Product productToBeAdded,
			BindingResult result, HttpServletRequest request) {
		if (result.hasErrors()) {
			return "addProduct";
		}
		MultipartFile productImage = productToBeAdded.getProductImage();
		String rootDirectory = request.getSession().getServletContext().getRealPath("/");
		if (productImage != null && !productImage.isEmpty()) {
			try {
				productImage.transferTo(
						new File(rootDirectory + "resources\\images\\" + productToBeAdded.getProductId() + ".png"));
			} catch (Exception e) {
				throw new RuntimeException("Product Image saving failed", e);
			}
		}
		productService.addProduct(productToBeAdded);
		return "redirect:/products";
	}

	@InitBinder
	public void initialiseBinder(WebDataBinder binder) {
		binder.setAllowedFields("productId", "name", "unitPrice", "description", "manufacturer", "category",
				"unitsInStock", "condition", "productImage", "language");
		//binder.setValidator(unitsInStockValidator);
		binder.setValidator(productValidator);
	}

	// Not recommended
	// http://localhost:8080/webstore/products/home
	@RequestMapping("/home")
	public ModelAndView greeting(Map<String, Object> model) {
		model.put("greeting", "Welcome to Web Store!");
		model.put("tagline", "The one and only amazing web store");
		View view = new InternalResourceView("/WEB-INF/views/welcome.jsp");
		return new ModelAndView(view, model);
	}

	// http://localhost:8080/webstore/products/welcome/greeting
	// nothing particular be seen on page as model.put not used
	@RequestMapping("/welcome/greeting")
	public String greeting() {
		return "welcome";
	}

	// http://localhost:8080/webstore/products/welcome/greeting
	// nothing particular be seen on page as model.put not used
	@RequestMapping("/welcome/greeting2")
	public String greeting2() {
		return "forward:/products/welcome/greeting";
	}

	// http://localhost:8080/webstore/products/welcome/greeting
	// nothing particular be seen on page as model.put not used
	@RequestMapping("/welcome/greeting3")
	public String greeting3() {
		return "redirect:/products/welcome/greeting";
	}

	// @RequestMapping("/")
	// public String welcome(Model model) {
	// model.addAttribute("greeting", "Welcome to Web Store!");
	// model.addAttribute("tagline", "The one and only amazing web store");
	// return "forward:/welcome/greeting";
	// }

	// http://localhost:8080/webstore/products/product?id=P12349
	@ExceptionHandler(ProductNotFoundException.class)
	public ModelAndView handleError(HttpServletRequest req, ProductNotFoundException exception) {
		ModelAndView mav = new ModelAndView();
		mav.addObject("invalidProductId", exception.getProductId());
		mav.addObject("exception", exception);
		mav.addObject("url", req.getRequestURL() + "?" + req.getQueryString());
		mav.setViewName("productNotFound");
		return mav;
	}

	@RequestMapping("/invalidPromoCode")
	public String invalidPromoCode() {
		return "invalidPromoCode";
	}
}