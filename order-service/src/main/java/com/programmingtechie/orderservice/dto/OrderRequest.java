package com.programmingtechie.orderservice.dto;

import com.programmingtechie.orderservice.model.OrderLineItems;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderLineItemsDto> orderLineItemsDtoList;
}