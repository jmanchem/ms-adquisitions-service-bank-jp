package com.example.msacquisitionbank.services;

import com.example.msacquisitionbank.models.entities.Acquisition;
import com.example.msacquisitionbank.models.entities.Customer;
import com.example.msacquisitionbank.repositories.IAcquisitionRepository;
import com.example.msacquisitionbank.repositories.IRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class AcquisitionService extends BaseService<Acquisition, String> implements IAcquisitionService{

    private final IAcquisitionRepository acquisitionRepository;

    @Autowired
    public AcquisitionService(IAcquisitionRepository acquisitionRepository) {
        this.acquisitionRepository = acquisitionRepository;
    }

    @Override
    protected IRepository<Acquisition, String> getRepository() {
        return acquisitionRepository;
    }

    @Override
    public Mono<List<Acquisition>> findByCustomerIdentityNumber(String customerIdentityNumber) {
        return acquisitionRepository.findAll()
                .filter(p -> p.getCustomerHolder().get(0).getCustomerIdentityNumber().equals(customerIdentityNumber))
                .switchIfEmpty(Mono.error(new RuntimeException("The customer holder does not exist")))
                .collectList()
                .flatMap(Mono::just);
    }

    @Override
    public Mono<Acquisition> findByCardNumber(String cardNumber) {
        return acquisitionRepository.findByCardNumber(cardNumber);
    }

    @Override
    public Flux<Acquisition> findAllByCustomerHolder(List<Customer> customers) {
        return acquisitionRepository.findAllByCustomerHolder(customers);
    }
}
