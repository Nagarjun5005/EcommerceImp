package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.OrderDTO;
import com.ecommerce.project.payload.OrderItemDTO;
import com.ecommerce.project.repository.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.method.AuthorizeReturnObject;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AddressRepository addressRepository;


    @Autowired
    private  PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;


    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName,
                               String pgPaymentId, String pgStatus, String pgResponseMessage) {
        // 1️⃣ Get the user's cart using their email
        Cart cart=cartRepository.findCartByEmail(emailId);
        if (cart==null){
            throw new  ResourceNotFoundException("Cart","email",emailId);
        }
        // 2️⃣ Get the selected address for delivery
        Address address=addressRepository.findById(addressId).orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));
        // 3️⃣ Create a new Order object and populate its fields
        Order order=new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setAddress(address);
        order.setOrderStatus("Order accepted");
        order.setTotalAmount(cart.getTotalPrice());
        // 4️⃣ Create and save Payment details
        Payment payment=new Payment( paymentMethod,pgPaymentId,pgStatus,pgName,pgResponseMessage);
        payment.setOrder(order);
        paymentRepository.save(payment);
        // Link payment back to the order
        order.setPayment(payment);
        // Save the order
        Order savedOrder  = orderRepository.save(order);

        // 5️⃣ Move items from Cart to OrderItem list
        List<CartItem> cartItems = cart.getCartItems();
        if(cartItems==null){
            throw new APIException("cart is empty");
        }
        List<OrderItem>orderItems=new ArrayList<>();
        for(CartItem cartItem:cartItems){
            OrderItem orderItem=new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems=orderItemRepository.saveAll(orderItems);


       // 6️⃣ Update stock of each product and clear from cart
            cart.getCartItems().forEach(item->{
            int quantity= item.getQuantity();
            Product product=item.getProduct();
            product.setQuantity(product.getQuantity()-quantity);
            productRepository.save(product);

                // remove product from cart
            cartService.deleteProductFromCart(cart.getCartId(),product.getProductId());
        });



        // 7️⃣ Prepare and return response DTO (summary)
        OrderDTO orderDTO=modelMapper.map(savedOrder,OrderDTO.class);
        // For each order item, convert it to OrderItemDTO and add it to the orderDTO
        orderItems.forEach(item->orderDTO.getOrderItems()
                .add(modelMapper.map(item, OrderItemDTO.class)));

        // Set the address ID in the DTO (since DTO might only store the ID instead of the full Address object)
        orderDTO.setAddressId(addressId);
        return orderDTO;



    }
}
