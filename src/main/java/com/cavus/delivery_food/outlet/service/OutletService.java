package com.cavus.delivery_food.outlet.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cavus.delivery_food.outlet.dto.OutletRequest;
import com.cavus.delivery_food.outlet.dto.OutletResponse;
import com.cavus.delivery_food.outlet.entity.Outlet;
import com.cavus.delivery_food.outlet.mapper.OutletMapper;
import com.cavus.delivery_food.outlet.repository.OutletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class OutletService {

    private final OutletRepository outletRepository;
    private final OutletMapper outletMapper;

    public OutletResponse cretate(OutletRequest request) {
        String normalizedName = normalizeName(request.getName());

        if(outletRepository.existsByNameIgnoreCase(normalizedName)){
            throw new OutletExistException();
        }

        Outlet outlet = outletMapper.toEntity(request);
        outlet.setName(normalizedName);
        return outletMapper.toResponse( outletRepository.save(outlet));
    }

    public List<OutletResponse> findAll() {
        return outletMapper.toOutletResponseList(outletRepository.findAll());
    }

    public List<OutletResponse> findAllByActive() {
        return outletMapper.toOutletResponseList(outletRepository.findByActiveTrue());
    }

    public OutletResponse findById(UUID id) {
        if(id == null){
            throw new IllegalArgumentException("ID boş olamaz");
        }
        return outletMapper.toResponse(outletRepository.findById(id)
            .orElseThrow(() -> new OutletNotFoundException(id)));
    }

    // ! Burada productService'de ürünü hangi outlet'e ekleyeceğimizi bulmak için kullanıyoruz.
    public Outlet getEntityById(UUID id) {
        if(id == null){
            throw new IllegalArgumentException("ID boş olamaz");
        }
        return outletRepository.findById(id)
            .orElseThrow(() -> new OutletNotFoundException(id));
    }

    public OutletResponse update(UUID uuid, OutletRequest request) {
        Outlet outlet = getEntityById(uuid);

        if (request.getName() != null) {
            String normalizedName = normalizeName(request.getName());

            if (normalizedName.equalsIgnoreCase(outlet.getName())
                    && outletRepository.existsByNameIgnoreCase(normalizedName)) {
                throw new OutletExistException();
            }

            request.setName(normalizedName);
        }

        outletMapper.updateOutletFromRequest(request, outlet);
        return outletMapper.toResponse(outletRepository.save(outlet));

    }
    
    public List<OutletResponse> createBulk(List<OutletRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Outlet listesi boş olamaz");
        }

        Set<String> names = new HashSet<>();
        
        List<Outlet> outlets = requests.stream().map(t -> {
            String normalizedName = normalizeName(t.getName());
            
            if (!names.add(normalizedName)) {
                throw new IllegalArgumentException("Outlet adı tekrar edemez: " + normalizedName);
            }

            if(outletRepository.existsByNameIgnoreCase(normalizedName)){
                throw new OutletExistException();
            }

            Outlet outlet = outletMapper.toEntity(t);
            outlet.setName(normalizedName);
            return outlet;
        }).toList();

        return outletMapper.toOutletResponseList(outletRepository.saveAll(outlets));
    }


    
    

    private String normalizeName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
