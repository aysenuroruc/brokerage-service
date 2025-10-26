package service;

import dto.AssetDto;

import java.util.List;

public interface AssetService {
    List<AssetDto> getAssets(Long customerId, String assetName);
    AssetDto createAsset(AssetDto assetDto);         // <--- burada create var
    AssetDto updateUsableSize(Long id, Double usableSize);
    void deleteAsset(Long id);
}
