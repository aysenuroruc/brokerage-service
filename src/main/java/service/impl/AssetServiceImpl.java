package service.impl;

import model.dto.AssetDto;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.mapper.AssetMapper;
import model.entity.Asset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.AssetRepository;
import service.AssetService;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AssetServiceImpl implements AssetService {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetMapper assetMapper;

    @Override
    public List<AssetDto> getAssets(Long customerId, String assetName) {
        if (assetName != null && !assetName.isBlank()) {
            return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                    .map(assetMapper::toDto)
                    .map(List::of)
                    .orElseGet(List::of);
        }
        return assetRepository.findByCustomerId(customerId)
                .stream()
                .map(assetMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public AssetDto createAsset(AssetDto assetDto) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(assetDto.getCustomerId(), assetDto.getAssetName())
                .orElseGet(() -> assetMapper.toEntity(assetDto));

        asset.setSize(assetDto.getSize() != null ? assetDto.getSize() : asset.getSize());
        asset.setUsableSize(assetDto.getUsableSize() != null ? assetDto.getUsableSize() : asset.getUsableSize());

        Asset saved = assetRepository.save(asset);
        return assetMapper.toDto(saved);
    }

    @Override
    public AssetDto updateUsableSize(Long id, Double usableSize) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));

        asset.setUsableSize(BigDecimal.valueOf(usableSize));
        Asset saved = assetRepository.save(asset);
        log.info("Updated usableSize for assetId={} to {}", id, usableSize);
        return assetMapper.toDto(saved);
    }

    @Override
    public void deleteAsset(Long id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Asset not found with id: " + id);
        }
        assetRepository.deleteById(id);
    }
}
