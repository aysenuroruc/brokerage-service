package model.mapper;

import model.dto.AssetDto;
import model.entity.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetDto toDto(Asset asset);
    Asset toEntity(AssetDto assetDto);
}
