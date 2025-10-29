package model.mapper;

import model.dto.OrderDto;
import model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    @Mapping(target = "createDate", expression = "java(order.getCreateDate() != null ? order.getCreateDate().toString() : null)")
    OrderDto toDto(Order order);

    @Mapping(target = "createDate", expression = "java(orderDto.getCreateDate() != null ? java.time.LocalDateTime.parse(orderDto.getCreateDate()) : java.time.LocalDateTime.now())")
    Order toEntity(OrderDto orderDto);
}
