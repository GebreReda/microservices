package com.programmingtechie.orderservice.service;

import com.programmingtechie.orderservice.dto.InventoryResponse;
import com.programmingtechie.orderservice.dto.OrderLineItemsDto;
import com.programmingtechie.orderservice.dto.OrderRequest;
import com.programmingtechie.orderservice.model.Order;
import com.programmingtechie.orderservice.model.OrderLineItems;
import com.programmingtechie.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
     public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
         List<OrderLineItems> orderLineItems = orderRequest
                 .getOrderLineItemsDtoList()
                 .stream()
                 .map(this::mapToDto)
                 .toList();
         order.setOrderLineItems(orderLineItems);

         //Collecct skuCodes of current order
         List<String> skuCodes = order.getOrderLineItems().stream().map(OrderLineItems::getSkuCode).toList();
         System.out.println(skuCodes);

         //check if there are items in stack before placing an order
         InventoryResponse[] inventoryResponsesArray = webClientBuilder.build().get()
                 .uri("http://inventory-service/api/inventory",
                         uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                  .retrieve()
                  .bodyToMono(InventoryResponse[].class)
                  .block();

         boolean allProductsInStock = Arrays.stream(inventoryResponsesArray)
                 .allMatch(InventoryResponse::getIsInStock);
         if(allProductsInStock)
             orderRepository.save(order);
         else throw new IllegalArgumentException("Sorry item does not exist in stalk");
     }
     private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
         OrderLineItems orderLineItems = new OrderLineItems();
         orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
         orderLineItems.setPrice(orderLineItemsDto.getPrice());
         orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

         return orderLineItems;
     }
}
