package org.example.login.controller;

import org.example.login.dto.Response.CategoriesResponse;
import org.example.login.dto.Response.ProductsResponse;
import org.example.login.dto.Response.ReviewsResponse;
import org.example.login.entity.Categories;
import org.example.login.entity.Products;
import org.example.login.entity.Reviews;
import org.example.login.service.ProductsService;
import org.example.login.service.ReviewsService;
import org.example.login.util.PriceFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
public class ProductCon {
    @Autowired
    ProductsService productsService;
    @Autowired
    ReviewsService reviewsService;

    @GetMapping("/list")
    public String productsList(Model model) {
        List<ProductsResponse> productsList = productsService.selectAll()
                .stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());

        model.addAttribute("productsList", productsList);
        return "/product/productlist";
    }

    @GetMapping("/{productId}")
    public String productDetail(@PathVariable Long productId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "3") int size,
                                Model model) {
        Products product = productsService.selectOne(productId);
        String formattedPrice = PriceFormatter.formatPrice(product.getPrice());

        Page<ReviewsResponse> reviewPage = reviewsService.getReviewsByProductId(productId, page, size)
                .map(this::convertToReviewsResponse);

        model.addAttribute("product", convertToProductResponse(product));
        model.addAttribute("formattedPrice", formattedPrice);
        model.addAttribute("reviewList", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("productId", productId);

        return "/product/productview";
    }

    private ProductsResponse convertToProductResponse(Products product) {
        Categories categories = product.getCategories();
        CategoriesResponse categoriesResponse = convertToCategoriesResponse(categories);

        return ProductsResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .img(product.getImg())
                .createdAt(product.getCreatedAt())
                .categories(categoriesResponse)
                .build();
    }

    private CategoriesResponse convertToCategoriesResponse(Categories categories) {
            return CategoriesResponse.builder()
                    .name(categories.getName())
                    .build();
    }

    private ReviewsResponse convertToReviewsResponse(Reviews review) {
        return ReviewsResponse.builder()
                .reviewId(review.getReviewId())
                .title(review.getTitle())
                .comment(review.getComment())
                .rating(review.getRating())
                .createdAt(review.getCreatedAt())
                .username(review.getUsers().getUsername())
                .build();
    }



}