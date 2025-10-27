package controller;

import model.dto.AssetDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.AssetService;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    @Autowired
    private AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetDto>> getAssets(
            @RequestParam Long customerId,
            @RequestParam(required = false) String assetName) {

        List<AssetDto> assets = assetService.getAssets(customerId, assetName);
        return ResponseEntity.ok(assets);
    }

    @PostMapping
    public ResponseEntity<AssetDto> createAsset(@Valid @RequestBody AssetDto assetDto) {
        AssetDto createdAsset = assetService.createAsset(assetDto);
        return ResponseEntity.ok(createdAsset);
    }

    @PatchMapping("/{id}/usable-size")
    public ResponseEntity<AssetDto> updateUsableSize(
            @PathVariable Long id,
            @RequestParam Double usableSize) {

        AssetDto updated = assetService.updateUsableSize(id, usableSize);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
